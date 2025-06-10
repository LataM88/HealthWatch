package com.examplehealtwatch.controller;

import com.examplehealtwatch.User;
import com.examplehealtwatch.request.MedicationRequest;
import com.examplehealtwatch.service.EmailService;
import com.examplehealtwatch.service.LoginService;
import com.examplehealtwatch.service.MedicationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@CrossOrigin(origins = "*")
@RequestMapping("/api")
public class MedicationController {

    @Autowired
    private MedicationService medicationService;

    @Autowired
    private LoginService loginService;

    @Autowired
    private EmailService emailService;

    @PostMapping("/medication")
    public ResponseEntity<String> addMedication(@RequestBody MedicationRequest request,
                                                @RequestHeader("Authorization") String apiKey) {
        if (!loginService.validateApiKey(apiKey)) {
            return ResponseEntity.status(403).body("Invalid API Key");
        }

        Long userId = loginService.getUserIdFromApiKey(apiKey);
        medicationService.saveMedication(request, userId);

        System.out.println("Dodano lek dla użytkownika ID: " + userId);
        return ResponseEntity.ok("{\"message\": \"Lek dodany!\"}");
    }

    @GetMapping("/medications")
    public ResponseEntity<List<Map<String, String>>> getMedications(@RequestHeader("Authorization") String apiKey) {
        System.out.println("Otrzymano API Key: " + apiKey);
        if (!loginService.validateApiKey(apiKey)) {
            System.out.println("Nieprawidłowy klucz API");
            return ResponseEntity.status(403).body(null);
        }

        Long userId = loginService.getUserIdFromApiKey(apiKey);
        System.out.println("ID użytkownika: " + userId);
        List<Map<String, String>> medications = medicationService.getMedicationsForUser(userId);

        return ResponseEntity.ok(medications);
    }

    @DeleteMapping("/medication/{id}")
    public ResponseEntity<String> deleteMedication(@PathVariable Long id, @RequestHeader("Authorization") String apiKey) {
        System.out.println("Otrzymano DELETE dla leku ID: " + id);
        System.out.println("API Key w żądaniu: " + apiKey);

        if (!loginService.validateApiKey(apiKey)) {
            System.out.println("Nieprawidłowy klucz API!");
            return ResponseEntity.status(403).body("Invalid API Key");
        }

        Long userId = loginService.getUserIdFromApiKey(apiKey);
        boolean deleted = medicationService.deleteMedication(id, userId);

        if (deleted) {
            System.out.println("Lek usunięty poprawnie!");
            return ResponseEntity.ok("{\"message\": \"Lek usunięty!\"}");
        } else {
            System.out.println("Lek nie znaleziony lub brak uprawnień!");
            return ResponseEntity.status(404).body("{\"message\": \"Lek nie znaleziony lub brak uprawnień!\"}");
        }
    }
    @PostMapping("/medications/share")
    public ResponseEntity<Map<String, String>> shareMedicationList(
            @RequestHeader("Authorization") String apiKey,
            @RequestParam String doctorEmail,
            @RequestParam(required = false, defaultValue = "") String message) {

        Map<String, String> response = new HashMap<>();

        try {
            // Walidacja API Key
            if (!loginService.validateApiKey(apiKey)) {
                response.put("status", "error");
                response.put("message", "Nieprawidłowy klucz API");
                return ResponseEntity.status(403).body(response);
            }

            // Walidacja email
            if (doctorEmail == null || doctorEmail.trim().isEmpty() || !isValidEmail(doctorEmail)) {
                response.put("status", "error");
                response.put("message", "Nieprawidłowy adres email");
                return ResponseEntity.status(400).body(response);
            }

            Long userId = loginService.getUserIdFromApiKey(apiKey);

            // Wyślij email z listą leków
            emailService.sendMedicationList(userId, doctorEmail.trim(), message);

            response.put("status", "success");
            response.put("message", "Lista dostarczona na adres: " + doctorEmail);

            System.out.println("Lista leków wysłana dla użytkownika ID: " + userId + " na email: " + doctorEmail);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            System.err.println("Błąd podczas wysyłania listy leków: " + e.getMessage());
            e.printStackTrace();

            response.put("status", "error");
            response.put("message", "Nie udało się wysłać listy leków: " + e.getMessage());
            return ResponseEntity.status(500).body(response);
        }
    }
    private boolean isValidEmail(String email) {
        return email != null && email.contains("@") && email.contains(".") && email.length() > 5;
    }
}

