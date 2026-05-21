package dao;

import config.DbConnection;
import models.Patient;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.sql.Types;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class PatientDao {
    private static final String SELECT_PATIENTS =
        "SELECT p.id, p.name, p.age, p.gender, p.blood, p.phone, p.address, p.admitted_at, " +
        "a.id AS admission_id, a.doctor_id, a.ward, a.status, a.appointment_at, a.appointment_notes, " +
        "d.name AS doctor_name, d.dept AS doctor_dept " +
        "FROM patients p " +
        "LEFT JOIN admissions a ON a.id = (SELECT a2.id FROM admissions a2 WHERE a2.patient_id = p.id ORDER BY a2.id DESC LIMIT 1) " +
        "LEFT JOIN doctors d ON d.id = a.doctor_id ";

    public int addPatient(Patient patient) throws SQLException {
        String patientSql = "INSERT INTO patients (name, age, gender, blood, phone, address) VALUES (?, ?, ?, ?, ?, ?)";
        String admissionSql = "INSERT INTO admissions (patient_id, doctor_id, ward, status, appointment_at, appointment_notes) VALUES (?, ?, ?, ?, ?, ?)";

        try (Connection conn = DbConnection.getConnection()) {
            conn.setAutoCommit(false);
            try (PreparedStatement patientStmt = conn.prepareStatement(patientSql, Statement.RETURN_GENERATED_KEYS);
                 PreparedStatement admissionStmt = conn.prepareStatement(admissionSql)) {
                bindPatientFields(patientStmt, patient);
                patientStmt.executeUpdate();

                int patientId;
                try (ResultSet keys = patientStmt.getGeneratedKeys()) {
                    if (!keys.next()) {
                        throw new SQLException("Patient ID was not generated.");
                    }
                    patientId = keys.getInt(1);
                }

                admissionStmt.setInt(1, patientId);
                admissionStmt.setInt(2, patient.getDoctorId());
                admissionStmt.setString(3, patient.getWard());
                admissionStmt.setString(4, patient.getStatus());
                bindAppointmentFields(admissionStmt, patient, 5);
                admissionStmt.executeUpdate();

                conn.commit();
                return patientId;
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            } finally {
                conn.setAutoCommit(true);
            }
        }
    }

    public Patient getPatientById(int id) throws SQLException {
        String sql = SELECT_PATIENTS + "WHERE p.id = ?";

        try (Connection conn = DbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapPatient(rs);
                }
            }
        }
        return null;
    }

    public List<Patient> getPatients(int limit, int offset, String search) throws SQLException {
        List<Patient> patients = new ArrayList<>();
        SearchPlan plan = buildSearchPlan(search);
        String sql = SELECT_PATIENTS + plan.whereClause + " ORDER BY p.admitted_at DESC LIMIT ? OFFSET ?";

        try (Connection conn = DbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            int index = bindSearchParams(stmt, plan, 1);
            stmt.setInt(index++, limit);
            stmt.setInt(index, offset);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    patients.add(mapPatient(rs));
                }
            }
        }
        return patients;
    }

    public int getPatientCount(String search) throws SQLException {
        SearchPlan plan = buildSearchPlan(search);
        String sql = "SELECT COUNT(*) FROM patients p " +
                     "LEFT JOIN admissions a ON a.id = (SELECT a2.id FROM admissions a2 WHERE a2.patient_id = p.id ORDER BY a2.id DESC LIMIT 1) " +
                     "LEFT JOIN doctors d ON d.id = a.doctor_id " +
                     plan.whereClause;

        try (Connection conn = DbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            bindSearchParams(stmt, plan, 1);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        }
        return 0;
    }

    public int getPatientCount() throws SQLException {
        return getPatientCount("");
    }

    public int getActiveAdmissionCount() throws SQLException {
        String sql = "SELECT COUNT(*) FROM admissions WHERE status <> 'discharged'";
        return getSingleCount(sql);
    }

    public int getDischargedCount() throws SQLException {
        String sql = "SELECT COUNT(*) FROM admissions WHERE status = 'discharged'";
        return getSingleCount(sql);
    }

    public boolean updatePatient(Patient patient) throws SQLException {
        String patientSql = "UPDATE patients SET name = ?, age = ?, gender = ?, blood = ?, phone = ?, address = ? WHERE id = ?";
        String admissionSql = "UPDATE admissions SET doctor_id = ?, ward = ?, status = ?, appointment_at = ?, appointment_notes = ? WHERE id = ?";

        try (Connection conn = DbConnection.getConnection()) {
            conn.setAutoCommit(false);
            try (PreparedStatement patientStmt = conn.prepareStatement(patientSql);
                 PreparedStatement admissionStmt = conn.prepareStatement(admissionSql)) {
                bindPatientFields(patientStmt, patient);
                patientStmt.setInt(7, patient.getId());
                int patientRows = patientStmt.executeUpdate();

                admissionStmt.setInt(1, patient.getDoctorId());
                admissionStmt.setString(2, patient.getWard());
                admissionStmt.setString(3, patient.getStatus());
                bindAppointmentFields(admissionStmt, patient, 4);
                admissionStmt.setInt(6, patient.getAdmissionId());
                int admissionRows = admissionStmt.executeUpdate();

                conn.commit();
                return patientRows > 0 && admissionRows > 0;
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            } finally {
                conn.setAutoCommit(true);
            }
        }
    }

    public boolean dischargePatient(int patientId) throws SQLException {
        String sql = "UPDATE admissions SET status = 'discharged' WHERE patient_id = ? AND status <> 'discharged'";

        try (Connection conn = DbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, patientId);
            return stmt.executeUpdate() > 0;
        }
    }

    public Map<String, Integer> getDepartmentDistribution() throws SQLException {
        Map<String, Integer> distribution = new LinkedHashMap<>();
        String sql = "SELECT d.dept, COUNT(a.id) AS patient_count " +
                     "FROM doctors d " +
                     "LEFT JOIN admissions a ON a.doctor_id = d.id " +
                     "GROUP BY d.dept " +
                     "ORDER BY d.dept ASC";

        try (Connection conn = DbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                distribution.put(rs.getString("dept"), rs.getInt("patient_count"));
            }
        }
        return distribution;
    }

    public Map<String, Integer> getDoctorWorkload() throws SQLException {
        Map<String, Integer> workload = new LinkedHashMap<>();
        String sql = "SELECT d.name, d.dept, COUNT(CASE WHEN a.status <> 'discharged' THEN 1 END) AS active_count " +
                     "FROM doctors d " +
                     "LEFT JOIN admissions a ON a.doctor_id = d.id " +
                     "GROUP BY d.id, d.name, d.dept " +
                     "ORDER BY active_count DESC, d.dept ASC, d.name ASC";

        try (Connection conn = DbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                workload.put(rs.getString("name") + " (" + rs.getString("dept") + ")", rs.getInt("active_count"));
            }
        }
        return workload;
    }

    public Map<String, Integer> getAdmissionStatusCounts() throws SQLException {
        Map<String, Integer> statuses = new LinkedHashMap<>();
        String sql = "SELECT status, COUNT(*) AS status_count FROM admissions GROUP BY status ORDER BY status ASC";

        try (Connection conn = DbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                statuses.put(rs.getString("status"), rs.getInt("status_count"));
            }
        }
        return statuses;
    }

    private int getSingleCount(String sql) throws SQLException {
        try (Connection conn = DbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            if (rs.next()) {
                return rs.getInt(1);
            }
        }
        return 0;
    }

    private void bindPatientFields(PreparedStatement stmt, Patient patient) throws SQLException {
        stmt.setString(1, patient.getName());
        stmt.setInt(2, patient.getAge());
        stmt.setString(3, patient.getGender());
        stmt.setString(4, patient.getBlood());
        stmt.setString(5, patient.getPhone());
        stmt.setString(6, patient.getAddress());
    }

    private void bindAppointmentFields(PreparedStatement stmt, Patient patient, int startIndex) throws SQLException {
        String appointmentAt = patient.getAppointmentAt();
        if (appointmentAt == null || appointmentAt.trim().isEmpty()) {
            stmt.setNull(startIndex, Types.TIMESTAMP);
        } else {
            stmt.setTimestamp(startIndex, parseAppointmentTimestamp(appointmentAt));
        }
        stmt.setString(startIndex + 1, patient.getAppointmentNotes() == null ? "" : patient.getAppointmentNotes());
    }

    private Timestamp parseAppointmentTimestamp(String value) throws SQLException {
        String normalized = value.trim().replace("T", " ");
        if (normalized.length() == 16) {
            normalized += ":00";
        }
        try {
            return Timestamp.valueOf(normalized);
        } catch (IllegalArgumentException e) {
            throw new SQLException("Appointment date and time must be valid.", e);
        }
    }

    private Patient mapPatient(ResultSet rs) throws SQLException {
        Timestamp admittedAt = rs.getTimestamp("admitted_at");
        Timestamp appointmentAt = rs.getTimestamp("appointment_at");
        return new Patient(
            rs.getInt("id"),
            rs.getString("name"),
            rs.getInt("age"),
            rs.getString("gender"),
            rs.getString("blood"),
            rs.getString("phone"),
            rs.getString("address"),
            admittedAt == null ? "" : admittedAt.toString(),
            rs.getInt("admission_id"),
            rs.getInt("doctor_id"),
            rs.getString("doctor_name"),
            rs.getString("doctor_dept"),
            rs.getString("ward"),
            rs.getString("status"),
            appointmentAt == null ? "" : appointmentAt.toString(),
            rs.getString("appointment_notes")
        );
    }

    private SearchPlan buildSearchPlan(String search) {
        SearchPlan plan = new SearchPlan();
        if (search == null || search.trim().isEmpty()) {
            return plan;
        }

        String raw = search.trim();
        String normalized = raw.toLowerCase(Locale.ROOT);
        List<String> clauses = new ArrayList<>();
        List<Object> params = new ArrayList<>();

        String digits = raw.replaceAll("[^0-9]", "");
        if (!digits.isEmpty() && raw.matches(".*\\d+.*")) {
            try {
                clauses.add("p.id = ?");
                params.add(Integer.parseInt(digits));
            } catch (NumberFormatException ignored) {
                // Keep the search usable even if an unusually large number is typed.
            }
        }

        if (normalized.contains("discharged")) {
            clauses.add("a.status = ?");
            params.add("discharged");
        } else if (normalized.contains("observation") || normalized.contains("monitor")) {
            clauses.add("a.status = ?");
            params.add("under observation");
        } else if (normalized.contains("admitted")) {
            clauses.add("a.status <> ?");
            params.add("discharged");
        }

        if (normalized.contains("emergency") || normalized.contains("critical") || normalized.contains("priority")) {
            clauses.add("(a.status = ? OR p.age >= ?)");
            params.add("emergency");
            params.add(65);
        }

        String[] departments = {
            "cardiology", "general medicine", "orthopedics", "neurology",
            "pediatrics", "emergency", "dermatology"
        };
        for (String dept : departments) {
            if (normalized.contains(dept)) {
                clauses.add("LOWER(d.dept) = ?");
                params.add(dept);
                break;
            }
        }

        if (normalized.contains("female") || normalized.contains("women")) {
            clauses.add("LOWER(p.gender) = ?");
            params.add("female");
        } else if (normalized.contains("male") || normalized.contains("men")) {
            clauses.add("LOWER(p.gender) = ?");
            params.add("male");
        }

        if (normalized.contains("child") || normalized.contains("children") || normalized.contains("kids")) {
            clauses.add("p.age <= ?");
            params.add(12);
        } else if (normalized.contains("senior") || normalized.contains("elderly") || normalized.contains("older")) {
            clauses.add("p.age >= ?");
            params.add(65);
        }

        String bloodMatch = findBloodGroup(normalized);
        if (bloodMatch != null) {
            clauses.add("LOWER(p.blood) = ?");
            params.add(bloodMatch.toLowerCase(Locale.ROOT));
        }

        if (normalized.contains("appointment") || normalized.contains("scheduled") || normalized.contains("follow up")
                || normalized.contains("follow-up")) {
            clauses.add("(a.appointment_at IS NOT NULL OR a.appointment_notes <> '')");
        }

        if (clauses.isEmpty()) {
            String like = "%" + raw + "%";
            plan.whereClause = "WHERE p.name LIKE ? OR CAST(p.id AS CHAR) LIKE ? OR p.phone LIKE ? " +
                               "OR p.blood LIKE ? OR d.name LIKE ? OR d.dept LIKE ? OR a.ward LIKE ? OR a.status LIKE ? " +
                               "OR a.appointment_notes LIKE ? OR CAST(a.appointment_at AS CHAR) LIKE ? ";
            for (int i = 0; i < 10; i++) {
                plan.params.add(like);
            }
            return plan;
        }

        plan.whereClause = "WHERE " + String.join(" AND ", clauses) + " ";
        plan.params.addAll(params);
        return plan;
    }

    private int bindSearchParams(PreparedStatement stmt, SearchPlan plan, int startIndex) throws SQLException {
        int index = startIndex;
        for (Object param : plan.params) {
            if (param instanceof Integer) {
                stmt.setInt(index++, (Integer) param);
            } else {
                stmt.setString(index++, String.valueOf(param));
            }
        }
        return index;
    }

    private String findBloodGroup(String normalized) {
        String compact = normalized.replace("blood group", "")
                                   .replace("blood", "")
                                   .replace(" ", "")
                                   .replace("positive", "+")
                                   .replace("negative", "-");
        String[] bloodGroups = {"ab+", "ab-", "a+", "a-", "b+", "b-", "o+", "o-"};
        for (String group : bloodGroups) {
            if (compact.contains(group)) {
                return group.toUpperCase(Locale.ROOT);
            }
        }
        return null;
    }

    private static class SearchPlan {
        private String whereClause = "";
        private final List<Object> params = new ArrayList<>();
    }
}
