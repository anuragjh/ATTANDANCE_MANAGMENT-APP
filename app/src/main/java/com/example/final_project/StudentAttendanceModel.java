package com.example.final_project;

import java.util.List;

public class StudentAttendanceModel {
    private String teacherName;
    private String subjectName;
    private List<String> present;

    public StudentAttendanceModel() {
        // Default constructor required for calls to DataSnapshot.getValue(User.class)
    }

    public StudentAttendanceModel(String teacherName, String subjectName, List<String> present) {
        this.teacherName = teacherName;
        this.subjectName = subjectName;
        this.present = present;
    }

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

    public List<String> getPresent() {
        return present;
    }

    public void setPresent(List<String> present) {
        this.present = present;
    }
}
