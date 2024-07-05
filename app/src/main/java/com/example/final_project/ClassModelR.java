package com.example.final_project;

public class ClassModelR {
    private String className;
    private String subjectName;
    private boolean attendanceDone;
    private String id; // Document ID field
    private String teacherName;

    public ClassModelR() {
        // Default constructor required for calls to DataSnapshot.getValue(ClassModelR.class)
    }

    public ClassModelR(String className, String subjectName, String teacherName) {
        this.className = className;
        this.subjectName = subjectName;
        this.teacherName = teacherName;
        this.attendanceDone = false; // Default to false initially
    }

    // Getters and Setters
    public String getClassName() {
        return className;
    }

    public void setClassName(String className) {
        this.className = className;
    }

    public String getSubjectName() {
        return subjectName;
    }

    public void setSubjectName(String subjectName) {
        this.subjectName = subjectName;
    }

    public boolean isAttendanceDone() {
        return attendanceDone;
    }

    public void setAttendanceDone(boolean attendanceDone) {
        this.attendanceDone = attendanceDone;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTeacherName() {
        return teacherName;
    }

    public void setTeacherName(String teacherName) {
        this.teacherName = teacherName;
    }
}
