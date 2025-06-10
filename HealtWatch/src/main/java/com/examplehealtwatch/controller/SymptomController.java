package com.examplehealtwatch.controller;

import com.examplehealtwatch.service.SymptomService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;

@RestController
@CrossOrigin(origins = "*")
@RequestMapping("/api/symptoms")
public class SymptomController {

    @Autowired
    private SymptomService symptomService;

    // Dodaj nowy objaw
    @PostMapping("/add")
    public ResponseEntity<Map<String, Object>> addSymptom(
            @RequestParam String apiKey,
            @RequestParam String symptomName,
            @RequestParam Integer intensity,
            @RequestParam String symptomDate,
            @RequestParam(required = false) String notes) {

        try {
            // Parsowanie daty z formatu "yyyy-MM-dd HH:mm:ss"
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
            LocalDateTime dateTime = LocalDateTime.parse(symptomDate, formatter);

            Map<String, Object> response = symptomService.addSymptom(apiKey, symptomName, intensity, dateTime, notes);

            if (response.get("status").equals("error")) {
                return ResponseEntity.status(400).body(response);
            }

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            return ResponseEntity.status(400).body(Map.of(
                    "status", "error",
                    "message", "Invalid date format. Use yyyy-MM-dd HH:mm:ss"
            ));
        }
    }

    // Pobierz wszystkie objawy użytkownika
    @GetMapping("/list")
    public ResponseEntity<Map<String, Object>> getUserSymptoms(@RequestParam String apiKey) {
        Map<String, Object> response = symptomService.getUserSymptoms(apiKey);

        if (response.get("status").equals("error")) {
            return ResponseEntity.status(401).body(response);
        }

        return ResponseEntity.ok(response);
    }

    // Pobierz objawy dla konkretnej nazwy
    @GetMapping("/by-name")
    public ResponseEntity<Map<String, Object>> getSymptomsByName(
            @RequestParam String apiKey,
            @RequestParam String symptomName) {

        Map<String, Object> response = symptomService.getSymptomsByName(apiKey, symptomName);

        if (response.get("status").equals("error")) {
            return ResponseEntity.status(401).body(response);
        }

        return ResponseEntity.ok(response);
    }

    // Usuń objaw
    @DeleteMapping("/delete")
    public ResponseEntity<Map<String, Object>> deleteSymptom(
            @RequestParam String apiKey,
            @RequestParam Long symptomId) {

        Map<String, Object> response = symptomService.deleteSymptom(apiKey, symptomId);

        if (response.get("status").equals("error")) {
            return ResponseEntity.status(400).body(response);
        }

        return ResponseEntity.ok(response);
    }

    // Pobierz unikalne nazwy objawów użytkownika
    @GetMapping("/names")
    public ResponseEntity<Map<String, Object>> getUserSymptomNames(@RequestParam String apiKey) {
        Map<String, Object> response = symptomService.getUserSymptomNames(apiKey);

        if (response.get("status").equals("error")) {
            return ResponseEntity.status(401).body(response);
        }

        return ResponseEntity.ok(response);
    }
}