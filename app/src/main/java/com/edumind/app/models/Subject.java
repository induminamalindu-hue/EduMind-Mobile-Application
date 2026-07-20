package com.edumind.app.models;

public class Subject {
    public int id;
    public String name;
    public String code;
    public String branchName;
    public Integer studentCount;

    @Override
    public String toString() {
        return code + " — " + name;
    }
}
