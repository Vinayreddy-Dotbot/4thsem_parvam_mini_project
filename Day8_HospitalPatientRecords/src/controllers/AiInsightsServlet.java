package controllers;

import dao.PatientDao;
import models.Patient;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.sql.SQLException;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@SuppressWarnings("serial")
public class AiInsightsServlet extends HttpServlet {
    private final PatientDao patientDao = new PatientDao();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setContentType("text/html;charset=UTF-8");

        try {
            List<Patient> patients = patientDao.getPatients(500, 0, "");
            Collections.sort(patients, new Comparator<Patient>() {
                @Override
                public int compare(Patient first, Patient second) {
                    return Integer.compare(
                        HospitalAiService.calculateRiskScore(second),
                        HospitalAiService.calculateRiskScore(first)
                    );
                }
            });

            AiStats stats = calculateStats(patients);
            String htmlContent = TemplateEngine.render(getServletContext(), "ai_insights.html");
            htmlContent = htmlContent.replace("{{TOTAL_PATIENTS}}", String.valueOf(patients.size()));
            htmlContent = htmlContent.replace("{{HIGH_PRIORITY_COUNT}}", String.valueOf(stats.highPriorityCount));
            htmlContent = htmlContent.replace("{{MONITORING_COUNT}}", String.valueOf(stats.monitoringCount));
            htmlContent = htmlContent.replace("{{AVG_RISK_SCORE}}", String.valueOf(stats.averageRiskScore));
            htmlContent = htmlContent.replace("{{ACTIVE_ADMISSIONS}}", String.valueOf(stats.activeAdmissions));
            htmlContent = htmlContent.replace("{{AI_ANALYTICS_SUMMARY}}", buildAnalyticsSummary(patients));
            htmlContent = htmlContent.replace("{{EMERGENCY_ROWS}}", buildEmergencyRows(patients));
            htmlContent = htmlContent.replace("{{AI_PATIENT_ROWS}}", buildPatientRows(patients));
            response.getWriter().write(htmlContent);
        } catch (SQLException e) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.getWriter().write("Database error while loading AI insights: " + HtmlUtil.escape(e.getMessage()));
            e.printStackTrace();
        }
    }

    private AiStats calculateStats(List<Patient> patients) {
        AiStats stats = new AiStats();
        int totalRisk = 0;

        for (Patient patient : patients) {
            int score = HospitalAiService.calculateRiskScore(patient);
            totalRisk += score;

            if (!patient.isDischarged()) {
                stats.activeAdmissions++;
                if (score >= 55) {
                    stats.highPriorityCount++;
                } else if (score >= 35) {
                    stats.monitoringCount++;
                }
            }
        }

        stats.averageRiskScore = patients.isEmpty() ? 0 : Math.round((float) totalRisk / patients.size());
        return stats;
    }

    private String buildEmergencyRows(List<Patient> patients) {
        StringBuilder rows = new StringBuilder();
        int shown = 0;

        for (Patient patient : patients) {
            int score = HospitalAiService.calculateRiskScore(patient);
            if (patient.isDischarged() || score < 55) {
                continue;
            }

            String riskClass = HtmlUtil.escape(HospitalAiService.getRiskClass(patient));
            rows.append("<article class='ai-emergency-card risk-").append(riskClass).append("'>")
                .append("<div class='ai-risk-rail'>")
                .append("<span>Risk</span><strong>").append(score).append("</strong><small>/100</small>")
                .append("</div>")
                .append("<div class='ai-emergency-main'>")
                .append("<div class='ai-insight-title-row'>")
                .append(patientLink(patient))
                .append("<span class='status-chip ").append(riskClass).append("'>")
                .append(HtmlUtil.escape(HospitalAiService.getPriorityLabel(patient))).append("</span>")
                .append("</div>")
                .append("<div class='ai-reason-grid'>")
                .append("<div class='ai-reason-tile'><i class='fa-solid fa-magnifying-glass-chart'></i><span>AI Reason</span><p>")
                .append(HtmlUtil.escape(HospitalAiService.getRiskReasons(patient))).append("</p></div>")
                .append("<div class='ai-reason-tile'><i class='fa-solid fa-bell'></i><span>Priority Action</span><p>")
                .append(HtmlUtil.escape(HospitalAiService.getEmergencyAction(patient))).append("</p></div>")
                .append("</div>")
                .append("</div>")
                .append("<div class='ai-card-action-column'><a class='btn btn-secondary btn-mini' href='detail?id=")
                .append(patient.getId()).append("'><i class='fa-solid fa-eye'></i> View</a></div>")
                .append("</article>");
            shown++;
            if (shown >= 8) {
                break;
            }
        }

        if (shown == 0) {
            return "<div class='empty-record-card text-center'>No high-priority emergency records found.</div>";
        }
        return rows.toString();
    }

    private String buildPatientRows(List<Patient> patients) {
        if (patients.isEmpty()) {
            return "<div class='empty-record-card text-center'>No patient records available for AI analysis.</div>";
        }

        StringBuilder rows = new StringBuilder();
        int shown = 0;
        for (Patient patient : patients) {
            int score = HospitalAiService.calculateRiskScore(patient);
            String riskClass = HtmlUtil.escape(HospitalAiService.getRiskClass(patient));
            rows.append("<article class='ai-patient-analysis-card risk-").append(riskClass).append("'>")
                .append("<div class='ai-patient-card-top'>")
                .append(patientLink(patient))
                .append("<div class='ai-score-badge ").append(riskClass).append("'><span>Score</span><strong>")
                .append(score).append("</strong><small>/100</small></div>")
                .append("</div>")
                .append("<div class='ai-patient-card-meta'>")
                .append("<span class='status-chip ").append(riskClass).append("'>")
                .append(HtmlUtil.escape(HospitalAiService.getPriorityLabel(patient))).append("</span>")
                .append("<span><i class='fa-solid fa-user-doctor'></i> ")
                .append(HtmlUtil.escape(blank(patient.getDoctorDept()) ? "Unassigned" : patient.getDoctorDept()))
                .append("</span>")
                .append("</div>")
                .append("<div class='ai-card-copy-grid'>")
                .append("<div><span><i class='fa-solid fa-notes-medical'></i> AI Health Summary</span><p>")
                .append(HtmlUtil.escape(truncate(HospitalAiService.getHealthSummary(patient), 190))).append("</p></div>")
                .append("<div><span><i class='fa-solid fa-user-doctor'></i> Doctor Recommendation</span><p>")
                .append(HtmlUtil.escape(truncate(HospitalAiService.getDoctorRecommendation(patient), 180))).append("</p></div>")
                .append("</div>")
                .append("<a class='btn btn-primary btn-mini ai-detail-button' href='detail?id=")
                .append(patient.getId()).append("'><i class='fa-solid fa-notes-medical'></i> Detail</a>")
                .append("</article>");
            shown++;
            if (shown >= 50) {
                break;
            }
        }
        return rows.toString();
    }

    private String buildAnalyticsSummary(List<Patient> patients) {
        if (patients.isEmpty()) {
            return "AI analytics will appear after patient records are added.";
        }

        Map<String, DepartmentRisk> risks = new LinkedHashMap<>();
        int active = 0;
        int discharged = 0;

        for (Patient patient : patients) {
            if (patient.isDischarged()) {
                discharged++;
            } else {
                active++;
            }

            String department = patient.getDoctorDept();
            if (department == null || department.trim().isEmpty()) {
                department = "Unassigned";
            }
            DepartmentRisk risk = risks.get(department);
            if (risk == null) {
                risk = new DepartmentRisk();
                risks.put(department, risk);
            }
            risk.totalScore += HospitalAiService.calculateRiskScore(patient);
            risk.count++;
        }

        String topDepartment = "Unassigned";
        int topAverage = 0;
        for (Map.Entry<String, DepartmentRisk> entry : risks.entrySet()) {
            int average = entry.getValue().count == 0 ? 0 : entry.getValue().totalScore / entry.getValue().count;
            if (average > topAverage) {
                topAverage = average;
                topDepartment = entry.getKey();
            }
        }

        return "AI analyzed " + patients.size() + " records. Active admissions: " + active
            + ", discharged: " + discharged + ". Highest average risk is currently in "
            + topDepartment + " at " + topAverage + "/100.";
    }

    private String patientLink(Patient patient) {
        return "<div class='ai-patient-identity'>"
            + "<div class='ai-patient-avatar'><i class='fa-solid fa-hospital-user'></i></div>"
            + "<div><a class='record-link' href='detail?id=" + patient.getId() + "'>"
            + HtmlUtil.escape(patient.getName()) + "</a>"
            + "<span class='dob-subtitle'>ID #" + patient.getId() + " - "
            + HtmlUtil.escape(patient.getStatusDisplay()) + "</span></div>"
            + "</div>";
    }

    private String truncate(String value, int maxLength) {
        if (value == null || value.length() <= maxLength) {
            return value == null ? "" : value;
        }
        return value.substring(0, maxLength - 3) + "...";
    }

    private boolean blank(String value) {
        return value == null || value.trim().isEmpty();
    }

    private static class AiStats {
        private int highPriorityCount;
        private int monitoringCount;
        private int averageRiskScore;
        private int activeAdmissions;
    }

    private static class DepartmentRisk {
        private int totalScore;
        private int count;
    }
}
