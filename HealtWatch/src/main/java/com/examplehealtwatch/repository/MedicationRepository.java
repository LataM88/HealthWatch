package com.examplehealtwatch.repository;

import com.examplehealtwatch.Medication;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/**
 * Repozytorium do zarządzania encjami Medication.
 * Pozwala na operacje CRUD na lekach oraz integrację z warstwą serwisową.
 * Ułatwia dostęp do danych o lekach zapisanych w bazie.
 */
public interface MedicationRepository extends JpaRepository<Medication, Long> {
    List<Medication> findByUserId(Long userId);
}
