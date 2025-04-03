package com.temelio.service;

import com.temelio.entity.EmailRequest;
import com.temelio.entity.Nonprofit;
import com.temelio.entity.SentEmail;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class EmailService {
    private final Map<String, List<SentEmail>> sentEmailsBySender = new HashMap<>();
    private final List<SentEmail> sentEmails = new ArrayList<>();
    private final Map<String, String> emailStatus = new HashMap<>();
    private final MockEmailSenderService mockEmailSenderService;

    public EmailService(MockEmailSenderService mockEmailSenderService) {
        this.mockEmailSenderService = mockEmailSenderService;
    }

    public void sendEmails(EmailRequest emailRequest, Map<String, Nonprofit> nonprofits) {
        String senderId = emailRequest.getSenderId();
        List<String> receiverIds = emailRequest.getReceiverIds();
        String subject = emailRequest.getSubject();
        String messageTemplate = emailRequest.getEmailBody();

        for (String receiverId : receiverIds) {
            Nonprofit nonprofit = nonprofits.get(receiverId);
            if (nonprofit != null) {
                emailStatus.put(receiverId, "IN_PROGRESS");

                try {
                    String message = messageTemplate
                            .replace("{name}", nonprofit.getName())
                            .replace("{address}", nonprofit.getAddress());

                    // Calling mock email sender service
                    ResponseEntity<String> response = mockEmailSenderService.send(senderId, receiverId, subject, message);

                    if (response.getStatusCode().is2xxSuccessful()) {
                        sentEmailsBySender.computeIfAbsent(senderId, k -> new ArrayList<>())
                                .add(new SentEmail(senderId,receiverId, subject,message, new Date(), "SENT"));

                        sentEmails.add(new SentEmail(senderId,receiverId,subject, message, new Date(), "SENT"));
                        emailStatus.put(receiverId, "SENT");
                    } else {
                        emailStatus.put(receiverId, "FAILED");
                    }

                } catch (Exception e) {
                    emailStatus.put(receiverId, "FAILED");
                }
            } else {
                emailStatus.put(receiverId, "Nonprofit not registered with email ID: " + receiverId);
            }
        }
    }

    public List<SentEmail> getSentEmails() {
        return new ArrayList<>(sentEmails);
    }

    public List<SentEmail> getSentEmailsBySender(String senderId) {
        return new ArrayList<>(sentEmailsBySender.get(senderId));
    }


}
