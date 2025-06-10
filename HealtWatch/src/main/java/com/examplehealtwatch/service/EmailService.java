package com.examplehealtwatch.service;

import com.examplehealtwatch.Medication;
import com.examplehealtwatch.MedicationRepository;
import com.examplehealtwatch.User;
import com.examplehealtwatch.UserRespository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
public class EmailService {

    @Autowired
    private JavaMailSender mailSender;

    @Autowired
    private MedicationRepository medicationRepository;

    @Autowired
    private UserRespository userRepository;

    public void sendMedicationList(Long userId, String doctorEmail, String message) {
        try {
            // Pobierz użytkownika
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new RuntimeException("Użytkownik nie znaleziony"));

            // Pobierz leki użytkownika
            List<Medication> medications = medicationRepository.findByUserId(userId);

            if (medications.isEmpty()) {
                throw new RuntimeException("Brak leków do wysłania");
            }

            // Stwórz treść emaila
            String emailContent = createEmailContent(user, medications, message);

            // Wyślij email
            SimpleMailMessage mailMessage = new SimpleMailMessage();
            mailMessage.setTo(doctorEmail);
            mailMessage.setSubject("Lista leków pacjenta - " + user.getName() + " " + user.getSurname());
            mailMessage.setText(emailContent);
            mailMessage.setFrom("noreply@healthwatch.com"); // Można ustawić własny adres

            mailSender.send(mailMessage);

            System.out.println("Email wysłany pomyślnie do: " + doctorEmail);

        } catch (Exception e) {
            System.err.println("Błąd podczas wysyłania emaila: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Nie udało się wysłać emaila: " + e.getMessage());
        }
    }

    private String createEmailContent(User user, List<Medication> medications, String userMessage) {
        StringBuilder content = new StringBuilder();

        content.append("LISTA LEKÓW PACJENTA\n");
        content.append("==========================================\n\n");

        content.append("Pacjent: ").append(user.getName()).append(" ").append(user.getSurname()).append("\n");
        content.append("Email: ").append(user.getEmail()).append("\n");
        content.append("Data wysłania: ").append(LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm"))).append("\n\n");

        if (userMessage != null && !userMessage.trim().isEmpty()) {
            content.append("Wiadomość od pacjenta:\n");
            content.append("\"").append(userMessage.trim()).append("\"\n\n");
        }

        content.append("AKTUALNA LISTA LEKÓW:\n");
        content.append("==========================================\n\n");

        for (int i = 0; i < medications.size(); i++) {
            Medication med = medications.get(i);
            content.append(i + 1).append(". ").append(med.getName()).append("\n");
            content.append("   Dawka: ").append(med.getDosage()).append("\n");
            content.append("   Godzina: ").append(med.getTime()).append("\n");
            content.append("   Dni: ").append(formatDays(med.getDays())).append("\n\n");
        }

        content.append("==========================================\n");
        content.append("Email wygenerowany automatycznie przez aplikację HealthWatch\n");
        content.append("Data: ").append(LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm:ss")));

        return content.toString();
    }

    private String formatDays(String days) {
        if (days == null || days.isEmpty()) {
            return "Nie określono";
        }

        // Zamień skróty na pełne nazwy dni
        return days.replace("Pon", "Poniedziałek")
                .replace("Wt", "Wtorek")
                .replace("Śr", "Środa")
                .replace("Czw", "Czwartek")
                .replace("Pt", "Piątek")
                .replace("Sob", "Sobota")
                .replace("Ndz", "Niedziela");
    }
}