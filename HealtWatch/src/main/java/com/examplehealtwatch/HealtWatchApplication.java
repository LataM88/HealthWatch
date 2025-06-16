package com.examplehealtwatch;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Główna klasa uruchamiająca aplikację HealthWatch.
 * Odpowiada za start całej aplikacji Spring Boot oraz inicjalizację kontekstu.
 * Punkt wejścia do systemu monitorowania zdrowia.
 */
@SpringBootApplication
public class HealtWatchApplication {

	public static void main(String[] args) {
		SpringApplication.run(HealtWatchApplication.class, args);
	}

}
