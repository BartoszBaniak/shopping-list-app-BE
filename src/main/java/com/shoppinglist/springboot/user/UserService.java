package com.shoppinglist.springboot.user;

import com.shoppinglist.springboot.Token.TokenRepository;
import com.shoppinglist.springboot.Token.TokenResetRepository;
import com.shoppinglist.springboot.Token.TokenService;
import com.shoppinglist.springboot.exceptions.ResourceNotFoundException;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCrypt;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.regex.Pattern;

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

    public List<User> getAllUsers() {
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

    private ResponseEntity<?> checkEmailExists(String email) {
        if (userDAO.existsUserWithEmail(email)) {
            logger.warn("Email already exists");
            ApiError error = new ApiError("Validation", "email", "Email already exists");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
        }
        return ResponseEntity.ok("Email checked.");
    }

    public ResponseEntity<?> passwordValidator(String password, String retPassword) {
        if (password.length() < 8 || password.length() > 32) {
            ApiError error = new ApiError("Validation", "password", "Password length should be between 8 and 32 characters");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
        }
        if (!password.matches(".*[a-z].*")) {
            ApiError error = new ApiError("Validation", "password", "Password should contain at least one lowercase letter");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
        }
        if (!password.matches(".*[A-Z].*")) {
            ApiError error = new ApiError("Validation", "password", "Password should contain at least one uppercase letter");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
        }
        if (!password.matches(".*[!@#$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>\\/?].*")) {
            ApiError error = new ApiError("Validation", "password", "Password should contain at least one special character");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
        }
        if (!password.matches(".*\\d.*")) {
            ApiError error = new ApiError("Validation", "password", "Password should contain at least one digit");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
        }
        if (password.contains(" ")) {
            ApiError error = new ApiError("Validation", "password", "Password should not contain spaces");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
        }
        if (!password.equals(retPassword)) {
            ApiError error = new ApiError("Validation", "retPassword", "Invalid retPassword");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
        }
        return ResponseEntity.ok("password is approved.");
    }

    private ResponseEntity<?> checkFullName(String firstname, String lastname) {
        if (firstname.length() > 50) {
            logger.warn("Invalid firstname");
            ApiError error = new ApiError("Validation", "firstname", "Invalid firstname");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
        }
        if (lastname.length() > 50) {
            logger.warn("Invalid lastname");
            ApiError error = new ApiError("Validation", "lastname", "Invalid lastname");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
        }
        return ResponseEntity.ok("fullname checked.");
    }

    public ResponseEntity<?> addUser(UserRegistrationRequest userRegistrationRequest) {
        final String regexPattern = "^(?=.{1,64}@)[A-Za-z0-9_-]+(\\.[A-Za-z0-9_-]+)*@" +
                "[^-][A-Za-z0-9-]+(\\.[A-Za-z0-9-]+)*(\\.[A-Za-z]{2,})$";
        if (userRegistrationRequest.firstName() == null || userRegistrationRequest.firstName() == "" || userRegistrationRequest.lastName() == "" ||
                userRegistrationRequest.lastName() == null || userRegistrationRequest.email() == null || userRegistrationRequest.email() == "" ||
                userRegistrationRequest.password() == null || userRegistrationRequest.password() == "" ||
                userRegistrationRequest.retPassword() == null || userRegistrationRequest.retPassword() == "") {
            logger.warn("Missing data");
            ApiError error = new ApiError("Validation", null, "Missing data");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
        }
        String firstname = userRegistrationRequest.firstName();
        String lastname = userRegistrationRequest.lastName();
        String email = userRegistrationRequest.email();
        String password = userRegistrationRequest.password();
        String retPassword = userRegistrationRequest.retPassword();
        ZonedDateTime currentZonedDateTime = ZonedDateTime.now();

        if (!checkEmailValid(email, regexPattern) || email.length() > 255) {
            logger.warn("Invalid email");
            ApiError error = new ApiError("Validation", "email", "Invalid email");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
        }
        ResponseEntity<?> checkFullNameResult = checkFullName(firstname, lastname);
        if (checkFullNameResult.getStatusCode() != HttpStatus.OK) {
            return checkFullNameResult;
        }
        ResponseEntity<?> passwordValidationResult = passwordValidator(password, retPassword);
        if (passwordValidationResult.getStatusCode() != HttpStatus.OK) {
            logger.warn("Invalid password");
            return passwordValidationResult;
        }
        ResponseEntity<?> checkEmailExistsResult = checkEmailExists(email);
        if (checkEmailExistsResult.getStatusCode() != HttpStatus.OK) {
            logger.warn("Email already in database");
            return checkEmailExistsResult;
        }

        String generatedSecuredPasswordHash = BCrypt.hashpw(password, BCrypt.gensalt(12));
        User user = new User(firstname, lastname, email, generatedSecuredPasswordHash);
        userDAO.addUser(user);

        String refreshToken = tokenService.generateToken(EXPIRATION_TIME_REFRESH, user.getId());
        logger.info("Account created");
        return ResponseEntity.ok("Account activated successfully.");
    }

    private boolean checkEmailValid(String email, String emailRegex) {
        return Pattern.compile(emailRegex)
                .matcher(email)
                .matches();
    }

    public ResponseEntity<?> updateUser(String uuid, UserUpdateRequest userUpdateRequest, HttpServletRequest request) {
        ResponseEntity<?> checkAuthorizationResult = checkAuthorization(request);
        if (checkAuthorizationResult.getStatusCode() != HttpStatus.OK) {
            ApiError error = new ApiError("General", null, "Missing authorization");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
        }
        User user = userDAO.getUserById(uuid)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Customer with id [%s] not found".formatted(uuid))
                );
        var userId = getUserIDFromAccessToken(request);
        if (!uuid.equals(userId)) {
            ApiError error = new ApiError("Access", null, "Access denied");
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

    public ResponseEntity<?> checkAuthorization(HttpServletRequest request) {
        if (checkAuthorizationHeader(request)) {
            try {
                if (checkLoggedUser(request)) {
                    return ResponseEntity.ok("Account logged in successfully.");
                } else {
                    ApiError error = new ApiError("Refresh", null, "Access token expired");
                    return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
                }
            } catch (Exception e) {
                ApiError error = new ApiError("Access", null, "Invalid token");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
            }
        } else {
            ApiError error = new ApiError("Access", null, "Not logged in");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
        }
    }

    boolean checkAuthorizationHeader(HttpServletRequest request) {
        String authorizationHeader = request.getHeader("Authorization");
        return authorizationHeader != null && authorizationHeader.startsWith("Bearer ");
    }

    boolean checkLoggedUser(HttpServletRequest request) {
        String authorizationHeader = request.getHeader("Authorization");
        if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
            String jwtToken = authorizationHeader.substring(7);
            return tokenService.validateAccessToken(jwtToken);
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

    public ResponseEntity<?> getUserDetails(String uuid, UserService userService, HttpServletRequest request) {
        ResponseEntity<?> checkAuthorizationResult = checkAuthorization(request);
        if (checkAuthorizationResult.getStatusCode() != HttpStatus.OK) {
            ApiError error = new ApiError("General", null, "Missing authorization");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
        }
        var user = userService.getUserById(uuid);
        var userId = userService.getUserIDFromAccessToken(request);
        var userDTO = new UserDTO(user.getId(), user.getFirstname(), user.getLastname(), user.getEmail());
        if (!uuid.equals(userId)) {
            userDTO.setEmail(null);
        }
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_JSON)
                .body(userDTO);
    }

}