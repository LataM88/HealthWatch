package com.examplehealtwatch;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface SymptomRepository extends JpaRepository<Symptom, Long> {

    // Znajdź wszystkie objawy dla konkretnego użytkownika
    List<Symptom> findByUserIdOrderBySymptomDateDesc(Long userId);

    // Znajdź objawy użytkownika w określonym przedziale czasowym
    List<Symptom> findByUserIdAndSymptomDateBetweenOrderBySymptomDateDesc(
            Long userId, LocalDateTime startDate, LocalDateTime endDate);

    // Znajdź objawy użytkownika o konkretnej nazwie
    List<Symptom> findByUserIdAndSymptomNameIgnoreCaseOrderBySymptomDateDesc(
            Long userId, String symptomName);

    // Znajdź objawy użytkownika o konkretnej nazwie w przedziale czasowym
    List<Symptom> findByUserIdAndSymptomNameIgnoreCaseAndSymptomDateBetweenOrderBySymptomDateDesc(
            Long userId, String symptomName, LocalDateTime startDate, LocalDateTime endDate);

    // Znajdź wszystkie unikalne nazwy objawów dla użytkownika
    @Query("SELECT DISTINCT s.symptomName FROM Symptom s WHERE s.user.id = :userId ORDER BY s.symptomName")
    List<String> findDistinctSymptomNamesByUserId(@Param("userId") Long userId);
}