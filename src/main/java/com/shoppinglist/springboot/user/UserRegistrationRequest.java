package com.shoppinglist.springboot.user;

public record UserRegistrationRequest
        (
                String firstname, String lastname, String email, String address, String phoneNumber
        )
{
}
