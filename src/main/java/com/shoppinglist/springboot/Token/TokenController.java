package com.shoppinglist.springboot.Token;

import com.shoppinglist.springboot.user.User;
import com.shoppinglist.springboot.user.Error;
import com.shoppinglist.springboot.user.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("api/auth")
public class TokenController {
    private final UserService userService;
    private final TokenService tokenService;

    public TokenController(UserService userService, TokenService tokenService) {
        this.userService = userService;
        this.tokenService = tokenService;
    }

    public User getUserByEmail(@PathVariable("email") String email) {
        return userService.getUserByEmail(email);
    }

    @PostMapping("login")
    public ResponseEntity < ? > login(@RequestBody LoginRequest requestBody) {
        if ((requestBody.email() == null && requestBody.password() == null) || (requestBody.email() == "" && requestBody.password() == "")) {
            Error error = new Error("Validation", "Both", "Missing data");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
        } else if (requestBody.email() == null || requestBody.email() == "") {
            Error error = new Error("Validation", "E-mail", "Missing e-mail");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
        } else if (requestBody.password() == null || requestBody.password() == "") {
            Error error = new Error("Validation", "Password", "Missing password");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
        } else {
            return tokenService.loginUser(requestBody.email(), requestBody.password(), userService);
        }
    }

    @GetMapping("refresh")
    public ResponseEntity < ? > refresh(@CookieValue(value = "refreshToken", defaultValue = "") String refreshToken) {
        if (refreshToken == null) {
            Error error = new Error("Refresh", null, "No token");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
        } else {
            return tokenService.refreshToken(refreshToken);
        }
    }

    @DeleteMapping("logout")
    public ResponseEntity < ? > logout(@CookieValue(value = "refreshToken", defaultValue = "") String refreshToken) {
        if (!refreshToken.isEmpty()) {
            try {
                // Usuwanie tokenu
                tokenService.deleteToken(refreshToken);
                // Usuwanie ciasteczka
                return ResponseEntity.ok().build();
            } catch (Exception e) {
                Error error = new Error("logout", "", "error during logout");
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
            }
        } else {
            // Brak ciasteczka
            Error error = new Error("logout", "", "missing cookie");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
        }
    }

    @DeleteMapping("logout-all")
    public ResponseEntity < ? > logoutAll(@CookieValue(value = "refreshToken", defaultValue = "") String refreshToken) {
        if (!refreshToken.isEmpty()) {
            try {
                Integer userId = tokenService.getUserIdFromToken(refreshToken);
                // Usunięcie wszystkich tokenów użytkownika z bazy danych
                tokenService.deleteAllTokens(userId);
                return ResponseEntity.ok().build();
            } catch (Exception e) {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
            }
        } else {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
    }
}