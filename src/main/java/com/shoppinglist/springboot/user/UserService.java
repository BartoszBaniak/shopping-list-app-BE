package com.shoppinglist.springboot.user;

import com.shoppinglist.springboot.exceptions.DuplicateResourceException;
import com.shoppinglist.springboot.exceptions.NotValidResourceException;
import com.shoppinglist.springboot.exceptions.ResourceNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.regex.Pattern;

public class UserService
{
    private final UserDAO userDAO;

    @Autowired
    public UserService(UserDAO userDAO) {
        this.userDAO = userDAO;
    }

    public List<User> getAllUsers() {
        return userDAO.getAllUsers();
    }

    public User getUserById(Integer id) {
        return userDAO.getUserById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException("User with id [%s] not found".formatted(id))
                );
    }

    public User getUserByEmail(String email) {
        return userDAO.getUserByEmail(email)
                .orElseThrow(() ->
                        new ResourceNotFoundException("User with email [%s] not found".formatted(email))
                );
    }

    private void checkEmailExists(String email) {
        if (userDAO.existsUserWithEmail(email)) {
            throw new DuplicateResourceException("Email already taken");
        }
    }

    public void addUser(UserRegistrationRequest userRegistrationRequest) {
        if (userRegistrationRequest.firstname() == null || userRegistrationRequest.lastname() == null || userRegistrationRequest.email() == null) {
            throw new NotValidResourceException("Missing data");
        }

        String firstname = userRegistrationRequest.firstname();
        String lastname = userRegistrationRequest.lastname();
        String email = userRegistrationRequest.email();
        checkEmailExists(email);
        String address = userRegistrationRequest.address();
        String phoneNumber = userRegistrationRequest.phoneNumber();

        User user = new User(firstname, lastname, email, address, phoneNumber);
        userDAO.addUser(user);
    }

    public void deleteUser(Integer id) {
        User user = userDAO.getUserById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException("User with id [%s] not found".formatted(id))
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

        if (userUpdateRequest.address() != null) {
            String address = userUpdateRequest.address();
            user.setAddress(address);
        }

        if (userUpdateRequest.phoneNumber() != null) {
            String phoneNumber = userUpdateRequest.phoneNumber();
            user.setPhoneNumber(phoneNumber);
        }

        userDAO.updateUser(user);
    }

}
