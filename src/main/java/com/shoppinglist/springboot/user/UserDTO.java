package com.shoppinglist.springboot.user;

import jakarta.annotation.Nullable;

import java.time.LocalDate;

public class UserDTO {

    private String id;
    private String firstName;
    private String lastName;
    private String email;
    private LocalDate birthDate;

    public UserDTO(String id, String firstname, String lastname, String email, @Nullable LocalDate birthDate) {
        this.id = id;
        this.firstName = firstname;
        this.lastName = lastname;
        this.email = email;
        this.birthDate = birthDate;
    }

    public String getId() {
        return id;
    }

    public String getFirstname() {
        return firstName;
    }

    public String getLastname() {
        return lastName;
    }

    public String getEmail() {
        return email;
    }

    public LocalDate getBirthDate() {
        return birthDate;
    }

    public void setBirthDate(LocalDate birthDate) {
        this.birthDate = birthDate;
    }

    public void setEmail(String email) {
        this.email = email;
    }
}