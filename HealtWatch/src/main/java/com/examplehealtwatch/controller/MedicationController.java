package com.examplehealtwatch.controller;

import com.examplehealtwatch.request.MedicationRequest;
import com.examplehealtwatch.service.MedicationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@CrossOrigin(origins = "*")
@RequestMapping("/api")
public class MedicationController {

    @Autowired
    private MedicationService medicationService;

    @PostMapping("/medication")
    public ResponseEntity<String> addMedication(@RequestBody MedicationRequest request) {
        System.out.println("Otrzymano żądanie POST na /api/medication");
        System.out.println("Treść żądania: " + request);

        medicationService.saveMedication(request);

        String jsonResponse = "{\"message\": \"Lek dodany!\"}"; // Ręczne utworzenie odpowiedzi jako JSON String
        return ResponseEntity.ok(jsonResponse);
    }
}
