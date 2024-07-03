package com.example.final_project;

public class AttendanceModel {
    private String teacherName;
    private String subjectName;
    private String status;

    public AttendanceModel() {
        // Default constructor required for Firestore
    }

    public AttendanceModel(String teacherName, String subjectName, String status) {
        this.teacherName = teacherName;
        this.subjectName = subjectName;
        this.status = status;
    }

    // Getters and setters
    public String getTeacherName() {
        return teacherName;
    }

    public void setTeacherName(String teacherName) {
        this.teacherName = teacherName;
    }

    public String getSubjectName() {
        return subjectName;
    }

    public void setSubjectName(String subjectName) {
        this.subjectName = subjectName;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
