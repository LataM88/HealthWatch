package com.examplehealtwatch.repository;

import com.examplehealtwatch.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

/**
 * Repozytorium do zarządzania encjami User.
 * Pozwala na operacje CRUD na użytkownikach oraz wyszukiwanie po wybranych polach.
 * Integruje się z systemem uwierzytelniania i autoryzacji.
 */
public interface UserRespository extends JpaRepository<User, Long> {
    @Query("SELECT u FROM User u WHERE u.email =?1")
    Optional<User> findByEmail(String email);
}
