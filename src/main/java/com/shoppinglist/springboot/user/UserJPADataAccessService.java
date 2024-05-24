package com.shoppinglist.springboot.user;

import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class UserJPADataAccessService implements UserDAO {
    private final UserRepository userRepository;

    public UserJPADataAccessService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public List < User > getAllUsers() {
        return userRepository.findAll();
    }

    @Override
    public void addUser(User user) {
        userRepository.save(user);
    }

    @Override
    public Optional < User > getUserById(Integer id) {
        return userRepository.findById(id);
    }

    @Override
    public Optional < User > getUserByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    @Override
    public boolean existsUserWithEmail(String email) {
        return userRepository.existsByEmail(email);
    }

    @Override
    public void deleteUser(User user) {
        userRepository.delete(user);
    }

    @Override
    public void updateUser(User user) {
        userRepository.save(user);
    }

    @Override
    public Optional < User > getById(Integer userId) {
        return userRepository.findById(userId);
    }

    @Override
    public boolean existsById(Integer userId) {
        return userRepository.existsById(userId);
    }
}