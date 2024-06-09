package com.shoppinglist.springboot.user;

import com.shoppinglist.springboot.Token.TokenRepository;
import com.shoppinglist.springboot.Token.TokenResetRepository;
import com.shoppinglist.springboot.Token.TokenService;
import com.shoppinglist.springboot.exceptions.DuplicateResourceException;
import com.shoppinglist.springboot.exceptions.NotValidResourceException;
import com.shoppinglist.springboot.exceptions.ResourceNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.security.crypto.bcrypt.BCrypt;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.regex.Pattern;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;

import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;



@Service
public class UserService {
    private static final Logger logger = LoggerFactory.getLogger(UserService.class);
    private final UserDAO userDAO;
    @Autowired
    TokenResetRepository tokenResetRepository;
    private static final long EXPIRATION_TIME_REFRESH = 3600000 * 24;

    @Autowired
    TokenService tokenService;
    @Autowired
    private TokenRepository tokenRepository;
    @Autowired
    private UserRepository userRepository;

    @Autowired
    public UserService(UserDAO userDAO, TokenService tokenService) {
        this.userDAO = userDAO;
        this.tokenService = tokenService;
    }

    public List < User > getAllUsers() {
        return userDAO.getAllUsers();
    }

    public User getUserById(String id) {
        return userDAO.getUserById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Customer with id [%s] not found".formatted(id))
                );
    }

    public User getUserByEmail(String email) {
        return userDAO.getUserByEmail(email)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Customer with email [%s] not found".formatted(email))
                );
    }

    private ResponseEntity < ? > checkEmailExists(String email) {
        if (userDAO.existsUserWithEmail(email)) {
            logger.warn("Email already exists");
            Error error = new Error("Validation", "email", "Email already exists");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
        }
        return ResponseEntity.ok("Email checked.");
    }

    public ResponseEntity < ? > passwordValidator(String password) {
        if (password.length() < 8 || password.length() > 32) {
            Error error = new Error("Validation", "password", "Password length should be between 8 and 32 characters");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
        }
        if (!password.matches(".*[a-z].*")) {
            Error error = new Error("Validation", "password", "Password should contain at least one lowercase letter");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
        }
        if (!password.matches(".*[A-Z].*")) {
            Error error = new Error("Validation", "password", "Password should contain at least one uppercase letter");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
        }
        if (!password.matches(".*[!@#$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>\\/?].*")) {
            Error error = new Error("Validation", "password", "Password should contain at least one special character");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
        }
        if (!password.matches(".*\\d.*")) {
            Error error = new Error("Validation", "password", "Password should contain at least one digit");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
        }
        if (password.contains(" ")) {
            Error error = new Error("Validation", "password", "Password should not contain spaces");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
        }
        return ResponseEntity.ok("password is approved.");
    }

    private ResponseEntity < ? > checkFullName(String firstname, String lastname) {
        if (firstname.length() > 50) {
            logger.warn("Invalid firstname");
            Error error = new Error("Validation", "firstname", "Invalid firstname");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
        }
        if (lastname.length() > 50) {
            logger.warn("Invalid lastname");
            Error error = new Error("Validation", "lastname", "Invalid lastname");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
        }
        return ResponseEntity.ok("fullname checked.");
    }


    public ResponseEntity < ? > addUser(UserRegistrationRequest userRegistrationRequest) {
        final String regexPattern = "^(?=.{1,64}@)[A-Za-z0-9_-]+(\\.[A-Za-z0-9_-]+)*@" +
                "[^-][A-Za-z0-9-]+(\\.[A-Za-z0-9-]+)*(\\.[A-Za-z]{2,})$";
        if (userRegistrationRequest.firstname() == null || userRegistrationRequest.firstname() == "" || userRegistrationRequest.lastname() == ""
                || userRegistrationRequest.lastname() == null || userRegistrationRequest.email() == null || userRegistrationRequest.email() == ""
                || userRegistrationRequest.password() == null || userRegistrationRequest.password() == ""
                || userRegistrationRequest.birthDate() == null) {
            logger.warn("Missing data");
            Error error = new Error("Validation", null, "Missing data");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
        }
        String firstname = userRegistrationRequest.firstname();
        String lastname = userRegistrationRequest.lastname();
        String email = userRegistrationRequest.email();
        String password = userRegistrationRequest.password();
        LocalDate birthDate = userRegistrationRequest.birthDate();
        ZonedDateTime currentZonedDateTime = ZonedDateTime.now();
        ZonedDateTime birthDateWithZonedDateTime = birthDate.atStartOfDay(currentZonedDateTime.getZone());
        int age = currentZonedDateTime.getYear() - birthDateWithZonedDateTime.getYear();

        if (birthDateWithZonedDateTime.isAfter(currentZonedDateTime)) {
            logger.warn("Date of birth cannot be in the future");
            Error error = new Error("Validation", "birthdate", "Date of birth cannot be in the future");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
        }
        if (currentZonedDateTime.getMonthValue() < birthDateWithZonedDateTime.getMonthValue() ||
                (currentZonedDateTime.getMonthValue() == birthDateWithZonedDateTime.getMonthValue() &&
                        currentZonedDateTime.getDayOfMonth() < birthDateWithZonedDateTime.getDayOfMonth())) {
            age--;
        }
        //Confirmation that he is at least 13 years old in that day
        if (age < 13) {
            logger.warn("Too young");
            Error error = new Error("Validation", "birthdate", "Too young");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
        }

        if (!checkEmailValid(email, regexPattern) || email.length() > 255) {
            logger.warn("Invalid email");
            Error error = new Error("Validation", "email", "Invalid email");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
        }
        ResponseEntity < ? > checkFullNameResult = checkFullName(firstname, lastname);
        if (checkFullNameResult.getStatusCode() != HttpStatus.OK) {
            return checkFullNameResult;
        }
        ResponseEntity < ? > passwordValidationResult = passwordValidator(password);
        if (passwordValidationResult.getStatusCode() != HttpStatus.OK) {
            logger.warn("Invalid password");
            return passwordValidationResult;
        }
        ResponseEntity < ? > checkEmailExistsResult = checkEmailExists(email);
        if (checkEmailExistsResult.getStatusCode() != HttpStatus.OK) {
            logger.warn("Email already in database");
            return checkEmailExistsResult;
        }

        //Encrypting password
        String generatedSecuredPasswordHash = BCrypt.hashpw(password, BCrypt.gensalt(12));
        User user = new User(firstname, lastname, email, generatedSecuredPasswordHash, birthDate, false);
        userDAO.addUser(user);
        logger.info("Account created");
        return ResponseEntity.ok("Account activated successfully.");
    }

    @Transactional
    public ResponseEntity<?> deleteUser(String id, HttpServletRequest request) {
        ResponseEntity<?> checkAuthorizationResult = checkAuthorization(request);
        if (checkAuthorizationResult.getStatusCode() != HttpStatus.OK) {
            Error error = new Error("General", null, "Missing authorization");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
        }

        String userId = getUserIDFromAccessToken(request);

        // Ensure the logged-in user is deleting their own account
        if (!id.equals(userId)) {
            Error error = new Error("Forbidden", null, "Access denied");
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(error);
        }

        Optional<User> optionalUser = userDAO.getUserById(id);
        if (!optionalUser.isPresent()) {
            Error error = new Error("Not Found", null, "User not found");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
        }
        User user = optionalUser.get();
        userDAO.deleteUser(user);
        HttpHeaders headers = new HttpHeaders();
        tokenService.logoutAllSessions(id, headers);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }


    private boolean checkEmailValid(String email, String emailRegex) {
        return Pattern.compile(emailRegex)
                .matcher(email)
                .matches();
    }

    public ResponseEntity < ? > updateUser(String uuid, UserUpdateRequest userUpdateRequest, HttpServletRequest request) {
        ResponseEntity < ? > checkAuthorizationResult = checkAuthorization(request);
        if (checkAuthorizationResult.getStatusCode() != HttpStatus.OK) {
            Error error = new Error("General", null, "Missing authorization");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
        }
        User user = userDAO.getUserById(uuid)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Customer with id [%s] not found".formatted(uuid))
                );
        var userId = getUserIDFromAccessToken(request);
        //uuid - id of requested user, userId - id of user logged in
        if (!uuid.equals(userId)) {
            Error error = new Error("Access", null, "Access denied");
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(error);
        }
        if (userUpdateRequest.firstname() != null) {
            String firstname = userUpdateRequest.firstname();
            user.setFirstname(firstname);
        }

        if (userUpdateRequest.lastname() != null) {
            String lastname = userUpdateRequest.lastname();
            user.setLastname(lastname);
        }

        if (userUpdateRequest.birthDate() != null) {
            LocalDate birthDate = userUpdateRequest.birthDate();
            user.setBirthDate(birthDate);
        }

        userDAO.updateUser(user);
        return ResponseEntity.ok("User updated successfully");
    }

    public boolean hasExpired(LocalDateTime expiryDateTime) {
        LocalDateTime currentDateTime = LocalDateTime.now();
        return expiryDateTime.isAfter(currentDateTime);
    }

    private String getFileExtension(String filename) {
        int lastDotIndex = filename.lastIndexOf('.');
        if (lastDotIndex != -1) {
            return filename.substring(lastDotIndex + 1);
        }
        return null;
    }


    public ResponseEntity < ? > changePassword(String userId, ChangePasswordRequest changePasswordRequest) {
        User user = getUserById(userId);
        String currentPassword = changePasswordRequest.currentPassword();
        String newPassword = changePasswordRequest.newPassword();
        if (!BCrypt.checkpw(currentPassword, user.getPassword())) {
            Error error = new Error("Validation", "Password", "Invalid password");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
        }
        ResponseEntity < ? > passwordValidationResult = passwordValidator(newPassword);
        if (passwordValidationResult.getStatusCode() != HttpStatus.OK) {
            return passwordValidationResult;
        }
        String hashedNewPassword = BCrypt.hashpw(newPassword, BCrypt.gensalt(12));
        user.setPassword(hashedNewPassword);
        userDAO.updateUser(user);
        tokenService.deleteAllTokens(userId);
        return ResponseEntity.ok().build();
    }

    public ResponseEntity < ? > checkAuthorization(HttpServletRequest request) {
        if (checkAuthorizationHeader(request)) {
            try {
                if (checkLoggedUser(request)) {
                    return ResponseEntity.ok("Account logged in successfully.");
                } else {
                    Error error = new Error("Refresh", null, "Access token expired");
                    return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
                }
            } catch (Exception e) {
                Error error = new Error("Access", null, "Invalid token");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
            }
        } else {
            Error error = new Error("Access", null, "Not logged in");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
        }
    }

    boolean checkAuthorizationHeader(HttpServletRequest request) {
        String authorizationHeader = request.getHeader("Authorization");
        if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
            return true;
        }
        return false;
    }

    boolean checkLoggedUser(HttpServletRequest request) {
        String authorizationHeader = request.getHeader("Authorization");
        if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
            String jwtToken = authorizationHeader.substring(7);
            if (tokenService.validateAccessToken(jwtToken)) {
                return true;
            }
        }
        return false;
    }

    public String getUserIDFromAccessToken(HttpServletRequest request) {
        String authorizationHeader = request.getHeader("Authorization");
        if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
            String jwtToken = authorizationHeader.substring(7);
            if (tokenService.validateAccessToken(jwtToken)) {
                return tokenService.getUserIdFromToken(jwtToken);
            }
        }
        return null;
    }

    public ResponseEntity < ? > getUserDetails(String uuid, UserService userService, HttpServletRequest request) {
        //Check if user is logged in
        ResponseEntity < ? > checkAuthorizationResult = checkAuthorization(request);
        if (checkAuthorizationResult.getStatusCode() != HttpStatus.OK) {
            Error error = new Error("General", null, "Missing authorization");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
        }
        var user = userService.getUserById(uuid);
        var userId = userService.getUserIDFromAccessToken(request);
        var userDTO = new UserDTO(user.getId(), user.getFirstname(), user.getLastname(), user.getEmail(), user.getBirthDate());
        //uuid - id of requested user, userId - id of user logged in
        if (!uuid.equals(userId)) {
            userDTO.setBirthDate(null);
            userDTO.setEmail(null);
        }
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_JSON)
                .body(userDTO);
    }

}