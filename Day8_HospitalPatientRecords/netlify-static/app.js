const doctors = [
  { id: 1, name: "Dr. Asha Rao", dept: "Cardiology" },
  { id: 2, name: "Dr. Kiran Patel", dept: "Cardiology" },
  { id: 3, name: "Dr. Meera Singh", dept: "Neurology" },
  { id: 4, name: "Dr. Rohan Das", dept: "Orthopedics" },
  { id: 5, name: "Dr. Kavya Nair", dept: "General Medicine" },
  { id: 6, name: "Dr. Sameer Khan", dept: "Emergency" }
];

const seed = {
  patients: [
    { id: 1001, name: "Rahul Kumar", age: 54, gender: "Male", blood: "O+", phone: "9876543210", address: "Bengaluru", issue: "Chest pain and high blood pressure", admittedAt: "2026-05-19T09:30" },
    { id: 1002, name: "Ananya Reddy", age: 23, gender: "Female", blood: "A+", phone: "9988776655", address: "Mysuru", issue: "High fever and weakness", admittedAt: "2026-05-20T11:15" },
    { id: 1003, name: "Mohammed Ali", age: 67, gender: "Male", blood: "B+", phone: "9123456780", address: "Jayanagar", issue: "Breathing issue and diabetes history", admittedAt: "2026-05-18T16:45" },
    { id: 1004, name: "Priya Sharma", age: 31, gender: "Female", blood: "AB+", phone: "9012345678", address: "Banashankari", issue: "Fracture after road accident", admittedAt: "2026-05-21T08:10" }
  ],
  admissions: [
    { id: 1, patientId: 1001, doctorId: 1, ward: "ICU-2", status: "Emergency", appointmentAt: "2026-05-22T10:00", appointmentNotes: "ECG review and BP monitoring" },
    { id: 2, patientId: 1002, doctorId: 5, ward: "G-12", status: "Under Observation", appointmentAt: "2026-05-22T14:30", appointmentNotes: "CBC test follow-up" },
    { id: 3, patientId: 1003, doctorId: 6, ward: "ER-1", status: "Emergency", appointmentAt: "2026-05-21T18:00", appointmentNotes: "Oxygen level review" },
    { id: 4, patientId: 1004, doctorId: 4, ward: "O-5", status: "Admitted", appointmentAt: "2026-05-23T09:00", appointmentNotes: "X-ray and plaster review" }
  ]
};

let state = loadState();
let selectedId = null;

const $ = (selector) => document.querySelector(selector);
const $$ = (selector) => Array.from(document.querySelectorAll(selector));

function loadState() {
  const saved = localStorage.getItem("hospital-records-netlify");
  if (saved) return JSON.parse(saved);
  localStorage.setItem("hospital-records-netlify", JSON.stringify(seed));
  return structuredClone(seed);
}

function saveState() {
  localStorage.setItem("hospital-records-netlify", JSON.stringify(state));
}

function admissionFor(patientId) {
  return state.admissions.find((admission) => admission.patientId === patientId);
}

function doctorFor(id) {
  return doctors.find((doctor) => doctor.id === Number(id)) || doctors[0];
}

function riskFor(patient, admission) {
  const text = `${patient.issue} ${admission.status}`.toLowerCase();
  const critical = ["chest", "breathing", "stroke", "accident", "bleeding", "emergency", "icu"];
  if (critical.some((word) => text.includes(word)) || patient.age >= 65) {
    return { level: "High", className: "high", note: "Priority review needed. Monitor vitals and doctor workload." };
  }
  if (patient.age >= 50 || text.includes("fever") || text.includes("diabetes")) {
    return { level: "Medium", className: "medium", note: "Needs observation and scheduled follow-up." };
  }
  return { level: "Low", className: "low", note: "Stable case with normal follow-up flow." };
}

function summaryFor(patient, admission) {
  const doctor = doctorFor(admission.doctorId);
  const risk = riskFor(patient, admission);
  return `${patient.name} is a ${patient.age}-year-old ${patient.gender.toLowerCase()} patient admitted in ${admission.ward} under ${doctor.name} (${doctor.dept}). Main issue: ${patient.issue}. Current status is ${admission.status}. AI priority is ${risk.level}.`;
}

function setView(id) {
  $$(".view").forEach((view) => view.classList.toggle("active-view", view.id === id));
  $$(".nav-link").forEach((link) => link.classList.toggle("active", link.dataset.view === id));
  history.replaceState(null, "", `#${id}`);
}

function populateDepartments() {
  const dept = $("#dept");
  dept.innerHTML = [...new Set(doctors.map((doctor) => doctor.dept))]
    .map((name) => `<option>${name}</option>`)
    .join("");
  updateDoctorOptions();
}

function updateDoctorOptions(selectedDoctorId) {
  const dept = $("#dept").value;
  const options = doctors
    .filter((doctor) => doctor.dept === dept)
    .map((doctor) => `<option value="${doctor.id}" ${Number(selectedDoctorId) === doctor.id ? "selected" : ""}>${doctor.name}</option>`)
    .join("");
  $("#doctor").innerHTML = options;
}

function renderStats() {
  const total = state.patients.length;
  const active = state.admissions.filter((a) => a.status !== "Discharged").length;
  const emergency = state.admissions.filter((a) => a.status === "Emergency").length;
  const appointments = state.admissions.filter((a) => a.appointmentAt).length;
  $("#statsGrid").innerHTML = [
    ["Total Patients", total],
    ["Active Admissions", active],
    ["Emergency Cases", emergency],
    ["Appointments", appointments]
  ].map(([label, value]) => `<article class="stat-card"><strong>${value}</strong><span>${label}</span></article>`).join("");

  const statuses = ["Admitted", "Under Observation", "Emergency", "Discharged"];
  $("#statusChart").innerHTML = statuses.map((status) => {
    const count = state.admissions.filter((a) => a.status === status).length;
    const width = total ? Math.max(8, (count / total) * 100) : 0;
    return `<div class="bar-row"><div class="bar-meta"><span>${status}</span><strong>${count}</strong></div><div class="bar"><span style="width:${width}%"></span></div></div>`;
  }).join("");

  $("#doctorWorkload").innerHTML = doctors.map((doctor) => {
    const count = state.admissions.filter((a) => a.doctorId === doctor.id && a.status !== "Discharged").length;
    return `<div class="workload-row"><span>${doctor.name}<small> · ${doctor.dept}</small></span><strong>${count}</strong></div>`;
  }).join("");
}

function renderPatients(list = state.patients) {
  if (!list.length) {
    $("#patientList").innerHTML = `<article class="panel"><h3>No records found</h3><p>Try another search query.</p></article>`;
    return;
  }
  $("#patientList").innerHTML = list.map((patient) => {
    const admission = admissionFor(patient.id);
    const doctor = doctorFor(admission.doctorId);
    const statusClass = admission.status.toLowerCase().replace("under ", "").replace(" ", "-");
    return `<article class="record-card" data-id="${patient.id}">
      <div class="avatar">${patient.name.slice(0, 1)}</div>
      <div>
        <h3>${patient.name} <small>HP-${patient.id}</small></h3>
        <p>${patient.issue} · ${doctor.dept} · ${admission.ward}</p>
      </div>
      <span class="status-pill ${statusClass}">${admission.status}</span>
    </article>`;
  }).join("");
}

function renderDetail(patientId) {
  selectedId = patientId;
  const patient = state.patients.find((item) => item.id === patientId);
  const admission = admissionFor(patientId);
  if (!patient || !admission) return;
  const doctor = doctorFor(admission.doctorId);
  const risk = riskFor(patient, admission);
  $("#detailPanel").innerHTML = `<h2>${patient.name}</h2>
    <p class="eyebrow">HP-${patient.id}</p>
    <div class="risk ${risk.className}">AI Risk: ${risk.level} · ${risk.note}</div>
    <div class="detail-list">
      <div><span>Age / Gender</span><strong>${patient.age} / ${patient.gender}</strong></div>
      <div><span>Blood</span><strong>${patient.blood}</strong></div>
      <div><span>Phone</span><strong>${patient.phone}</strong></div>
      <div><span>Doctor</span><strong>${doctor.name}</strong></div>
      <div><span>Department</span><strong>${doctor.dept}</strong></div>
      <div><span>Ward</span><strong>${admission.ward}</strong></div>
      <div><span>Status</span><strong>${admission.status}</strong></div>
      <div><span>Appointment</span><strong>${formatDate(admission.appointmentAt)}</strong></div>
    </div>
    <h3>AI Health Summary</h3>
    <p>${summaryFor(patient, admission)}</p>
    <div class="detail-actions">
      <button class="button primary" data-action="edit">Edit</button>
      <button class="button ghost" data-action="discharge">Discharge</button>
      <button class="button ghost" data-action="delete">Delete</button>
    </div>`;
}

function renderAi() {
  const highRisk = state.patients.filter((patient) => riskFor(patient, admissionFor(patient.id)).level === "High");
  const deptCounts = doctors.reduce((acc, doctor) => {
    acc[doctor.dept] = state.admissions.filter((a) => doctorFor(a.doctorId).dept === doctor.dept).length;
    return acc;
  }, {});
  $("#aiGrid").innerHTML = [
    ["AI Health Summary", "Generates a short patient report using patient details, issue, admission status and assigned doctor.", "HS"],
    ["AI Disease Risk Prediction", `High priority cases now detected: ${highRisk.length}. Rules check age, emergency status and critical issue keywords.`, "RP"],
    ["AI Smart Patient Search", "Supports natural phrases like admitted patients, discharged, emergency, cardiology or patient ID.", "SS"],
    ["AI Doctor Recommendation", "Doctor choices are grouped by department, helping staff assign the correct specialist.", "DR"],
    ["AI Emergency Alert", highRisk.length ? highRisk.map((p) => `HP-${p.id}: ${p.name}`).join(", ") : "No emergency-priority cases right now.", "EA"],
    ["AI Analytics Dashboard", Object.entries(deptCounts).map(([dept, count]) => `${dept}: ${count}`).join(" · "), "AD"]
  ].map(([title, body, icon]) => `<article class="ai-card"><div class="ai-icon">${icon}</div><h3>${title}</h3><p>${body}</p></article>`).join("");
}

function smartFilter(query) {
  const q = query.trim().toLowerCase();
  if (!q) return state.patients;
  return state.patients.filter((patient) => {
    const admission = admissionFor(patient.id);
    const doctor = doctorFor(admission.doctorId);
    const haystack = `hp-${patient.id} ${patient.id} ${patient.name} ${patient.issue} ${patient.blood} ${admission.status} ${admission.ward} ${doctor.name} ${doctor.dept}`.toLowerCase();
    if (q.includes("admitted")) return admission.status === "Admitted";
    if (q.includes("observation")) return admission.status === "Under Observation";
    if (q.includes("emergency") || q.includes("critical")) return admission.status === "Emergency" || riskFor(patient, admission).level === "High";
    if (q.includes("discharged")) return admission.status === "Discharged";
    if (q.includes("appointment")) return Boolean(admission.appointmentAt);
    return haystack.includes(q);
  });
}

function submitPatient(event) {
  event.preventDefault();
  const editId = Number($("#editId").value);
  const patient = {
    id: editId || Math.max(...state.patients.map((p) => p.id), 1000) + 1,
    name: $("#name").value.trim(),
    age: Number($("#age").value),
    gender: $("#gender").value,
    blood: $("#blood").value,
    phone: $("#phone").value.trim(),
    address: $("#address").value.trim(),
    issue: $("#issue").value.trim(),
    admittedAt: editId ? state.patients.find((p) => p.id === editId).admittedAt : new Date().toISOString().slice(0, 16)
  };
  const admission = {
    id: editId ? admissionFor(editId).id : Math.max(...state.admissions.map((a) => a.id), 0) + 1,
    patientId: patient.id,
    doctorId: Number($("#doctor").value),
    ward: $("#ward").value.trim(),
    status: $("#status").value,
    appointmentAt: $("#appointmentAt").value,
    appointmentNotes: $("#appointmentNotes").value.trim()
  };

  if (editId) {
    state.patients = state.patients.map((item) => item.id === editId ? patient : item);
    state.admissions = state.admissions.map((item) => item.patientId === editId ? admission : item);
    toast("Patient updated");
  } else {
    state.patients.unshift(patient);
    state.admissions.unshift(admission);
    toast("Patient registered");
  }
  saveState();
  resetForm();
  renderAll();
  setView("records");
  renderDetail(patient.id);
}

function editSelected() {
  const patient = state.patients.find((item) => item.id === selectedId);
  const admission = admissionFor(selectedId);
  const doctor = doctorFor(admission.doctorId);
  $("#editId").value = patient.id;
  $("#name").value = patient.name;
  $("#age").value = patient.age;
  $("#gender").value = patient.gender;
  $("#blood").value = patient.blood;
  $("#phone").value = patient.phone;
  $("#address").value = patient.address;
  $("#issue").value = patient.issue;
  $("#dept").value = doctor.dept;
  updateDoctorOptions(doctor.id);
  $("#ward").value = admission.ward;
  $("#status").value = admission.status;
  $("#appointmentAt").value = admission.appointmentAt || "";
  $("#appointmentNotes").value = admission.appointmentNotes || "";
  setView("register");
  toast("Edit mode enabled");
}

function dischargeSelected() {
  const admission = admissionFor(selectedId);
  admission.status = "Discharged";
  saveState();
  renderAll();
  renderDetail(selectedId);
  toast("Patient discharged");
}

function deleteSelected() {
  state.patients = state.patients.filter((patient) => patient.id !== selectedId);
  state.admissions = state.admissions.filter((admission) => admission.patientId !== selectedId);
  selectedId = null;
  saveState();
  renderAll();
  $("#detailPanel").innerHTML = `<div class="empty-state"><strong>Select a patient</strong><span>Patient details, AI summary and actions will appear here.</span></div>`;
  toast("Patient deleted");
}

function resetForm() {
  $("#patientForm").reset();
  $("#editId").value = "";
  populateDepartments();
}

function formatDate(value) {
  if (!value) return "Not scheduled";
  return new Date(value).toLocaleString([], { dateStyle: "medium", timeStyle: "short" });
}

function toast(message) {
  const node = $("#toast");
  node.textContent = message;
  node.classList.add("show");
  window.clearTimeout(toast.timer);
  toast.timer = window.setTimeout(() => node.classList.remove("show"), 2200);
}

function renderAll() {
  renderStats();
  renderPatients(smartFilter($("#smartSearch").value));
  renderAi();
}

function bindEvents() {
  $$(".nav-link, [data-view]").forEach((link) => {
    link.addEventListener("click", (event) => {
      event.preventDefault();
      setView(link.dataset.view);
    });
  });
  $("#dept").addEventListener("change", () => updateDoctorOptions());
  $("#patientForm").addEventListener("submit", submitPatient);
  $("#resetForm").addEventListener("click", resetForm);
  $("#searchBtn").addEventListener("click", () => {
    setView("records");
    renderPatients(smartFilter($("#smartSearch").value));
  });
  $("#smartSearch").addEventListener("input", () => renderPatients(smartFilter($("#smartSearch").value)));
  $("#patientList").addEventListener("click", (event) => {
    const card = event.target.closest(".record-card");
    if (card) renderDetail(Number(card.dataset.id));
  });
  $("#detailPanel").addEventListener("click", (event) => {
    const action = event.target.dataset.action;
    if (action === "edit") editSelected();
    if (action === "discharge") dischargeSelected();
    if (action === "delete") deleteSelected();
  });
}

function init() {
  populateDepartments();
  bindEvents();
  renderAll();
  const startView = location.hash.replace("#", "") || "home";
  setView(["home", "register", "records", "ai"].includes(startView) ? startView : "home");
}

init();
