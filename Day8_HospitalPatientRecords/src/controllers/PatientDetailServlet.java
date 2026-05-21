package controllers;

import dao.PatientDao;
import models.Patient;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.sql.SQLException;

@SuppressWarnings("serial")
public class PatientDetailServlet extends HttpServlet {
    private final PatientDao patientDao = new PatientDao();

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

            String htmlContent = TemplateEngine.render(getServletContext(), "detail.html");
            htmlContent = htmlContent.replace("{{PATIENT_ID}}", String.valueOf(patient.getId()));
            htmlContent = htmlContent.replace("{{PATIENT_NAME}}", HtmlUtil.escape(patient.getName()));
            htmlContent = htmlContent.replace("{{PATIENT_AGE}}", String.valueOf(patient.getAge()));
            htmlContent = htmlContent.replace("{{PATIENT_GENDER}}", HtmlUtil.escape(patient.getGender()));
            htmlContent = htmlContent.replace("{{PATIENT_BLOOD}}", HtmlUtil.escape(patient.getBlood()));
            htmlContent = htmlContent.replace("{{PATIENT_PHONE}}", HtmlUtil.escape(patient.getPhone()));
            htmlContent = htmlContent.replace("{{PATIENT_ADDRESS}}", HtmlUtil.escape(patient.getAddress()));
            htmlContent = htmlContent.replace("{{ADMITTED_AT}}", HtmlUtil.shortDateTime(patient.getAdmittedAt()));
            htmlContent = htmlContent.replace("{{DOCTOR_NAME}}", HtmlUtil.escape(patient.getDoctorName()));
            htmlContent = htmlContent.replace("{{DOCTOR_DEPT}}", HtmlUtil.escape(patient.getDoctorDept()));
            htmlContent = htmlContent.replace("{{WARD}}", HtmlUtil.escape(patient.getWard()));
            htmlContent = htmlContent.replace("{{STATUS}}", HtmlUtil.escape(patient.getStatusDisplay()));
            htmlContent = htmlContent.replace("{{APPOINTMENT_AT}}", displayAppointmentAt(patient));
            htmlContent = htmlContent.replace("{{APPOINTMENT_NOTES}}", displayAppointmentNotes(patient));
            htmlContent = htmlContent.replace("{{STATUS_CLASS}}", HtmlUtil.escape(HospitalAiService.getRiskClass(patient)));
            htmlContent = htmlContent.replace("{{PRIORITY_LABEL}}", HtmlUtil.escape(HospitalAiService.getPriorityLabel(patient)));
            htmlContent = htmlContent.replace("{{RISK_LEVEL}}", HtmlUtil.escape(HospitalAiService.getRiskLevel(patient)));
            htmlContent = htmlContent.replace("{{RISK_REASONS}}", HtmlUtil.escape(HospitalAiService.getRiskReasons(patient)));
            htmlContent = htmlContent.replace("{{AI_SUMMARY}}", HtmlUtil.escape(HospitalAiService.getHealthSummary(patient)));
            htmlContent = htmlContent.replace("{{AI_DOCTOR_RECOMMENDATION}}", HtmlUtil.escape(HospitalAiService.getDoctorRecommendation(patient)));
            htmlContent = htmlContent.replace("{{AI_EMERGENCY_ACTION}}", HtmlUtil.escape(HospitalAiService.getEmergencyAction(patient)));
            htmlContent = htmlContent.replace("{{DISCHARGE_ACTION}}", patient.isDischarged() ? "" :
                "<a href='discharge?id=" + patient.getId() + "' class='btn btn-danger'><i class='fa-solid fa-door-open'></i> Discharge</a>");
            response.getWriter().write(htmlContent);
        } catch (NumberFormatException e) {
            response.sendRedirect("list");
        } catch (SQLException e) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.getWriter().write("Database error while loading patient detail: " + HtmlUtil.escape(e.getMessage()));
        }
    }

    private String displayAppointmentAt(Patient patient) {
        if (patient.getAppointmentAt() == null || patient.getAppointmentAt().trim().isEmpty()) {
            return "<span class='muted-text'>Not scheduled</span>";
        }
        return HtmlUtil.shortDateTime(patient.getAppointmentAt());
    }

    private String displayAppointmentNotes(Patient patient) {
        if (patient.getAppointmentNotes() == null || patient.getAppointmentNotes().trim().isEmpty()) {
            return "<span class='muted-text'>No notes added</span>";
        }
        return HtmlUtil.escape(patient.getAppointmentNotes());
    }
}
