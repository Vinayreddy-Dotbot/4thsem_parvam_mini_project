package controllers;

import dao.DoctorDao;
import dao.PatientDao;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.sql.SQLException;
import java.util.Map;

@SuppressWarnings("serial")
public class HomeServlet extends HttpServlet {
    private final PatientDao patientDao = new PatientDao();
    private final DoctorDao doctorDao = new DoctorDao();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setContentType("text/html;charset=UTF-8");

        try {
            String htmlContent = TemplateEngine.render(getServletContext(), "home.html");
            htmlContent = htmlContent.replace("{{TOTAL_PATIENTS}}", String.valueOf(patientDao.getPatientCount()));
            htmlContent = htmlContent.replace("{{TOTAL_DOCTORS}}", String.valueOf(doctorDao.getDoctorCount()));
            htmlContent = htmlContent.replace("{{ACTIVE_ADMISSIONS}}", String.valueOf(patientDao.getActiveAdmissionCount()));
            htmlContent = htmlContent.replace("{{DISCHARGED_COUNT}}", String.valueOf(patientDao.getDischargedCount()));
            htmlContent = htmlContent.replace("{{DEPARTMENT_ROWS}}", buildRows(patientDao.getDepartmentDistribution(), "No department data available."));
            htmlContent = htmlContent.replace("{{WORKLOAD_ROWS}}", buildRows(patientDao.getDoctorWorkload(), "No doctor workload data available."));
            htmlContent = htmlContent.replace("{{STATUS_ROWS}}", buildRows(patientDao.getAdmissionStatusCounts(), "No admission statuses available."));
            response.getWriter().write(htmlContent);
        } catch (SQLException e) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.getWriter().write("Database error while loading dashboard: " + HtmlUtil.escape(e.getMessage()));
            e.printStackTrace();
        }
    }

    private String buildRows(Map<String, Integer> values, String emptyMessage) {
        if (values.isEmpty()) {
            return "<tr><td colspan='2' class='text-center'>" + HtmlUtil.escape(emptyMessage) + "</td></tr>";
        }

        StringBuilder rows = new StringBuilder();
        for (Map.Entry<String, Integer> entry : values.entrySet()) {
            rows.append("<tr>")
                .append("<td>").append(HtmlUtil.escape(formatLabel(entry.getKey()))).append("</td>")
                .append("<td><span class='metric-pill'>").append(entry.getValue()).append("</span></td>")
                .append("</tr>");
        }
        return rows.toString();
    }

    private String formatLabel(String value) {
        if (value == null || value.trim().isEmpty()) {
            return "Unassigned";
        }
        String[] parts = value.replace("_", " ").replace("-", " ").split("\\s+");
        StringBuilder label = new StringBuilder();
        for (String part : parts) {
            if (part.isEmpty()) {
                continue;
            }
            if (label.length() > 0) {
                label.append(" ");
            }
            label.append(Character.toUpperCase(part.charAt(0))).append(part.substring(1).toLowerCase());
        }
        return label.toString();
    }
}
