package com.example.healtwatchp.models;

public class Symptom {
    private Long id;
    private String symptomName;
    private Integer intensity;
    private String symptomDate;
    private String notes;

    public Symptom() {}

    public Symptom(String symptomName, Integer intensity, String symptomDate, String notes) {
        this.symptomName = symptomName;
        this.intensity = intensity;
        this.symptomDate = symptomDate;
        this.notes = notes;
    }

    // Getters
    public Long getId() { return id; }
    public String getSymptomName() { return symptomName; }
    public Integer getIntensity() { return intensity; }
    public String getSymptomDate() { return symptomDate; }
    public String getNotes() { return notes; }

    // Setters
    public void setId(Long id) { this.id = id; }
    public void setSymptomName(String symptomName) { this.symptomName = symptomName; }
    public void setIntensity(Integer intensity) { this.intensity = intensity; }
    public void setSymptomDate(String symptomDate) { this.symptomDate = symptomDate; }
    public void setNotes(String notes) { this.notes = notes; }

    // Helper method do formatowania daty dla wyświetlania
    public String getFormattedDate() {
        try {
            // Konwersja z "2024-06-10T14:30:00" do "10.06.2024 14:30"
            String[] parts = symptomDate.split("T");
            String datePart = parts[0]; // 2024-06-10
            String timePart = parts[1].substring(0, 5); // 14:30

            String[] dateParts = datePart.split("-");
            return dateParts[2] + "." + dateParts[1] + "." + dateParts[0] + " " + timePart;
        } catch (Exception e) {
            return symptomDate; // Zwróć oryginalną datę w przypadku błędu
        }
    }

    // Helper method do opisu intensywności
    public String getIntensityDescription() {
        if (intensity <= 3) return "Łagodne";
        else if (intensity <= 6) return "Średnie";
        else return "Silne";
    }
}