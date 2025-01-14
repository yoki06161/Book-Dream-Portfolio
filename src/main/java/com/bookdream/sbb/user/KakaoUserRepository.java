package com.bookdream.sbb.user;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

public interface KakaoUserRepository extends JpaRepository<KakaoUser, Long> {
    Optional<KakaoUser> findByEmail(String email);
    boolean existsByEmail(String email);
    boolean existsByUsername(String username);
}