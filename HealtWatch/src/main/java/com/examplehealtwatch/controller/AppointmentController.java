package com.examplehealtwatch.controller;

import com.examplehealtwatch.request.AppointmentRequest;
import com.examplehealtwatch.service.AppointmentService;
import com.examplehealtwatch.service.LoginService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@CrossOrigin(origins = "*")
@RequestMapping("/api")
/**
 * Kontroler REST do obsługi wizyt lekarskich.
 * Umożliwia dodawanie, pobieranie i zarządzanie wizytami użytkowników.
 * Integruje się z warstwą serwisową oraz obsługuje żądania HTTP.
 */
public class AppointmentController {

    @Autowired
    private AppointmentService appointmentService;

    @Autowired
    private LoginService loginService;

    /**
     * Dodaje nową wizytę lekarską dla zalogowanego użytkownika.
     * Wymaga poprawnego klucza API w nagłówku żądania.
     * @param request dane wizyty
     * @param apiKey klucz autoryzacyjny użytkownika
     * @return odpowiedź z informacją o powodzeniu operacji
     */
    @PostMapping("/appointment")
    public ResponseEntity<String> addAppointment(@RequestBody AppointmentRequest request,
                                                 @RequestHeader("Authorization") String apiKey) {
        if (!loginService.validateApiKey(apiKey)) {
            return ResponseEntity.status(403).body("Invalid API Key");
        }

        Long userId = loginService.getUserIdFromApiKey(apiKey);
        appointmentService.saveAppointment(request, userId);

        System.out.println("Dodano wizytę dla użytkownika ID: " + userId);
        return ResponseEntity.ok("{\"message\": \"Wizyta dodana!\"}");
    }

    /**
     * Pobiera listę wszystkich wizyt zalogowanego użytkownika.
     * Wymaga poprawnego klucza API w nagłówku żądania.
     * @param apiKey klucz autoryzacyjny użytkownika
     * @return lista wizyt w formie mapy
     */
    @GetMapping("/appointments")
    public ResponseEntity<List<Map<String, String>>> getAppointments(@RequestHeader("Authorization") String apiKey) {
        System.out.println("Otrzymano API Key: " + apiKey);
        if (!loginService.validateApiKey(apiKey)) {
            System.out.println("Nieprawidłowy klucz API");
            return ResponseEntity.status(403).body(null);
        }

        Long userId = loginService.getUserIdFromApiKey(apiKey);
        System.out.println("ID użytkownika: " + userId);
        List<Map<String, String>> appointments = appointmentService.getAppointmentsForUser(userId);

        return ResponseEntity.ok(appointments);
    }

    /**
     * Usuwa wizytę o podanym ID, jeśli należy do zalogowanego użytkownika.
     * Wymaga poprawnego klucza API w nagłówku żądania.
     * @param id identyfikator wizyty
     * @param apiKey klucz autoryzacyjny użytkownika
     * @return odpowiedź z informacją o powodzeniu lub błędzie
     */
    @DeleteMapping("/appointment/{id}")
    public ResponseEntity<String> deleteAppointment(@PathVariable Long id, @RequestHeader("Authorization") String apiKey) {
        System.out.println("Otrzymano DELETE dla wizyty ID: " + id);
        System.out.println("API Key w żądaniu: " + apiKey);

        if (!loginService.validateApiKey(apiKey)) {
            System.out.println("Nieprawidłowy klucz API!");
            return ResponseEntity.status(403).body("Invalid API Key");
        }

        Long userId = loginService.getUserIdFromApiKey(apiKey);
        boolean deleted = appointmentService.deleteAppointment(id, userId);

        if (deleted) {
            System.out.println("Wizyta usunięta poprawnie!");
            return ResponseEntity.ok("{\"message\": \"Wizyta usunięta!\"}");
        } else {
            System.out.println("Wizyta nie znaleziona lub brak uprawnień!");
            return ResponseEntity.status(404).body("{\"message\": \"Wizyta nie znaleziona lub brak uprawnień!\"}");
        }
    }
}