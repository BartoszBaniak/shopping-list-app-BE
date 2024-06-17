package com.shoppinglist.springboot.user;

public record ChangePasswordRequest(String currentPassword, String newPassword, String retNewPassword) {
}