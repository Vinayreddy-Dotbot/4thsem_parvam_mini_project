package controllers;

import dao.DoctorDao;
import dao.PatientDao;
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
public class RegisterPatientServlet extends HttpServlet {
    private final DoctorDao doctorDao = new DoctorDao();
    private final PatientDao patientDao = new PatientDao();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setContentType("text/html;charset=UTF-8");
        renderForm(response, new Patient(), "", 0);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setContentType("text/html;charset=UTF-8");

        Patient patient = readPatient(request);
        String doctorId = request.getParameter("doctorId");

        if (patient.getName().isEmpty() || patient.getGender().isEmpty() || patient.getBlood().isEmpty()
                || patient.getPhone().isEmpty() || patient.getAddress().isEmpty()
                || patient.getWard().isEmpty() || patient.getStatus().isEmpty()
                || doctorId == null || doctorId.trim().isEmpty()) {
            renderForm(response, patient, "All patient and admission fields are required.", 0);
            return;
        }

        try {
            patient.setDoctorId(Integer.parseInt(doctorId.trim()));
            if (patient.getAge() < 0) {
                renderForm(response, patient, "Age cannot be negative.", patient.getDoctorId());
                return;
            }

            int patientId = patientDao.addPatient(patient);
            response.sendRedirect("detail?id=" + patientId);
        } catch (NumberFormatException e) {
            renderForm(response, patient, "Age and doctor selection must be valid values.", 0);
        } catch (SQLException e) {
            renderForm(response, patient, "Database error: " + e.getMessage(), patient.getDoctorId());
        }
    }

    private Patient readPatient(HttpServletRequest request) {
        Patient patient = new Patient();
        patient.setName(value(request, "name"));
        patient.setGender(value(request, "gender"));
        patient.setBlood(value(request, "blood"));
        patient.setPhone(value(request, "phone"));
        patient.setAddress(value(request, "address"));
        patient.setWard(value(request, "ward"));
        patient.setStatus(value(request, "status"));
        patient.setAppointmentAt(value(request, "appointmentAt"));
        patient.setAppointmentNotes(value(request, "appointmentNotes"));

        try {
            patient.setAge(Integer.parseInt(value(request, "age")));
        } catch (NumberFormatException e) {
            patient.setAge(-1);
        }
        return patient;
    }

    private String value(HttpServletRequest request, String name) {
        String value = request.getParameter(name);
        return value == null ? "" : value.trim();
    }

    private void renderForm(HttpServletResponse response, Patient patient, String errorMessage, int selectedDoctorId)
            throws IOException {
        String htmlContent = TemplateEngine.render(getServletContext(), "register.html");
        htmlContent = fillPatientPlaceholders(htmlContent, patient);
        htmlContent = htmlContent.replace("{{ERROR_MESSAGE}}", buildError(errorMessage));
        htmlContent = htmlContent.replace("{{DOCTOR_OPTIONS}}", buildDoctorOptions(selectedDoctorId));
        htmlContent = htmlContent.replace("{{STATUS_OPTIONS}}", buildStatusOptions(patient.getStatus(), false));
        response.getWriter().write(htmlContent);
    }

    static String fillPatientPlaceholders(String htmlContent, Patient patient) {
        htmlContent = htmlContent.replace("{{PATIENT_NAME}}", HtmlUtil.escape(patient.getName()));
        htmlContent = htmlContent.replace("{{PATIENT_AGE}}", patient.getAge() >= 0 ? String.valueOf(patient.getAge()) : "");
        htmlContent = htmlContent.replace("{{PATIENT_GENDER}}", HtmlUtil.escape(patient.getGender()));
        htmlContent = htmlContent.replace("{{PATIENT_BLOOD}}", HtmlUtil.escape(patient.getBlood()));
        htmlContent = htmlContent.replace("{{PATIENT_PHONE}}", HtmlUtil.escape(patient.getPhone()));
        htmlContent = htmlContent.replace("{{PATIENT_ADDRESS}}", HtmlUtil.escape(patient.getAddress()));
        htmlContent = htmlContent.replace("{{PATIENT_WARD}}", HtmlUtil.escape(patient.getWard()));
        htmlContent = htmlContent.replace("{{APPOINTMENT_AT}}", HtmlUtil.toDateTimeLocal(patient.getAppointmentAt()));
        htmlContent = htmlContent.replace("{{APPOINTMENT_NOTES}}", HtmlUtil.escape(patient.getAppointmentNotes()));
        return htmlContent;
    }

    static String buildError(String errorMessage) {
        if (errorMessage == null || errorMessage.trim().isEmpty()) {
            return "";
        }
        return "<div class='alert-box alert-danger'><i class='fa-solid fa-triangle-exclamation'></i> "
            + HtmlUtil.escape(errorMessage) + "</div>";
    }

    static String buildStatusOptions(String selectedStatus, boolean includeDischarged) {
        String[] statuses = includeDischarged
            ? new String[] {"admitted", "under observation", "emergency", "discharged"}
            : new String[] {"admitted", "under observation", "emergency"};
        String selected = selectedStatus == null || selectedStatus.trim().isEmpty() ? "admitted" : selectedStatus;
        StringBuilder options = new StringBuilder();
        for (String status : statuses) {
            options.append("<option value='").append(HtmlUtil.escape(status)).append("'");
            if (status.equalsIgnoreCase(selected)) {
                options.append(" selected");
            }
            options.append(">").append(HtmlUtil.escape(formatStatus(status))).append("</option>");
        }
        return options.toString();
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

    private static String formatStatus(String value) {
        String[] parts = value.split("\\s+");
        StringBuilder result = new StringBuilder();
        for (String part : parts) {
            if (result.length() > 0) {
                result.append(" ");
            }
            result.append(Character.toUpperCase(part.charAt(0))).append(part.substring(1));
        }
        return result.toString();
    }
}
