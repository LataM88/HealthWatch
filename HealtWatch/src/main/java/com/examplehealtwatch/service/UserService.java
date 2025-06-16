package com.examplehealtwatch.service;

import com.examplehealtwatch.User;
import com.examplehealtwatch.repository.UserRespository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

/**
 * Serwis odpowiedzialny za logikę biznesową użytkowników.
 * Zarządza rejestracją, pobieraniem i aktualizacją danych użytkowników.
 * Integruje się z repozytorium oraz obsługuje szyfrowanie haseł.
 */
@Service
public class UserService {

    @Autowired
    private UserRespository userRepository;

    @Autowired
    private BCryptPasswordEncoder passwordEncoder;

    /**
     * Rejestruje nowego użytkownika w systemie.
     * Sprawdza, czy użytkownik o podanym e-mailu już istnieje, a następnie zapisuje nowego użytkownika z zaszyfrowanym hasłem.
     * @param user obiekt użytkownika
     * @return true jeśli rejestracja się powiodła, false jeśli użytkownik już istnieje
     */
    public boolean registerUser(User user) {
        Optional<User> existingUser = userRepository.findByEmail(user.getEmail());
        if (existingUser.isPresent()) {
            return false;
        }

        try {
            user.setPassword(passwordEncoder.encode(user.getPassword()));
            userRepository.save(user);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
}