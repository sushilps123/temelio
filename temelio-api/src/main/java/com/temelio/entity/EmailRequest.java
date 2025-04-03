package com.temelio.entity;

import java.util.List;

public class EmailRequest {
    private String senderId;
    private List<String> receiverIds;
    private String subject;
    private String emailBody;

    public EmailRequest(String senderId, List<String> receiverIds, String subject, String emailBody) {
        this.senderId = senderId;
        this.receiverIds = receiverIds;
        this.subject = subject;
        this.emailBody = emailBody;
    }

    public String getSenderId() { return senderId; }
    public List<String> getReceiverIds() { return receiverIds; }
    public String getSubject() { return subject; }
    public String getEmailBody() { return emailBody; }
}
