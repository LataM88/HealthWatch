package com.examplehealtwatch;

import jakarta.persistence.*;
import lombok.*;

/**
 * Klasa reprezentująca lek przyjmowany przez użytkownika.
 * Zawiera informacje o nazwie leku, dawkowaniu, czasie i dniach przyjmowania.
 * Umożliwia rejestrowanie i zarządzanie lekami w systemie.
 */
@Entity
@Table(name = "medication")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
@ToString
@Builder
public class Medication {

    /**
     * Unikalny identyfikator leku.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Nazwa leku.
     */
    private String name;

    /**
     * Dawka leku.
     */
    private String dosage;

    /**
     * Godzina przyjmowania leku.
     */
    private String time;

    /**
     * Dni tygodnia, w których lek jest przyjmowany.
     */
    private String days;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    @ToString.Exclude
    private User user;
}
