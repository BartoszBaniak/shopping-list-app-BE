package com.shoppinglist.springboot.user;


public record UserRegistrationRequest(
        String firstName, String lastName, String email, String password, String retPassword
) {
}