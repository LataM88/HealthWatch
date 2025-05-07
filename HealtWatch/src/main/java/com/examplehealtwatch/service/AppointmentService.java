package com.examplehealtwatch.service;

import com.examplehealtwatch.Appointment;
import com.examplehealtwatch.AppointmentRepository;
import com.examplehealtwatch.User;
import com.examplehealtwatch.UserRespository;
import com.examplehealtwatch.request.AppointmentRequest;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class AppointmentService {

    @Autowired
    private AppointmentRepository appointmentRepository;

    @Autowired
    private UserRespository userRepository;

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