package com.examplehealtwatch.service;

import com.examplehealtwatch.Symptom;
import com.examplehealtwatch.SymptomRepository;
import com.examplehealtwatch.User;
import com.examplehealtwatch.UserRespository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;

@Service
public class SymptomService {

    @Autowired
    private SymptomRepository symptomRepository;

    @Autowired
    private UserRespository userRepository;

    @Autowired
    private LoginService loginService;

    // Dodaj nowy objaw
    public Map<String, Object> addSymptom(String apiKey, String symptomName,
                                          Integer intensity, LocalDateTime symptomDate, String notes) {
        Map<String, Object> response = new HashMap<>();

        // Walidacja API key
        if (!loginService.validateApiKey(apiKey)) {
            response.put("status", "error");
            response.put("message", "Invalid API key");
            return response;
        }

        Long userId = loginService.getUserIdFromApiKey(apiKey);
        Optional<User> userOptional = userRepository.findById(userId);

        if (userOptional.isEmpty()) {
            response.put("status", "error");
            response.put("message", "User not found");
            return response;
        }

        try {
            Symptom symptom = Symptom.builder()
                    .user(userOptional.get())
                    .symptomName(symptomName.trim())
                    .intensity(intensity)
                    .symptomDate(symptomDate)
                    .notes(notes != null ? notes.trim() : null)
                    .build();

            symptomRepository.save(symptom);

            response.put("status", "success");
            response.put("message", "Symptom added successfully");
            response.put("symptomId", symptom.getId());

        } catch (Exception e) {
            response.put("status", "error");
            response.put("message", "Error adding symptom: " + e.getMessage());
        }

        return response;
    }

    // Pobierz wszystkie objawy użytkownika
    public Map<String, Object> getUserSymptoms(String apiKey) {
        Map<String, Object> response = new HashMap<>();

        if (!loginService.validateApiKey(apiKey)) {
            response.put("status", "error");
            response.put("message", "Invalid API key");
            return response;
        }

        Long userId = loginService.getUserIdFromApiKey(apiKey);
        List<Symptom> symptoms = symptomRepository.findByUserIdOrderBySymptomDateDesc(userId);

        List<Map<String, Object>> symptomsList = new ArrayList<>();
        for (Symptom symptom : symptoms) {
            Map<String, Object> symptomData = new HashMap<>();
            symptomData.put("id", symptom.getId());
            symptomData.put("symptomName", symptom.getSymptomName());
            symptomData.put("intensity", symptom.getIntensity());
            symptomData.put("symptomDate", symptom.getSymptomDate().toString());
            symptomData.put("notes", symptom.getNotes());
            symptomsList.add(symptomData);
        }

        response.put("status", "success");
        response.put("symptoms", symptomsList);
        return response;
    }

    // Pobierz objawy dla konkretnej nazwy
    public Map<String, Object> getSymptomsByName(String apiKey, String symptomName) {
        Map<String, Object> response = new HashMap<>();

        if (!loginService.validateApiKey(apiKey)) {
            response.put("status", "error");
            response.put("message", "Invalid API key");
            return response;
        }

        Long userId = loginService.getUserIdFromApiKey(apiKey);
        List<Symptom> symptoms = symptomRepository.findByUserIdAndSymptomNameIgnoreCaseOrderBySymptomDateDesc(userId, symptomName);

        List<Map<String, Object>> symptomsList = new ArrayList<>();
        for (Symptom symptom : symptoms) {
            Map<String, Object> symptomData = new HashMap<>();
            symptomData.put("id", symptom.getId());
            symptomData.put("symptomName", symptom.getSymptomName());
            symptomData.put("intensity", symptom.getIntensity());
            symptomData.put("symptomDate", symptom.getSymptomDate().toString());
            symptomData.put("notes", symptom.getNotes());
            symptomsList.add(symptomData);
        }

        response.put("status", "success");
        response.put("symptoms", symptomsList);
        return response;
    }

    // Usuń objaw
    public Map<String, Object> deleteSymptom(String apiKey, Long symptomId) {
        Map<String, Object> response = new HashMap<>();

        if (!loginService.validateApiKey(apiKey)) {
            response.put("status", "error");
            response.put("message", "Invalid API key");
            return response;
        }

        Long userId = loginService.getUserIdFromApiKey(apiKey);
        Optional<Symptom> symptomOptional = symptomRepository.findById(symptomId);

        if (symptomOptional.isEmpty()) {
            response.put("status", "error");
            response.put("message", "Symptom not found");
            return response;
        }

        Symptom symptom = symptomOptional.get();
        if (!symptom.getUser().getId().equals(userId)) {
            response.put("status", "error");
            response.put("message", "Unauthorized access");
            return response;
        }

        try {
            symptomRepository.delete(symptom);
            response.put("status", "success");
            response.put("message", "Symptom deleted successfully");
        } catch (Exception e) {
            response.put("status", "error");
            response.put("message", "Error deleting symptom: " + e.getMessage());
        }

        return response;
    }

    // Pobierz unikalne nazwy objawów użytkownika
    public Map<String, Object> getUserSymptomNames(String apiKey) {
        Map<String, Object> response = new HashMap<>();

        if (!loginService.validateApiKey(apiKey)) {
            response.put("status", "error");
            response.put("message", "Invalid API key");
            return response;
        }

        Long userId = loginService.getUserIdFromApiKey(apiKey);
        List<String> symptomNames = symptomRepository.findDistinctSymptomNamesByUserId(userId);

        response.put("status", "success");
        response.put("symptomNames", symptomNames);
        return response;
    }
}