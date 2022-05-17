package com.example.tlfsvf;

public class Model {
    private String task, description, id, date, dueDate;
    private boolean done;
    public Model(){

    }

    public Model(String task, String description, String id, String date, boolean done, String dueDate) {
        this.task = task;
        this.description = description;
        this.id = id;
        this.date = date;
        this.done = done;
        this.dueDate = dueDate;
    }

    public String getTask() {
        return task;
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

    public void setTask(String task) {
        this.task = task;
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

    public boolean isDone() {
        return this.done;
    }

    public void setDone(boolean done) {
        done = done;
    }

    public String getDueDate() {
        return dueDate;
    }

    public void setDueDate(String dueDate) {
        this.dueDate = dueDate;
    }
}
