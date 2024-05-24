package com.shoppinglist.springboot.user;

import com.shoppinglist.springboot.exceptions.DuplicateResourceException;
import com.shoppinglist.springboot.exceptions.NotValidResourceException;
import com.shoppinglist.springboot.exceptions.ResourceNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.security.crypto.bcrypt.BCrypt;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.regex.Pattern;

@Service
public class UserService {
    private final UserDAO userDAO;

    @Autowired
    public UserService(UserDAO userDAO) {
        this.userDAO = userDAO;
    }

    public List < User > getAllUsers() {
        return userDAO.getAllUsers();
    }

    public User getUserById(Integer id) {
        return userDAO.getUserById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException("User with id [%s] not found".formatted(id))
                );
    }
    public ResponseEntity < ? > passwordValidator(String password) {
        if (password.length() < 8 || password.length() > 32) {
            Error error = new Error("Weryfikacja", "password", "Password length should be between 8 and 32 characters");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
        }

        if (!password.matches(".*[a-z].*")) {
            Error error = new Error("Weryfikacja", "password", "Password should contain at least one lowercase letter");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
        }

        if (!password.matches(".*[A-Z].*")) {
            Error error = new Error("Weryfikacja", "password", "Password should contain at least one uppercase letter");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
        }

        if (!password.matches(".*[!@#$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>\\/?].*")) {
            Error error = new Error("Weryfikacja", "password", "Password should contain at least one special character");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
        }

        if (!password.matches(".*\\d.*")) {
            Error error = new Error("Weryfikacja", "password", "Password should contain at least one digit");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
        }

        if (password.contains(" ")) {
            Error error = new Error("Weryfikacja", "password", "Password should not contain spaces");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
        }

        return ResponseEntity.ok("password is approved.");
    }
    public User getUserByEmail(String email) {
        return userDAO.getUserByEmail(email)
                .orElseThrow(() ->
                        new ResourceNotFoundException("User with email [%s] not found".formatted(email))
                );
    }

    private ResponseEntity < ? > checkFullName(String firstname, String lastname) {
        if (firstname.length() > 50) {
            Error error = new Error("Weryfikacja", "firstname", "Invalid firstname");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
        }
        if (lastname.length() > 50) {
            Error error = new Error("Weryfikacja", "lastname", "Invalid lastname");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
        }
        return ResponseEntity.ok("fullname checked.");
    }
    private ResponseEntity < ? > checkEmailExists(String email) {
        if (userDAO.existsUserWithEmail(email)) {
            throw new DuplicateResourceException("Email already taken");
        }
        return null;
    }

    public ResponseEntity < ? > addUser(UserRegistrationRequest userRegistrationRequest) {
        final String regexPattern = "^(?=.{1,64}@)[A-Za-z0-9_-]+(\\.[A-Za-z0-9_-]+)*@" +
                "[^-][A-Za-z0-9-]+(\\.[A-Za-z0-9-]+)*(\\.[A-Za-z]{2,})$";
        if (userRegistrationRequest.firstname() == null || userRegistrationRequest.firstname() == "" || userRegistrationRequest.lastname() == "" ||
                userRegistrationRequest.lastname() == null || userRegistrationRequest.email() == null || userRegistrationRequest.email() == "" ||
                userRegistrationRequest.password() == null || userRegistrationRequest.password() == "" ||
                userRegistrationRequest.birthDate() == null || userRegistrationRequest.isActive() == null) {
            Error error = new Error("Weryfikacja", null, "Missing data");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
        }
        String firstname = userRegistrationRequest.firstname();
        String lastname = userRegistrationRequest.lastname();
        String email = userRegistrationRequest.email();
        String password = userRegistrationRequest.password();
        LocalDate birthDate = userRegistrationRequest.birthDate();
        Boolean isActive = userRegistrationRequest.isActive();
        ZonedDateTime currentZonedDateTime = ZonedDateTime.now();
        ZonedDateTime birthDateWithZonedDateTime = birthDate.atStartOfDay(currentZonedDateTime.getZone());
        int age = currentZonedDateTime.getYear() - birthDateWithZonedDateTime.getYear();

        if (birthDateWithZonedDateTime.isAfter(currentZonedDateTime)) {
            Error error = new Error("Weryfikacja", "birthdate", "Date of birth cannot be in the future");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
        }
        if (currentZonedDateTime.getMonthValue() < birthDateWithZonedDateTime.getMonthValue() ||
                (currentZonedDateTime.getMonthValue() == birthDateWithZonedDateTime.getMonthValue() &&
                        currentZonedDateTime.getDayOfMonth() < birthDateWithZonedDateTime.getDayOfMonth())) {
            age--;
        }
        //Confirmation that he is at least 13 years old in that day
        if (age < 13) {
            Error error = new Error("Weryfikacja", "birthdate", "Too young");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
        }

        if (!checkEmailValid(email, regexPattern) || email.length() > 255) {
            Error error = new Error("Weryfikacja", "email", "Invalid email");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
        }
        ResponseEntity < ? > checkFullNameResult = checkFullName(firstname, lastname);
        if (checkFullNameResult.getStatusCode() != HttpStatus.OK) {
            return checkFullNameResult;
        }
        ResponseEntity < ? > passwordValidationResult = passwordValidator(password);
        if (passwordValidationResult.getStatusCode() != HttpStatus.OK) {
            return passwordValidationResult;
        }
        ResponseEntity < ? > checkEmailExistsResult = checkEmailExists(email);
        if (checkEmailExistsResult.getStatusCode() != HttpStatus.OK) {
            return checkEmailExistsResult;
        }

        //Encrypting password
        String generatedSecuredPasswordHash = BCrypt.hashpw(password, BCrypt.gensalt(12));
        User user = new User(firstname, lastname, email, generatedSecuredPasswordHash, birthDate, isActive);
        userDAO.addUser(user);
        return ResponseEntity.ok("Account activated successfully.");
    }

    public void deleteUser(Integer id) {
        User user = userDAO.getUserById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Customer with id [%s] not found".formatted(id))
                );

        userDAO.deleteUser(user);
    }

    private boolean checkEmailValid(String email, String emailRegex) {
        return Pattern.compile(emailRegex)
                .matcher(email)
                .matches();
    }

    public void updateUser(Integer id, UserUpdateRequest userUpdateRequest) {
        User user = userDAO.getUserById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException("User with id [%s] not found".formatted(id))
                );

        if (userUpdateRequest.firstname() != null) {
            String firstname = userUpdateRequest.firstname();
            user.setFirstname(firstname);
        }

        if (userUpdateRequest.lastname() != null) {
            String lastname = userUpdateRequest.lastname();
            user.setLastname(lastname);
        }

        if (userUpdateRequest.email() != null && !user.getEmail().equals(userUpdateRequest.email())) {
            String email = userUpdateRequest.email();
            checkEmailExists(email);
            if (!checkEmailValid(email, "^(.+)@(\\S+)$")) {
                throw new NotValidResourceException("Email not valid");
            }
            user.setEmail(email);
        }

        userDAO.updateUser(user);
    }

}