//package com.example.final_project;
//
//public class AttendanceModel {
//    private String teacherName;
//    private String subjectName;
//    private String status;
//
//    public AttendanceModel() {
//        // Default constructor required for Firestore
//    }
//
//    public AttendanceModel(String teacherName, String subjectName, String status) {
//        this.teacherName = teacherName;
//        this.subjectName = subjectName;
//        this.status = status;
//    }
//
//    // Getters and setters
//    public String getTeacherName() {
//        return teacherName;
//    }
//
//    public void setTeacherName(String teacherName) {
//        this.teacherName = teacherName;
//    }
//
//    public String getSubjectName() {
//        return subjectName;
//    }
//
//    public void setSubjectName(String subjectName) {
//        this.subjectName = subjectName;
//    }
//
//    public String getStatus() {
//        return status;
//    }
//
//    public void setStatus(String status) {
//        this.status = status;
//    }
//}

package com.example.final_project;

import java.util.List;

public class StudentAttendanceModel {
    private String teacherName;
    private String subjectName;
    private List<String> present;

    public StudentAttendanceModel() {
        // Default constructor required for calls to DataSnapshot.getValue(StudentAttendanceModel.class)
    }

    public StudentAttendanceModel(String teacherName, String subjectName, List<String> present) {
        this.teacherName = teacherName;
        this.subjectName = subjectName;
        this.present = present;
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

    public List<String> getPresent() {
        return present;
    }

    public void setPresent(List<String> present) {
        this.present = present;
    }
}
