package com.shoppinglist.springboot.Token;

import org.springframework.data.jpa.repository.JpaRepository;

public interface TokenResetRepository extends JpaRepository<PasswordResetToken, String> {

    PasswordResetToken findByUserEmail(String email);

    PasswordResetToken findByToken(String token);

}