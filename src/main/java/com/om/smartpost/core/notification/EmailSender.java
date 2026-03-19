package com.om.smartpost.core.notification;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class EmailSender {
    @Autowired
    JavaMailSender mailSender;

    public void sendForgotPasswordEmail(String to, String subject, String htmlContent) throws MessagingException {
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message,true,"UTF-8");

        helper.setFrom("comp.dmce12@gmai.com");
        helper.setTo(to);
        helper.setSubject(subject);
        helper.setText(htmlContent,true);

        mailSender.send(message);
    }
}

