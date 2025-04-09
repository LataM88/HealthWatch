package com.example.healtwatchp.adapter;

public class Medication {
    private Long id;
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

    public Medication(Long id, String name, String dosage, String time, String days) {
        this.id = id;
        this.name = name;
        this.dosage = dosage;
        this.time = time;
        this.days = days;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    // Pozostałe gettery
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