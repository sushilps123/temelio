package com.temelio.controller;

import com.temelio.entity.EmailRequest;
import com.temelio.entity.Nonprofit;
import com.temelio.entity.SentEmail;
import com.temelio.service.EmailService;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;

import java.util.*;

@RestController
@RequestMapping("/v1/api")
public class NonprofitController {
    private final Map<String, Nonprofit> nonprofitEmailMapper = new HashMap<>();
    private final EmailService emailService;

    public NonprofitController(EmailService emailService) {
        this.emailService = emailService;
    }

    @PostMapping("/nonprofits")
    public ResponseEntity<String> createNonprofit(@RequestBody Nonprofit nonprofit) {
        String emailId = nonprofit.getEmail();
        if (nonprofitEmailMapper.containsKey(emailId)) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body("Nonprofit with email " + nonprofit.getEmail() + " already exists");
        }

        nonprofitEmailMapper.put(emailId, nonprofit);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body("Nonprofit with email " + nonprofit.getEmail() + " added successfully");
    }

    @PostMapping("/send-emails")
    public ResponseEntity<String> sendEmails(@RequestBody EmailRequest emailRequest) {
        emailService.sendEmails(emailRequest, nonprofitEmailMapper);
        return ResponseEntity.ok("Email sending initiated");
    }

    @GetMapping("/sent-emails")
    public ResponseEntity<List<SentEmail>> getSentEmails() {
        return ResponseEntity.ok(emailService.getSentEmails());
    }

    @GetMapping("/sent-emails/{senderId}")
    public ResponseEntity<List<SentEmail>> getSentEmailsBySender(@PathVariable String senderId) {
        return ResponseEntity.ok(emailService.getSentEmailsBySender(senderId));
    }
}
