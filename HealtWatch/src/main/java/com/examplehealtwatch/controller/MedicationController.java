package com.examplehealtwatch.controller;

import com.examplehealtwatch.User;
import com.examplehealtwatch.request.MedicationRequest;
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
}
