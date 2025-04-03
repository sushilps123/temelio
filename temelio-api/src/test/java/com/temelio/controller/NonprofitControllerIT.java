package com.temelio.controller;

import com.temelio.entity.EmailRequest;
import com.temelio.entity.Nonprofit;
import com.temelio.entity.SentEmail;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.test.annotation.DirtiesContext;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class NonprofitControllerIT {

    @Autowired
    private TestRestTemplate restTemplate;

    /**
     * ✅ Test: Create a Nonprofit Successfully
     */

    @Test
    void testCreateNonprofitSuccess() {
        Nonprofit nonprofit = new Nonprofit("HelpingHands", "123 Main St", "help@hands.org");

        ResponseEntity<String> response = restTemplate.postForEntity("/v1/api/nonprofits", nonprofit, String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody()).contains("Nonprofit with email help@hands.org added successfully");
    }

    /**
     * ❌ Negative Test: Attempt to Create a Duplicate Nonprofit
     */
    @Test
    void testCreateDuplicateNonprofit() {
        Nonprofit nonprofit = new Nonprofit("HelpingHands", "123 Main St", "duplicate@hands.org");

        // First attempt (should be successful)
        restTemplate.postForEntity("/v1/api/nonprofits", nonprofit, String.class);

        // Second attempt (should fail)
        ResponseEntity<String> response = restTemplate.postForEntity("/v1/api/nonprofits", nonprofit, String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
        assertThat(response.getBody()).contains("Nonprofit with email duplicate@hands.org already exists");
    }

    /**
     * ✅ Test: Send Emails to Registered Nonprofits
     */
    @Test
    void testSendEmailsToRegisteredNonprofits() {
        // First, create nonprofits
        restTemplate.postForEntity("/v1/api/nonprofits", new Nonprofit("Org1", "Street 1", "org1@email.com"), String.class);
        restTemplate.postForEntity("/v1/api/nonprofits", new Nonprofit("Org2", "Street 2", "org2@email.com"), String.class);

        // Create email request
        EmailRequest emailRequest = new EmailRequest("admin@temelio.com",List.of("org1@email.com", "org2@email.com"), "Test Subject", "Test Message");

        ResponseEntity<String> response = restTemplate.postForEntity("/v1/api/send-emails", emailRequest, String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).contains("Email sending initiated");
    }

    @Test
    void TestSendEmailsWithParameterizedMessageTemplates(){
        restTemplate.postForEntity("/v1/api/nonprofits", new Nonprofit("Org1", "Street 1", "org1@email.com"), String.class);
        restTemplate.postForEntity("/v1/api/nonprofits", new Nonprofit("Org2", "Street 2", "org2@email.com"), String.class);
        String messageTemplate = "Sending money to nonprofit {name} at address {address}";
        EmailRequest emailRequest = new EmailRequest("adminX@temelio.com",List.of("org1@email.com", "org2@email.com"), "Test Subject", messageTemplate);

        ResponseEntity<String> sendEmailRes = restTemplate.postForEntity("/v1/api/send-emails", emailRequest, String.class);
        ResponseEntity<List<SentEmail>> sentEmails = restTemplate.exchange(
                "/v1/api/sent-emails",
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<>() {}
        );

        SentEmail mail1 = sentEmails.getBody().get(0);
        SentEmail mail2 = sentEmails.getBody().get(1);

        assertThat(mail1.getSenderId()).isEqualTo("adminX@temelio.com");
        assertThat(mail1.getRecipient()).isIn("org1@email.com");
        assertThat(mail1.getSubject()).isEqualTo("Test Subject");
        assertThat(mail1.getMessage()).isEqualTo("Sending money to nonprofit Org1 at address Street 1");
        assertThat(mail1.getStatus()).isEqualTo("SENT");

        assertThat(mail2.getSenderId()).isEqualTo("adminX@temelio.com");
        assertThat(mail2.getRecipient()).isIn("org2@email.com");
        assertThat(mail2.getSubject()).isEqualTo("Test Subject");
        assertThat(mail2.getMessage()).isEqualTo("Sending money to nonprofit Org2 at address Street 2");
        assertThat(mail2.getStatus()).isEqualTo("SENT");


    }

    /**
     * ✅ Test: Retrieve Sent Emails
     */
    @Test
    void testGetAllSentEmails() {

        restTemplate.postForEntity("/v1/api/nonprofits", new Nonprofit("Org1", "Street 1", "org1@email.com"), String.class);
        restTemplate.postForEntity("/v1/api/nonprofits", new Nonprofit("Org2", "Street 2", "org2@email.com"), String.class);

        // Create email request
        EmailRequest emailRequest = new EmailRequest("admin1@temelio.com",List.of("org1@email.com", "org2@email.com"), "Test Subject", "Test Message");

        ResponseEntity<String> res1 = restTemplate.postForEntity("/v1/api/send-emails", emailRequest, String.class);

        // Fetch sent emails
        ResponseEntity<List<SentEmail>> response = restTemplate.exchange(
                "/v1/api/sent-emails",
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<>() {}
        );

        // Validate response
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull().hasSize(2);

        // Extract emails
        List<SentEmail> sentEmails = response.getBody();
        assertThat(sentEmails).isNotNull();

        SentEmail email1 = sentEmails.get(0);
        SentEmail email2 = sentEmails.get(1);

        // Validate email1
        assertThat(email1.getRecipient()).isIn("org1@email.com", "org2@email.com");
        assertThat(email1.getMessage()).isEqualTo("Test Message");
        assertThat(email1.getStatus()).isEqualTo("SENT");

        // Validate email2
        assertThat(email2.getRecipient()).isIn("org1@email.com", "org2@email.com");
        assertThat(email2.getMessage()).isEqualTo("Test Message");
        assertThat(email2.getStatus()).isEqualTo("SENT");
    }

    /**
     * ✅ Test: Retrieve Sent Emails by Sender ID
     */

    @Test
    void testGetSentEmailsBySender() {
        // Create nonprofits
        restTemplate.postForEntity("/v1/api/nonprofits", new Nonprofit("Org1", "Street 1", "org1@email.com"), String.class);
        restTemplate.postForEntity("/v1/api/nonprofits", new Nonprofit("Org2", "Street 2", "org2@email.com"), String.class);
        restTemplate.postForEntity("/v1/api/nonprofits", new Nonprofit("Org3", "Street 3", "org3@email.com"), String.class);
        restTemplate.postForEntity("/v1/api/nonprofits", new Nonprofit("Org4", "Street 4", "org4@email.com"), String.class);

        // First Email Request - Sent by admin@temelio.com
        EmailRequest emailRequest1 = new EmailRequest(
                "admin@temelio.com",
                List.of("org1@email.com", "org2@email.com"),
                "Subject 1",
                "Message 1"
        );
        ResponseEntity<String> emailResponse1 = restTemplate.postForEntity("/v1/api/send-emails", emailRequest1, String.class);
        assertThat(emailResponse1.getStatusCode()).isEqualTo(HttpStatus.OK);

        // Second Email Request - Sent by user@temelio.com
        EmailRequest emailRequest2 = new EmailRequest(
                "user@temelio.com",
                List.of("org3@email.com", "org4@email.com"),
                "Subject 2",
                "Message 2"
        );
        ResponseEntity<String> emailResponse2 = restTemplate.postForEntity("/v1/api/send-emails", emailRequest2, String.class);
        assertThat(emailResponse2.getStatusCode()).isEqualTo(HttpStatus.OK);

        // Fetch sent emails for admin@temelio.com
        ResponseEntity<List<SentEmail>> response1 = restTemplate.exchange(
                "/v1/api/sent-emails/admin@temelio.com",
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<>() {}
        );

        // Fetch sent emails for user@temelio.com
        ResponseEntity<List<SentEmail>> response2 = restTemplate.exchange(
                "/v1/api/sent-emails/user@temelio.com",
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<>() {}
        );

        // Validate response for admin@temelio.com
        assertThat(response1.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response1.getBody()).isNotNull().hasSize(2);

        for (SentEmail email : response1.getBody()) {
            assertThat(email.getSenderId()).isEqualTo("admin@temelio.com");
            assertThat(email.getRecipient()).isIn("org1@email.com", "org2@email.com");
            assertThat(email.getSubject()).isEqualTo("Subject 1");
            assertThat(email.getMessage()).isEqualTo("Message 1");
            assertThat(email.getStatus()).isEqualTo("SENT");
        }

        // Validate response for user@temelio.com
        assertThat(response2.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response2.getBody()).isNotNull().hasSize(2);

        for (SentEmail email : response2.getBody()) {
            assertThat(email.getSenderId()).isEqualTo("user@temelio.com");
            assertThat(email.getRecipient()).isIn("org3@email.com", "org4@email.com");
            assertThat(email.getSubject()).isEqualTo("Subject 2");
            assertThat(email.getMessage()).isEqualTo("Message 2");
            assertThat(email.getStatus()).isEqualTo("SENT");
        }
    }

}
