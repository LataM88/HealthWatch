package com.examplehealtwatch.repository;

import com.examplehealtwatch.Appointment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/**
 * Repozytorium do zarządzania encjami Appointment.
 * Umożliwia wykonywanie operacji CRUD na wizytach w bazie danych.
 * Wspiera integrację z warstwą serwisową aplikacji.
 */
public interface AppointmentRepository extends JpaRepository<Appointment, Long> {
    List<Appointment> findByUserId(Long userId);
}
