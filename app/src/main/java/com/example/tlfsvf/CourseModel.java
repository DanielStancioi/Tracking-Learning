package com.example.tlfsvf;

import java.util.ArrayList;
import java.util.List;

public class CourseModel {
    private String course, description, id, date, location, instructor, credits;
    private List<Double> marks ;
    private List<Double> marksMax ;
    List<Integer> mMarksPercent;
    public CourseModel(){

    }

    public CourseModel(String course, String description, String id, String date, List<Double> marks, String credits, String instructor, String location, List<Double> marksMax, List<Integer> mMarksPercent) {
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

    public void addMark(Double mark){
        this.marks.add(mark);
    }
    public List<Double> getMarks(){
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

    public List<Double> getMarksMax() {
        return marksMax;
    }

    public List<Integer> getmMarksPercent() {
        return mMarksPercent;
    }
}
