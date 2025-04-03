package com.temelio.service;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class MockEmailSenderService {

    public ResponseEntity<String> send(String senderId, String receiverId, String subject, String emailBody) {
        return new ResponseEntity<>("Emails sent successfully", HttpStatus.OK);
    }
}
