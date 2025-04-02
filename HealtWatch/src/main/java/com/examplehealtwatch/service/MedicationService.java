package com.examplehealtwatch.service;

import com.examplehealtwatch.Medication;
import com.examplehealtwatch.MedicationRepository;
import com.examplehealtwatch.User;
import com.examplehealtwatch.UserRespository;
import com.examplehealtwatch.request.MedicationRequest;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class MedicationService {

    @Autowired
    private MedicationRepository medicationRepository;

    @Autowired
    private UserRespository userRepository;

    @Transactional
    public void saveMedication(MedicationRequest request, Long userId) {
        User user = userRepository.findById(userId).orElseThrow(() -> new RuntimeException("User not found"));
        Medication medication = new Medication();
        medication.setName(request.getName());
        medication.setDosage(request.getDosage());
        medication.setTime(request.getTime());
        medication.setDays(request.getDays());
        medication.setUser(user);

        try {
            System.out.println("Zapisywanie leku: " + medication);
            medicationRepository.save(medication);
            System.out.println("Lek zapisany w bazie!");
        } catch (Exception e) {
            System.err.println("Błąd podczas zapisu leku: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public List<Map<String, String>> getMedicationsForUser(Long userId) {
        List<Medication> medications = medicationRepository.findByUserId(userId);

        List<Map<String, String>> result = new ArrayList<>();
        for (Medication medication : medications) {
            Map<String, String> medicationData = new HashMap<>();
            medicationData.put("id", medication.getId().toString());  // Konwersja Long na String
            medicationData.put("name", medication.getName());
            medicationData.put("dosage", medication.getDosage());
            medicationData.put("time", medication.getTime());
            medicationData.put("days", medication.getDays());
            result.add(medicationData);
        }

        return result;
    }

    public boolean deleteMedication(Long id, Long userId) {
        Optional<Medication> medication = medicationRepository.findById(id);

        if (medication.isPresent() && medication.get().getUser().getId().equals(userId)) {
            medicationRepository.deleteById(id);
            return true;
        } else {
            return false;
        }
    }
}