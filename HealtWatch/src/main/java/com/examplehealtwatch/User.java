package com.examplehealtwatch;

import jakarta.persistence.*;
import lombok.*;

import java.util.List;

/**
 * Klasa reprezentująca użytkownika systemu.
 * Przechowuje dane osobowe, dane logowania oraz powiązania z innymi encjami.
 * Stanowi podstawę do uwierzytelniania i autoryzacji w aplikacji.
 */
@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
@ToString
@Builder
public class User {
    /**
     * Unikalny identyfikator użytkownika.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Imię użytkownika.
     */
    @Column(nullable = false, length = 20)
    private String name;

    /**
     * Nazwisko użytkownika.
     */
    @Column(nullable = false, length = 20)
    private String surname;

    /**
     * Adres e-mail użytkownika (unikalny).
     */
    @Column(nullable = false, unique = true, length = 45)
    private String email;

    /**
     * Zaszyfrowane hasło użytkownika.
     */
    @Column(nullable = false, length = 640)
    private String password;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL)
    @ToString.Exclude
    private List<Medication> medications;
}
