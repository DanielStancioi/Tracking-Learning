package com.example.tlfsvf;

import java.util.ArrayList;
import java.util.List;

public class CourseModel {
    private String course, description, id, date, location, instructor, credits, endDate, minGrade;
    private List<String> marks ;
    private List<String> marksMax ;
    private List<String> mMarksPercent;
    private List<String> gradeDate;
    public CourseModel(){

    }

    public CourseModel(String course, String description, String id, String date, List<String> marks, String credits, String instructor, String location, List<String> marksMax, List<String> mMarksPercent, String endDate, String minGrade, List<String> gradeDate) {
        this.course = course;
        this.description = description;
        this.id = id;
        this.date = date;
        this.marks= marks;
        this.location= location;
        this.instructor= instructor;
        this.credits= credits;
        this.marksMax = marksMax;
        this.mMarksPercent = mMarksPercent;
        this.endDate = endDate;
        this.minGrade =minGrade;
        this.gradeDate = gradeDate;
    }

    public String getCourse() {
        return course;
    }

    public String getDescription() {
        return description;
    }

    public String getId() {
        return id;
    }

    public String getDate() {
        return date;
    }

    public void setCourse(String course) {
        this.course = course;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public void addMark(String mark){
        this.marks.add(mark);
    }
    public List<String> getMarks(){
        return this.marks;
    }

    public String getLocation() {
        return location;
    }

    public String getInstructor() {
        return instructor;
    }

    public String getCredits() {
        return credits;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public void setInstructor(String instructor) {
        this.instructor = instructor;
    }

    public void setCredits(String credits) {
        this.credits = credits;
    }

    public List<String> getMarksMax() {
        return marksMax;
    }

    public List<String> getmMarksPercent() {
        return mMarksPercent;
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

    public List<String> getGradeDate() {
        return gradeDate;
    }
}
