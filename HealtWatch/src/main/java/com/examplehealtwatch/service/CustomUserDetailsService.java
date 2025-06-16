package com.examplehealtwatch.service;

import com.examplehealtwatch.User;
import com.examplehealtwatch.repository.UserRespository;
import com.examplehealtwatch.CustomUserDetails;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Optional;

/**
 * Serwis implementujący UserDetailsService dla Spring Security.
 * Odpowiada za ładowanie szczegółów użytkownika na potrzeby uwierzytelniania.
 * Integruje się z repozytorium użytkowników.
 */
@Service
public class CustomUserDetailsService implements UserDetailsService {

    @Autowired
    private UserRespository repo;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        Optional<User> user = repo.findByEmail(email);

        if (user.isEmpty()) {
            throw new UsernameNotFoundException("User not found with email: " + email);
        }

        return new CustomUserDetails(user.get());
    }
}
