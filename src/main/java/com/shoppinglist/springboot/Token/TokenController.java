package com.shoppinglist.springboot.Token;

import com.shoppinglist.springboot.user.ApiError;
import com.shoppinglist.springboot.user.User;
import com.shoppinglist.springboot.user.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("api/auth")
public class TokenController {
    private static final Logger logger = LoggerFactory.getLogger(TokenController.class);
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
    public ResponseEntity<?> login(@RequestBody LoginRequest requestBody) {
        if ((requestBody.email() == null && requestBody.password() == null) || (requestBody.email() == "" && requestBody.password() == "")) {
            logger.warn("Missing e-mail and password");
            ApiError error = new ApiError("Validation", "Both", "Missing data");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
        } else if (requestBody.email() == null || requestBody.email() == "") {
            logger.warn("Missing e-mail");
            ApiError error = new ApiError("Validation", "E-mail", "Missing e-mail");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
        } else if (requestBody.password() == null || requestBody.password() == "") {
            logger.warn("Missing password");
            ApiError error = new ApiError("Validation", "Password", "Missing password");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
        } else {
            logger.info("User logged");
            return tokenService.loginUser(requestBody.email(), requestBody.password(), userService);
        }
    }

    @GetMapping("refresh")
    public ResponseEntity<?> refresh(@CookieValue(value = "refreshToken", defaultValue = "") String refreshToken) {
        if (refreshToken == null) {
            ApiError error = new ApiError("Refresh", null, "No token");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
        } else {
            return tokenService.refreshToken(refreshToken);
        }
    }

    @DeleteMapping("logout")
    public ResponseEntity<?> logout(@CookieValue(value = "refreshToken", defaultValue = "") String refreshToken) {
        if (!refreshToken.isEmpty()) {
            try {
                tokenService.deleteToken(refreshToken);
                ResponseCookie cookie = ResponseCookie.from("refreshToken", "")
                        .httpOnly(true)
                        .secure(true)
                        .path("/")
                        .maxAge(0)
                        .build();

                HttpHeaders headers = new HttpHeaders();
                headers.add(HttpHeaders.SET_COOKIE, cookie.toString());

                return ResponseEntity.ok()
                        .headers(headers)
                        .build();
            } catch (Exception e) {
                ApiError error = new ApiError("logout", "", "error during logout");
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
            }
        } else {
            ApiError error = new ApiError("logout", "", "missing cookie");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
        }
    }

    @DeleteMapping("logout-all")
    public ResponseEntity<?> logoutAll(@CookieValue(value = "refreshToken", defaultValue = "") String refreshToken) {
        if (!refreshToken.isEmpty()) {
            try {
                String userId = tokenService.getUserIdFromToken(refreshToken);
                tokenService.deleteAllTokens(userId);

                ResponseCookie cookie = ResponseCookie.from("refreshToken", "")
                        .httpOnly(true)
                        .secure(true)
                        .path("/")
                        .maxAge(0)
                        .build();

                HttpHeaders headers = new HttpHeaders();
                headers.add(HttpHeaders.SET_COOKIE, cookie.toString());

                return ResponseEntity.ok()
                        .headers(headers)
                        .build();
            } catch (Exception e) {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
            }
        } else {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
    }

}