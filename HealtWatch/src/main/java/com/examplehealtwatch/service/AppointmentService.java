package com.examplehealtwatch.service;

import com.examplehealtwatch.Appointment;
import com.examplehealtwatch.repository.AppointmentRepository;
import com.examplehealtwatch.User;
import com.examplehealtwatch.repository.UserRespository;
import com.examplehealtwatch.request.AppointmentRequest;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * Serwis odpowiedzialny za logikę biznesową wizyt lekarskich.
 * Zarządza operacjami na wizytach, w tym zapisem i pobieraniem danych z repozytorium.
 * Integruje się z warstwą kontrolerów oraz obsługuje transakcje.
 */
@Service
public class AppointmentService {

    @Autowired
    private AppointmentRepository appointmentRepository;

    @Autowired
    private UserRespository userRepository;

    /**
     * Zapisuje nową wizytę lekarską powiązaną z użytkownikiem o podanym ID.
     * Tworzy obiekt wizyty na podstawie danych z żądania i zapisuje go w bazie.
     * @param request dane wizyty
     * @param userId identyfikator użytkownika
     */
    @Transactional
    public void saveAppointment(AppointmentRequest request, Long userId) {
        User user = userRepository.findById(userId).orElseThrow(() -> new RuntimeException("User not found"));
        Appointment appointment = new Appointment();
        appointment.setDoctorName(request.getDoctorName());
        appointment.setDate(request.getDate());
        appointment.setTime(request.getTime());
        appointment.setNotes(request.getNotes());
        appointment.setUser(user);

        try {
            System.out.println("Zapisywanie wizyty: " + appointment);
            appointmentRepository.save(appointment);
            System.out.println("Wizyta zapisana w bazie!");
        } catch (Exception e) {
            System.err.println("Błąd podczas zapisu wizyty: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Pobiera wszystkie wizyty powiązane z danym użytkownikiem.
     * Zwraca listę map z podstawowymi danymi każdej wizyty.
     * @param userId identyfikator użytkownika
     * @return lista wizyt w formie mapy
     */
    public List<Map<String, String>> getAppointmentsForUser(Long userId) {
        List<Appointment> appointments = appointmentRepository.findByUserId(userId);

        List<Map<String, String>> result = new ArrayList<>();
        for (Appointment appointment : appointments) {
            Map<String, String> appointmentData = new HashMap<>();
            appointmentData.put("id", appointment.getId().toString());
            appointmentData.put("doctorName", appointment.getDoctorName());
            appointmentData.put("date", appointment.getDate());
            appointmentData.put("time", appointment.getTime());
            appointmentData.put("notes", appointment.getNotes());
            result.add(appointmentData);
        }

        return result;
    }

    /**
     * Usuwa wizytę o podanym ID, jeśli należy do użytkownika o podanym ID.
     * @param id identyfikator wizyty
     * @param userId identyfikator użytkownika
     * @return true jeśli usunięto wizytę, false w przeciwnym wypadku
     */
    public boolean deleteAppointment(Long id, Long userId) {
        Optional<Appointment> appointment = appointmentRepository.findById(id);

        if (appointment.isPresent() && appointment.get().getUser().getId().equals(userId)) {
            appointmentRepository.deleteById(id);
            return true;
        } else {
            return false;
        }
    }
}