package com.shoppinglist.springboot.user;

import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.util.Date;

public record UserUpdateRequest(
        String firstname, String lastname, LocalDate birthDate
) {
}