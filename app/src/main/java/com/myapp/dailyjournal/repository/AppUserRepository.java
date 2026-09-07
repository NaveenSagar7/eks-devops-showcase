package com.myapp.dailyjournal.repository;

import com.myapp.dailyjournal.model.AppUser;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AppUserRepository extends JpaRepository<AppUser, Long> {
    Optional<AppUser> findByPanHash(String panHash);
}
