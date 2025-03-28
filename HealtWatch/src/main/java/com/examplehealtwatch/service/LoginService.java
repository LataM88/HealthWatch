package com.examplehealtwatch.service;

import com.examplehealtwatch.User;
import com.examplehealtwatch.UserRespository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Service
public class LoginService {

    @Autowired
    private UserRespository userRepository;

    @Autowired
    private BCryptPasswordEncoder passwordEncoder;

    private Map<String, Long> apiKeys = new HashMap<>();

    public Map<String, Object> login(String email, String password) {
        Map<String, Object> response = new HashMap<>();

        Optional<User> userOptional = userRepository.findByEmail(email);

        if (userOptional.isEmpty()) {
            response.put("status", "error");
            response.put("message", "User not found");
            return response;
        }

        User user = userOptional.get();

        if (!passwordEncoder.matches(password, user.getPassword())) {
            response.put("status", "error");
            response.put("message", "Invalid credentials");
            return response;
        }

        String apiKey = generateApiKey();
        apiKeys.put(apiKey, user.getId());

        response.put("status", "succes");
        response.put("message", "Login successful");
        response.put("name", user.getName());
        response.put("surname", user.getSurname());
        response.put("email", user.getEmail());
        response.put("apiKey", apiKey);

        return response;
    }

    private String generateApiKey() {
        SecureRandom random = new SecureRandom();
        byte[] bytes = new byte[32];
        random.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    public boolean validateApiKey(String apiKey) {
        System.out.println("Walidacja klucza API: " + apiKey);
        return apiKeys.containsKey(apiKey);
    }

    public Long getUserIdFromApiKey(String apiKey) {
        return apiKeys.get(apiKey);
    }
}