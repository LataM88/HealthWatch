package com.examplehealtwatch.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Kontroler główny aplikacji.
 * Odpowiada za podstawowe endpointy testowe oraz sprawdzanie działania serwera.
 */
@RestController
public class HomeController {

    @GetMapping("/")
    public String home() {
        return "Serwer działa poprawnie!";
    }
}
