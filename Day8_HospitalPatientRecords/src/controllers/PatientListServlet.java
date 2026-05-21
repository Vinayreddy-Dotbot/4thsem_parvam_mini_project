package controllers;

import dao.PatientDao;
import models.Patient;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URLEncoder;
import java.sql.SQLException;
import java.util.List;

@SuppressWarnings("serial")
public class PatientListServlet extends HttpServlet {
    private final PatientDao patientDao = new PatientDao();
    private static final int RECORDS_PER_PAGE = 5;

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setContentType("text/html;charset=UTF-8");

        String search = value(request.getParameter("search"));
        int currentPage = parsePage(request.getParameter("page"));
        int limit = RECORDS_PER_PAGE;
        int offset = (currentPage - 1) * limit;

        try {
            List<Patient> patients = patientDao.getPatients(limit, offset, search);
            int totalRecords = patientDao.getPatientCount(search);
            int totalPages = (int) Math.ceil((double) totalRecords / limit);

            String htmlContent = TemplateEngine.render(getServletContext(), "list.html");
            htmlContent = htmlContent.replace("{{SEARCH_QUERY}}", HtmlUtil.escape(search));
            htmlContent = htmlContent.replace("{{RESET_BUTTON}}", search.isEmpty() ? "" :
                "<a href='list' class='btn-reset' title='Reset search'><i class='fa-solid fa-rotate-left'></i></a>");
            htmlContent = htmlContent.replace("{{PATIENT_ROWS}}", buildRows(patients));
            htmlContent = htmlContent.replace("{{PAGINATION_INFO}}", buildPageInfo(totalRecords, offset, limit));
            htmlContent = htmlContent.replace("{{PAGINATION_CONTROLS}}", buildPagination(currentPage, totalPages, search));
            response.getWriter().write(htmlContent);
        } catch (SQLException e) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.getWriter().write("Database error while reading patients: " + HtmlUtil.escape(e.getMessage()));
            e.printStackTrace();
        }
    }

    private String buildRows(List<Patient> patients) {
        if (patients.isEmpty()) {
            return "<div class='empty-record-card text-center'>No patient records found.</div>";
        }

        StringBuilder rows = new StringBuilder();
        for (Patient patient : patients) {
            String priorityClass = HtmlUtil.escape(HospitalAiService.getRiskClass(patient));
            rows.append("<article class='patient-record-card priority-").append(priorityClass).append("'>")
                .append("<div class='patient-id-rail'>")
                .append("<span>ID</span><strong>#").append(patient.getId()).append("</strong>")
                .append("</div>")
                .append("<div class='patient-record-main'>")
                .append("<div class='patient-record-heading'>")
                .append("<div class='patient-record-avatar'><i class='fa-solid fa-hospital-user'></i></div>")
                .append("<div>")
                .append("<a class='record-link patient-record-name' href='detail?id=").append(patient.getId()).append("'>")
                .append(HtmlUtil.escape(patient.getName())).append("</a>")
                .append("<span class='dob-subtitle'>")
                .append(HtmlUtil.escape(patient.getAge() + " yrs, " + patient.getGender()))
                .append("</span>")
                .append("</div>")
                .append("<span class='blood-badge patient-blood-badge'><i class='fa-solid fa-droplet'></i> ")
                .append(HtmlUtil.escape(patient.getBlood())).append("</span>")
                .append("</div>")
                .append("<div class='patient-info-grid'>")
                .append(infoTile("fa-phone", "Phone", HtmlUtil.escape(patient.getPhone())))
                .append(infoTile("fa-user-doctor", "Doctor", doctorLabel(patient)))
                .append(infoTile("fa-bed", "Ward / Status", wardStatusLabel(patient)))
                .append(infoTile("fa-calendar-check", "Appointment", appointmentLabel(patient)))
                .append("</div>")
                .append("</div>")
                .append("<aside class='patient-priority-panel'>")
                .append("<span class='status-chip ").append(priorityClass).append("'>")
                .append(HtmlUtil.escape(HospitalAiService.getPriorityLabel(patient))).append("</span>")
                .append("<span class='patient-risk-meta'><i class='fa-solid fa-brain'></i> AI priority scan</span>")
                .append("<div class='table-actions patient-card-actions'>")
                .append("<a href='detail?id=").append(patient.getId()).append("' class='btn btn-secondary btn-mini'><i class='fa-solid fa-eye'></i> View</a>")
                .append("<a href='edit?id=").append(patient.getId()).append("' class='btn btn-secondary btn-mini'><i class='fa-solid fa-pen'></i> Edit</a>");
            if (!patient.isDischarged()) {
                rows.append("<a href='discharge?id=").append(patient.getId()).append("' class='btn btn-danger btn-mini'><i class='fa-solid fa-door-open'></i> Discharge</a>");
            }
            rows.append("</div>")
                .append("</aside>")
                .append("</article>");
        }
        return rows.toString();
    }

    private String infoTile(String icon, String label, String value) {
        return "<div class='patient-info-tile'>"
            + "<i class='fa-solid " + icon + "'></i>"
            + "<div><span>" + HtmlUtil.escape(label) + "</span><strong>" + value + "</strong></div>"
            + "</div>";
    }

    private String doctorLabel(Patient patient) {
        String doctorName = HtmlUtil.escape(patient.getDoctorName());
        String doctorDept = HtmlUtil.escape(patient.getDoctorDept());
        if (doctorName.isEmpty() && doctorDept.isEmpty()) {
            return "<span class='muted-text'>Unassigned</span>";
        }
        return doctorName + "<small>" + doctorDept + "</small>";
    }

    private String wardStatusLabel(Patient patient) {
        return HtmlUtil.escape(patient.getWard())
            + "<small><span class='status-chip " + HtmlUtil.escape(HospitalAiService.getRiskClass(patient)) + "'>"
            + HtmlUtil.escape(patient.getStatusDisplay())
            + "</span></small>";
    }

    private String appointmentLabel(Patient patient) {
        StringBuilder appointment = new StringBuilder();
        if (patient.getAppointmentAt() == null || patient.getAppointmentAt().trim().isEmpty()) {
            appointment.append("<span class='muted-text'>Not scheduled</span>");
        } else {
            appointment.append(HtmlUtil.shortDateTime(patient.getAppointmentAt()));
        }

        if (patient.getAppointmentNotes() != null && !patient.getAppointmentNotes().trim().isEmpty()) {
            appointment.append("<small>").append(HtmlUtil.escape(patient.getAppointmentNotes())).append("</small>");
        }
        return appointment.toString();
    }

    private String formatAppointment(Patient patient) {
        StringBuilder appointment = new StringBuilder();
        if (patient.getAppointmentAt() == null || patient.getAppointmentAt().trim().isEmpty()) {
            appointment.append("<span class='muted-text'>Not scheduled</span>");
        } else {
            appointment.append(HtmlUtil.shortDateTime(patient.getAppointmentAt()));
        }

        if (patient.getAppointmentNotes() != null && !patient.getAppointmentNotes().trim().isEmpty()) {
            appointment.append("<span class='dob-subtitle'>")
                .append(HtmlUtil.escape(patient.getAppointmentNotes()))
                .append("</span>");
        }
        return appointment.toString();
    }

    private String buildPageInfo(int totalRecords, int offset, int limit) {
        if (totalRecords == 0) {
            return "Showing 0 records of 0 patients.";
        }
        int startRange = offset + 1;
        int endRange = Math.min(offset + limit, totalRecords);
        return "Showing <strong>" + startRange + "</strong> to <strong>" + endRange
            + "</strong> of <strong>" + totalRecords + "</strong> patients.";
    }

    private String buildPagination(int currentPage, int totalPages, String search) throws IOException {
        if (totalPages <= 1) {
            return "";
        }

        String encodedSearch = URLEncoder.encode(search, "UTF-8");
        StringBuilder controls = new StringBuilder();
        int prevPage = Math.max(1, currentPage - 1);
        int nextPage = Math.min(totalPages, currentPage + 1);

        controls.append("<a href='list?page=").append(prevPage).append("&search=").append(encodedSearch)
            .append("' class='page-link ").append(currentPage == 1 ? "disabled" : "").append("'>Prev</a>");
        for (int i = 1; i <= totalPages; i++) {
            controls.append("<a href='list?page=").append(i).append("&search=").append(encodedSearch)
                .append("' class='page-link ").append(i == currentPage ? "active" : "").append("'>")
                .append(i).append("</a>");
        }
        controls.append("<a href='list?page=").append(nextPage).append("&search=").append(encodedSearch)
            .append("' class='page-link ").append(currentPage == totalPages ? "disabled" : "").append("'>Next</a>");
        return controls.toString();
    }

    private int parsePage(String pageParam) {
        try {
            int page = Integer.parseInt(value(pageParam));
            return Math.max(1, page);
        } catch (NumberFormatException e) {
            return 1;
        }
    }

    private String value(String raw) {
        return raw == null ? "" : raw.trim();
    }
}
