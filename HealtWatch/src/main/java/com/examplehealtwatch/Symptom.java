package com.examplehealtwatch;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * Klasa opisująca objaw zgłoszony przez użytkownika.
 * Pozwala na rejestrowanie objawów wraz z datą i powiązaniem z użytkownikiem.
 * Ułatwia monitorowanie stanu zdrowia w aplikacji.
 */
@Entity
@Table(name = "symptoms")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
@ToString
@Builder
public class Symptom {

    /**
     * Unikalny identyfikator objawu.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Użytkownik powiązany z objawem.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    @ToString.Exclude
    private User user;

    /**
     * Nazwa objawu.
     */
    @Column(name = "symptom_name", nullable = false)
    private String symptomName;

    /**
     * Intensywność objawu (1-10).
     */
    @Column(nullable = false)
    private Integer intensity; // 1-10

    /**
     * Data i godzina wystąpienia objawu.
     */
    @Column(name = "symptom_date", nullable = false)
    private LocalDateTime symptomDate;

    /**
     * Dodatkowe notatki dotyczące objawu.
     */
    @Column(columnDefinition = "TEXT")
    private String notes;

    /**
     * Data utworzenia rekordu objawu.
     */
    @Column(name = "created_at")
    private LocalDateTime createdAt;

    /**
     * Data ostatniej aktualizacji rekordu objawu.
     */
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    public void prePersist() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}