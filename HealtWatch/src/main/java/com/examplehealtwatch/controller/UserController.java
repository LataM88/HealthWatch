package com.examplehealtwatch.controller;

import com.examplehealtwatch.User;
import com.examplehealtwatch.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@CrossOrigin(origins = "*")
@RequestMapping("/api")
/**
 * Kontroler REST do obsługi użytkowników.
 * Odpowiada za rejestrację, pobieranie i zarządzanie danymi użytkowników.
 * Integruje się z warstwą serwisową i obsługuje żądania HTTP.
 */
public class UserController {

    @Autowired
    private UserService userService;

    /**
     * Rejestruje nowego użytkownika w systemie.
     * Tworzy nowy obiekt użytkownika na podstawie przesłanych danych.
     * @param name imię użytkownika
     * @param surname nazwisko użytkownika
     * @param email adres e-mail użytkownika
     * @param password hasło użytkownika
     * @return odpowiedź z informacją o powodzeniu rejestracji
     */
    @PostMapping("/register")
    public ResponseEntity<String> registerUser(@RequestParam String name,
                                               @RequestParam String surname,
                                               @RequestParam String email,
                                               @RequestParam String password) {
        User user = new User();
        user.setName(name);
        user.setSurname(surname);
        user.setEmail(email);
        user.setPassword(password);

        boolean isRegistered = userService.registerUser(user);
        if (isRegistered) {
            return ResponseEntity.ok("success");
        } else {
            return ResponseEntity.status(400).body("User already exists");
        }
    }
}