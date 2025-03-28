package com.example.healtwatchp.adapter;

import java.util.List;

public class Medication {
    private String name;
    private String dosage;
    private String time;
    private String days;

    public Medication(String name, String dosage, String time, String days) {
        this.name = name;
        this.dosage = dosage;
        this.time = time;
        this.days = days;
    }

    public String getName() {
        return name;
    }

    public String getDosage() {
        return dosage;
    }

    public String getTime() {
        return time;
    }

    public String getDays() {
        return days;
    }
}
