package models;

public class Patient {
    private int id;
    private String name;
    private int age;
    private String gender;
    private String blood;
    private String phone;
    private String address;
    private String admittedAt;
    private int admissionId;
    private int doctorId;
    private String doctorName;
    private String doctorDept;
    private String ward;
    private String status;
    private String appointmentAt;
    private String appointmentNotes;

    public Patient() {
    }

    public Patient(int id, String name, int age, String gender, String blood, String phone,
                   String address, String admittedAt, int admissionId, int doctorId,
                   String doctorName, String doctorDept, String ward, String status,
                   String appointmentAt, String appointmentNotes) {
        this.id = id;
        this.name = name;
        this.age = age;
        this.gender = gender;
        this.blood = blood;
        this.phone = phone;
        this.address = address;
        this.admittedAt = admittedAt;
        this.admissionId = admissionId;
        this.doctorId = doctorId;
        this.doctorName = doctorName;
        this.doctorDept = doctorDept;
        this.ward = ward;
        this.status = status;
        this.appointmentAt = appointmentAt;
        this.appointmentNotes = appointmentNotes;
    }

    public boolean isDischarged() {
        return "discharged".equalsIgnoreCase(status);
    }

    public String getStatusDisplay() {
        if (status == null || status.trim().isEmpty()) {
            return "Not assigned";
        }
        String[] parts = status.split("[ _-]+");
        StringBuilder display = new StringBuilder();
        for (String part : parts) {
            if (part.isEmpty()) {
                continue;
            }
            if (display.length() > 0) {
                display.append(" ");
            }
            display.append(Character.toUpperCase(part.charAt(0))).append(part.substring(1).toLowerCase());
        }
        return display.toString();
    }

    public String getPriorityLabel() {
        if ("emergency".equalsIgnoreCase(status) || age >= 65) {
            return "High Priority";
        }
        if ("under observation".equalsIgnoreCase(status) || age <= 12) {
            return "Needs Monitoring";
        }
        if (isDischarged()) {
            return "Closed";
        }
        return "Stable";
    }

    public String getPriorityClass() {
        String label = getPriorityLabel();
        if ("High Priority".equals(label)) {
            return "danger";
        }
        if ("Needs Monitoring".equals(label)) {
            return "warning";
        }
        if ("Closed".equals(label)) {
            return "muted";
        }
        return "success";
    }

    public String getRiskLevel() {
        if ("emergency".equalsIgnoreCase(status) || age >= 65) {
            return "High";
        }
        if ("under observation".equalsIgnoreCase(status) || age <= 12) {
            return "Medium";
        }
        return "Low";
    }

    public String getAiHealthSummary() {
        StringBuilder summary = new StringBuilder();
        summary.append(name).append(" is a ").append(age).append("-year-old ").append(gender);
        summary.append(" patient with blood group ").append(blood).append(". ");
        summary.append("Current admission is assigned to ");
        summary.append(doctorName == null ? "an unassigned doctor" : doctorName);
        if (doctorDept != null && !doctorDept.trim().isEmpty()) {
            summary.append(" from ").append(doctorDept);
        }
        summary.append(", ward ").append(ward == null ? "N/A" : ward);
        summary.append(", with status ").append(getStatusDisplay());
        if (appointmentAt != null && !appointmentAt.trim().isEmpty()) {
            summary.append(". Next appointment is scheduled for ").append(appointmentAt);
        }
        summary.append(".");
        return summary.toString();
    }

    public String getDoctorRecommendation() {
        if (doctorDept == null || doctorDept.trim().isEmpty()) {
            return "Assign a specialist department after initial triage.";
        }
        if ("emergency".equalsIgnoreCase(status)) {
            return "Continue emergency supervision and keep the assigned " + doctorDept + " doctor on priority.";
        }
        return "The selected " + doctorDept + " department matches the current admission. Review again if symptoms change.";
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getAge() {
        return age;
    }

    public void setAge(int age) {
        this.age = age;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public String getBlood() {
        return blood;
    }

    public void setBlood(String blood) {
        this.blood = blood;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getAdmittedAt() {
        return admittedAt;
    }

    public void setAdmittedAt(String admittedAt) {
        this.admittedAt = admittedAt;
    }

    public int getAdmissionId() {
        return admissionId;
    }

    public void setAdmissionId(int admissionId) {
        this.admissionId = admissionId;
    }

    public int getDoctorId() {
        return doctorId;
    }

    public void setDoctorId(int doctorId) {
        this.doctorId = doctorId;
    }

    public String getDoctorName() {
        return doctorName;
    }

    public void setDoctorName(String doctorName) {
        this.doctorName = doctorName;
    }

    public String getDoctorDept() {
        return doctorDept;
    }

    public void setDoctorDept(String doctorDept) {
        this.doctorDept = doctorDept;
    }

    public String getWard() {
        return ward;
    }

    public void setWard(String ward) {
        this.ward = ward;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getAppointmentAt() {
        return appointmentAt;
    }

    public void setAppointmentAt(String appointmentAt) {
        this.appointmentAt = appointmentAt;
    }

    public String getAppointmentNotes() {
        return appointmentNotes;
    }

    public void setAppointmentNotes(String appointmentNotes) {
        this.appointmentNotes = appointmentNotes;
    }
}
