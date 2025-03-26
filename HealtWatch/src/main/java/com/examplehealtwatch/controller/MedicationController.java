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
}
