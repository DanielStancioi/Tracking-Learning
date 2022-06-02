package com.example.tlfsvf;

import java.util.ArrayList;
import java.util.List;

public class LabModel {
    private String lab, description, id, date, location, instructor, endDate, minGrade;
    private List<String> marks ;
    private List<String> marksMax ;
    private List<String> mMarksPercent;
    private List<String> gradeDate;
    public LabModel(){

    }

    public LabModel(String lab, String description, String id, String date, String location, String instructor, String endDate, String minGrade, List<String> marks, List<String> marksMax, List<String> mMarksPercent, List<String> gradeDate) {
        this.lab = lab;
        this.description = description;
        this.id = id;
        this.date = date;
        this.location = location;
        this.instructor = instructor;
        this.endDate = endDate;
        this.minGrade = minGrade;
        this.marks = marks;
        this.marksMax = marksMax;
        this.mMarksPercent = mMarksPercent;
        this.gradeDate = gradeDate;
    }


    public String getLab() {
        return lab;
    }

    public void setLab(String lab) {
        this.lab = lab;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getInstructor() {
        return instructor;
    }

    public void setInstructor(String instructor) {
        this.instructor = instructor;
    }

    public String getEndDate() {
        return endDate;
    }

    public void setEndDate(String endDate) {
        this.endDate = endDate;
    }

    public String getMinGrade() {
        return minGrade;
    }

    public void setMinGrade(String minGrade) {
        this.minGrade = minGrade;
    }

    public List<String> getMarks() {
        return marks;
    }

    public void setMarks(List<String> marks) {
        this.marks = marks;
    }

    public List<String> getMarksMax() {
        return marksMax;
    }

    public void setMarksMax(List<String> marksMax) {
        this.marksMax = marksMax;
    }

    public List<String> getmMarksPercent() {
        return mMarksPercent;
    }

    public void setmMarksPercent(List<String> mMarksPercent) {
        this.mMarksPercent = mMarksPercent;
    }

    public List<String> getGradeDate() {
        return gradeDate;
    }

    public void setGradeDate(List<String> gradeDate) {
        this.gradeDate = gradeDate;
    }
}
