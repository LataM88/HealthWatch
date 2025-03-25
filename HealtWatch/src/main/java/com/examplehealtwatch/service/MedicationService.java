package com.examplehealtwatch.service;

import com.examplehealtwatch.Medication;
import com.examplehealtwatch.MedicationRepository;
import com.examplehealtwatch.request.MedicationRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class MedicationService {

    @Autowired
    private MedicationRepository medicationRepository;

    public void saveMedication(MedicationRequest request) {
        Medication medication = new Medication();
        medication.setName(request.getName());
        medication.setDosage(request.getDosage());
        medication.setTime(request.getTime());
        medication.setDays(request.getDays());
        medicationRepository.save(medication);
    }
}
