package com.shoppinglist.springboot.MailService;

import com.shoppinglist.springboot.user.User;
import jakarta.annotation.Nullable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.UUID;

@Component
public class MailService {

//    @Autowired
//    JavaMailSender javaMailSender;
//    @Value("${mail.baseUrl}")
//    private String baseUrl;
//
//
//    public void sendEmail(User user, String emailType, @Nullable String token) {
//        String path = "";
//        String subject = "";
//        String actionText = "";
//
//        switch (emailType.toLowerCase()) {
//            case "reset":
//                path = "/reset-password/";
//                subject = "Password Reset Link";
//                actionText = "reset your password";
//                break;
//            default:
//                throw new IllegalArgumentException("Invalid email type");
//        }
//
//        String link = baseUrl + path + token;
//        String messageBody = String.format("Hello,\n\nPlease click on this link to %s: %s.\n\nRegards,\nXYZ", actionText, link);
//
//        SimpleMailMessage msg = new SimpleMailMessage();
//        msg.setFrom("sender@gmail.com"); // Consider externalizing
//        msg.setTo(user.getEmail());
//        msg.setSubject(subject);
//        msg.setText(messageBody);
//
//        try {
//            javaMailSender.send(msg);
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//    }
}