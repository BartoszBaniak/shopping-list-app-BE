package com.shoppinglist.springboot.user;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, String> {
    boolean existsByEmail(String email);

    Optional<User> findById(String id);

    Optional<User> findByEmail(String email);

    User getById(String id);

}