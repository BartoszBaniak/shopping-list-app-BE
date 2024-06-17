package com.shoppinglist.springboot.user;

public class UserDTO {

    private final String id;
    private final String firstName;
    private final String lastName;
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