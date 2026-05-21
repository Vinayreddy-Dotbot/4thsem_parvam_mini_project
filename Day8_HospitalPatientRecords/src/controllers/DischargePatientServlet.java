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
public class DischargePatientServlet extends HttpServlet {
    private final PatientDao patientDao = new PatientDao();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setContentType("text/html;charset=UTF-8");

        try {
            int id = Integer.parseInt(request.getParameter("id"));
            Patient patient = patientDao.getPatientById(id);
            if (patient == null || patient.isDischarged()) {
                response.sendRedirect("list");
                return;
            }

            String htmlContent = TemplateEngine.render(getServletContext(), "discharge_confirm.html");
            htmlContent = htmlContent.replace("{{PATIENT_ID}}", String.valueOf(patient.getId()));
            htmlContent = htmlContent.replace("{{PATIENT_NAME}}", HtmlUtil.escape(patient.getName()));
            htmlContent = htmlContent.replace("{{DOCTOR_NAME}}", HtmlUtil.escape(patient.getDoctorName()));
            htmlContent = htmlContent.replace("{{WARD}}", HtmlUtil.escape(patient.getWard()));
            response.getWriter().write(htmlContent);
        } catch (Exception e) {
            response.sendRedirect("list");
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        try {
            int id = Integer.parseInt(request.getParameter("id"));
            patientDao.dischargePatient(id);
            response.sendRedirect("detail?id=" + id);
        } catch (NumberFormatException e) {
            response.sendRedirect("list");
        } catch (SQLException e) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.getWriter().write("Database error while discharging patient: " + HtmlUtil.escape(e.getMessage()));
        }
    }
}
