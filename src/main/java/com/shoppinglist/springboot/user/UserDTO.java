package com.shoppinglist.springboot.user;

import jakarta.annotation.Nullable;

import java.time.LocalDate;

public class UserDTO {

    private String id;
    private String firstName;
    private String lastName;
    private String email;


    public UserDTO(String id, String firstname, String lastname, String email) {
        this.id = id;
        this.firstName = firstname;
        this.lastName = lastname;
        this.email = email;
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

    public void setEmail(String email) {
        this.email = email;
    }
}