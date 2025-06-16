package com.examplehealtwatch;

import jakarta.persistence.*;
import lombok.*;

/**
 * Klasa reprezentująca wizytę lekarską w systemie.
 * Przechowuje informacje o lekarzu, dacie i godzinie wizyty.
 * Umożliwia powiązanie wizyt z użytkownikami oraz zarządzanie nimi w bazie danych.
 */
@Entity
@Table(name = "appointments")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
@ToString
@Builder
public class Appointment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "doctor_name")
    private String doctorName;

    private String date;
    private String time;
    private String notes;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    @ToString.Exclude
    private User user;
}
