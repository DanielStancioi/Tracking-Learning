package com.example.tlfsvf;

import java.util.ArrayList;
import java.util.List;

public class CourseModel {
    private String course, description, id, date, location, instructor, minGrade, percent, examDate;
    private List<String> marks ;
    private List<String> marksMax ;
    private List<String> mMarksPercent;
    private List<String> gradeDate;
    public CourseModel(){

    }

    public CourseModel(String course, String description, String id, String date, List<String> marks,  String instructor, String location, List<String> marksMax, List<String> mMarksPercent, String minGrade, List<String> gradeDate, String percent, String examDate) {
        this.course = course;
        this.description = description;
        this.id = id;
        this.date = date;
        this.marks= marks;
        this.location= location;
        this.instructor= instructor;
        this.percent = percent;
        this.marksMax = marksMax;
        this.mMarksPercent = mMarksPercent;

        this.minGrade =minGrade;
        this.gradeDate = gradeDate;
        this.examDate = examDate;
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



    public void setLocation(String location) {
        this.location = location;
    }

    public void setInstructor(String instructor) {
        this.instructor = instructor;
    }

    public String getExamDate() {
        return examDate;
    }

    public void setExamDate(String examDate) {
        this.examDate = examDate;
    }

    public List<String> getMarksMax() {
        return marksMax;
    }

    public List<String> getmMarksPercent() {
        return mMarksPercent;
    }



    public String getMinGrade() {
        return minGrade;
    }

    public List<String> getGradeDate() {
        return gradeDate;
    }

    public String getPercent() {
        return percent;
    }

    public void setPercent(String percent) {
        this.percent = percent;
    }

    public void setMinGrade(String minGrade) {
        this.minGrade = minGrade;
    }

    public void setMarks(List<String> marks) {
        this.marks = marks;
    }

    public void setMarksMax(List<String> marksMax) {
        this.marksMax = marksMax;
    }

    public void setmMarksPercent(List<String> mMarksPercent) {
        this.mMarksPercent = mMarksPercent;
    }

    public void setGradeDate(List<String> gradeDate) {
        this.gradeDate = gradeDate;
    }
}
