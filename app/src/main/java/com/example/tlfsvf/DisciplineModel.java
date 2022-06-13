package com.example.tlfsvf;

public class DisciplineModel {
    private String name, description, id, date, credits, endDate, year, type, context;
    private CourseModel cmodel;
    private LabModel labModel;

    public DisciplineModel(){

    }

    public DisciplineModel(String name, String description, String id, String date, String credits,String type,String context,String year,  String endDate, CourseModel cmodel, LabModel labModel) {
        this.name = name;
        this.description = description;
        this.id = id;
        this.date = date;
        this.credits = credits;
        this.cmodel = cmodel;
        this.labModel = labModel;
        this.endDate = endDate;
        this.year = year;
        this.type = type;
        this.context =context;
    }

    public String getName() {
        return name;
    }

    public String getYear() {
        return year;
    }

    public void setYear(String year) {
        this.year = year;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getContext() {
        return context;
    }

    public void setContext(String context) {
        this.context = context;
    }

    public void setName(String name) {
        this.name = name;
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

    public String getEndDate() {
        return endDate;
    }

    public void setEndDate(String endDate) {
        this.endDate = endDate;
    }

    public CourseModel getCmodel() {
        return cmodel;
    }

    public void setCmodel(CourseModel cmodel) {
        this.cmodel = cmodel;
    }

    public LabModel getLabModel() {
        return labModel;
    }

    public void setLabModel(LabModel labModel) {
        this.labModel = labModel;
    }

    public String getCredits() {
        return credits;
    }

    public void setCredits(String credits) {
        this.credits = credits;
    }
}
