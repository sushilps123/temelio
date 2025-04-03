package com.temelio.entity;

import java.util.Date;

public class SentEmail {
    private String senderId;
    private String recipient;
    private String subject;
    private String message;
    private Date sentDate;
    private String status;

    public SentEmail(String senderId, String recipient,String subject, String message, Date sentDate, String status) {
        this.senderId = senderId;
        this.subject = subject;
        this.recipient = recipient;
        this.message = message;
        this.sentDate = sentDate;
        this.status = status;
    }

    // Getters
    public String getSenderId() { return senderId; }
    public String getRecipient() { return recipient; }
    public String getMessage() { return message; }
    public Date getSentDate() { return sentDate; }
    public String getStatus() { return status; }
    public String getSubject() {return subject;}


    // Setters
    public void setStatus(String status) { this.status = status; }
}
