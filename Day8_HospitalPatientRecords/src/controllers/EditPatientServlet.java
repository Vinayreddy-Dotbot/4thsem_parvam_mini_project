package controllers;

import dao.PatientDao;
import dao.DoctorDao;
import models.Doctor;
import models.Patient;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

@SuppressWarnings("serial")
public class EditPatientServlet extends HttpServlet {
    private final PatientDao patientDao = new PatientDao();
    private final DoctorDao doctorDao = new DoctorDao();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setContentType("text/html;charset=UTF-8");

        try {
            int id = Integer.parseInt(request.getParameter("id"));
            Patient patient = patientDao.getPatientById(id);
            if (patient == null) {
                response.sendRedirect("list");
                return;
            }
            renderForm(response, patient, "");
        } catch (Exception e) {
            response.sendRedirect("list");
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setContentType("text/html;charset=UTF-8");

        try {
            Patient existing = patientDao.getPatientById(Integer.parseInt(request.getParameter("id")));
            if (existing == null) {
                response.sendRedirect("list");
                return;
            }

            Patient patient = readPatient(request, existing);
            if (patient.getName().isEmpty() || patient.getGender().isEmpty() || patient.getBlood().isEmpty()
                    || patient.getPhone().isEmpty() || patient.getAddress().isEmpty()
                    || patient.getWard().isEmpty() || patient.getStatus().isEmpty()) {
                renderForm(response, patient, "All patient and admission fields are required.");
                return;
            }

            if (patient.getAge() < 0) {
                renderForm(response, patient, "Age cannot be negative.");
                return;
            }

            patientDao.updatePatient(patient);
            response.sendRedirect("detail?id=" + patient.getId());
        } catch (NumberFormatException e) {
            response.sendRedirect("list");
        } catch (SQLException e) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.getWriter().write("Database error while updating patient: " + HtmlUtil.escape(e.getMessage()));
        }
    }

    private Patient readPatient(HttpServletRequest request, Patient existing) {
        existing.setName(value(request, "name"));
        existing.setAge(parseInt(value(request, "age"), -1));
        existing.setGender(value(request, "gender"));
        existing.setBlood(value(request, "blood"));
        existing.setPhone(value(request, "phone"));
        existing.setAddress(value(request, "address"));
        existing.setDoctorId(parseInt(value(request, "doctorId"), existing.getDoctorId()));
        existing.setWard(value(request, "ward"));
        existing.setStatus(value(request, "status"));
        existing.setAppointmentAt(value(request, "appointmentAt"));
        existing.setAppointmentNotes(value(request, "appointmentNotes"));
        return existing;
    }

    private int parseInt(String value, int fallback) {
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            return fallback;
        }
    }

    private String value(HttpServletRequest request, String name) {
        String value = request.getParameter(name);
        return value == null ? "" : value.trim();
    }

    private void renderForm(HttpServletResponse response, Patient patient, String errorMessage)
            throws IOException {
        String htmlContent = TemplateEngine.render(getServletContext(), "edit.html");
        htmlContent = RegisterPatientServlet.fillPatientPlaceholders(htmlContent, patient);
        htmlContent = htmlContent.replace("{{PATIENT_ID}}", String.valueOf(patient.getId()));
        htmlContent = htmlContent.replace("{{ADMISSION_ID}}", String.valueOf(patient.getAdmissionId()));
        htmlContent = htmlContent.replace("{{ERROR_MESSAGE}}", RegisterPatientServlet.buildError(errorMessage));
        htmlContent = htmlContent.replace("{{DOCTOR_OPTIONS}}", buildDoctorOptions(patient.getDoctorId()));
        htmlContent = htmlContent.replace("{{STATUS_OPTIONS}}", RegisterPatientServlet.buildStatusOptions(patient.getStatus(), true));
        response.getWriter().write(htmlContent);
    }

    private String buildDoctorOptions(int selectedDoctorId) {
        StringBuilder options = new StringBuilder();
        String currentDept = null;

        try {
            List<Doctor> doctors = doctorDao.getAllDoctors();
            for (Doctor doctor : doctors) {
                if (currentDept == null || !currentDept.equals(doctor.getDept())) {
                    if (currentDept != null) {
                        options.append("</optgroup>");
                    }
                    currentDept = doctor.getDept();
                    options.append("<optgroup label='").append(HtmlUtil.escape(currentDept)).append("'>");
                }
                options.append("<option value='").append(doctor.getId()).append("'");
                if (doctor.getId() == selectedDoctorId) {
                    options.append(" selected");
                }
                options.append(">")
                    .append(HtmlUtil.escape(doctor.getName()))
                    .append(" - ")
                    .append(HtmlUtil.escape(doctor.getDept()))
                    .append("</option>");
            }
            if (currentDept != null) {
                options.append("</optgroup>");
            }
        } catch (SQLException e) {
            options.append("<option disabled>Failed to load doctors: ")
                .append(HtmlUtil.escape(e.getMessage()))
                .append("</option>");
        }
        return options.toString();
    }
}
