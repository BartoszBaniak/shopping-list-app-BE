package com.shoppinglist.springboot.user;

import java.util.List;
import java.util.Optional;

public interface UserDAO {
    List<User> getAllUsers();

    void addUser(User user);

    Optional<User> getUserById(String id);

    Optional<User> getUserByEmail(String email);

    boolean existsUserWithEmail(String email);

    void deleteUser(User user);

    void updateUser(User user);

    Optional<User> getById(String userId);

    boolean existsById(String userId);
}