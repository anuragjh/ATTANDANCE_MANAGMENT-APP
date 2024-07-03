package com.example.final_project;
public class ClassModel {
    private String className;
    private String subjectName;
    private boolean ongoing;

    public ClassModel() {
        // Default constructor required for calls to DataSnapshot.getValue(ClassModel.class)
    }

    public ClassModel(String className, String subjectName) {
        this.className = className;
        this.subjectName = subjectName;
        this.ongoing = true; // Set default to true
    }

    public ClassModel(String className, String subjectName, boolean ongoing) {
        this.className = className;
        this.subjectName = subjectName;
        this.ongoing = ongoing;
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

    public boolean isOngoing() {
        return ongoing;
    }

    public void setOngoing(boolean ongoing) {
        this.ongoing = ongoing;
    }
}
