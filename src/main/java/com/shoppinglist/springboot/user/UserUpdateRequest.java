package com.shoppinglist.springboot.user;

public record UserUpdateRequest
        (
                String firstname, String lastname, String email, String address, String phoneNumber
        ) {}