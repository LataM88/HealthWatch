package com.example.healtwatchp.adapter;

public class Appointment {
    private Long id;
    private String doctorName;
    private String date;
    private String time;
    private String notes;

    public Appointment(String doctorName, String date, String time, String notes) {
        this.doctorName = doctorName;
        this.date = date;
        this.time = time;
        this.notes = notes;
    }

    public Appointment(Long id, String doctorName, String date, String time, String notes) {
        this.id = id;
        this.doctorName = doctorName;
        this.date = date;
        this.time = time;
        this.notes = notes;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getDoctorName() { return doctorName; }
    public String getDate() { return date; }
    public String getTime() { return time; }
    public String getNotes() { return notes; }
}
