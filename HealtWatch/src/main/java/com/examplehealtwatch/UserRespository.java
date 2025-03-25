package com.examplehealtwatch;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface UserRespository extends JpaRepository<User, Long> {
    @Query("SELECT u FROM User u WHERE u.email =?1")
    Optional<User> findByEmail(String email);
}
