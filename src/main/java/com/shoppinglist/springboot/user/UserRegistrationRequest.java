package com.shoppinglist.springboot.user;

import java.time.LocalDate;
public record UserRegistrationRequest
        (
                String firstname, String lastname, String email, String password, LocalDate birthDate,String profilePic, Boolean isActive
        )
{
}
