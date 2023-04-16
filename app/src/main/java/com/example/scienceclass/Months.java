package com.example.scienceclass;

public class Months {

    String name, status, smid;

    public Months(String name, String status, String smid) {
        this.name = name;
        this.status = status;
        this.smid = smid;
    }

    public Months() {
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getSmid() {
        return smid;
    }

    public void setSmid(String smid) {
        this.smid = smid;
    }
}
