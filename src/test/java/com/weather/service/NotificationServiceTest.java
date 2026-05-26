package com.weather.service;

import com.weather.model.AlertRule;
import com.weather.model.PhotoSpot;
import jakarta.mail.Session;
import jakarta.mail.internet.MimeMessage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.springframework.mail.MailSendException;
import org.springframework.mail.javamail.JavaMailSender;

import java.time.LocalTime;
import java.util.Properties;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class NotificationServiceTest {

    private NotificationService notificationService;
    private JavaMailSender mailSender;
    private MimeMessage mimeMessage;
    private static final GeometryFactory GF = new GeometryFactory();

    @BeforeEach
    void setUp() {
        mailSender = mock(JavaMailSender.class);
        notificationService = new NotificationService(mailSender, "from@test.com", "[Test]");
        var session = Session.getDefaultInstance(new Properties());
        mimeMessage = new MimeMessage(session);
        when(mailSender.createMimeMessage()).thenReturn(mimeMessage);
    }

    @Test
    @DisplayName("sendAlert sends email when recipient configured")
    void sendAlert_hasRecipient_sendsEmail() {
        var spot = new PhotoSpot("user", "Tower View", GF.createPoint(new Coordinate(116.4, 39.9)), null);
        spot.setId(1L);
        var rule = new AlertRule(spot, "sunrise", "{}", LocalTime.of(6, 0));
        rule.setRecipientEmail("user@example.com");

        boolean result = notificationService.sendAlert(rule, spot, 85,
                "Good sunrise conditions expected");

        assertTrue(result);
        verify(mailSender).send(any(MimeMessage.class));
    }

    @Test
    @DisplayName("sendAlert skips when no recipient email configured")
    void sendAlert_noRecipient_skips() {
        var spot = new PhotoSpot("user", "Tower View", GF.createPoint(new Coordinate(116.4, 39.9)), null);
        spot.setId(1L);
        var rule = new AlertRule(spot, "sunrise", "{}", LocalTime.of(6, 0));

        boolean result = notificationService.sendAlert(rule, spot, 85, "summary");

        assertFalse(result);
        verify(mailSender, never()).send(any(MimeMessage.class));
    }

    @Test
    @DisplayName("sendAlert skips when recipient email is blank")
    void sendAlert_blankRecipient_skips() {
        var spot = new PhotoSpot("user", "Tower View", GF.createPoint(new Coordinate(116.4, 39.9)), null);
        spot.setId(1L);
        var rule = new AlertRule(spot, "sunrise", "{}", LocalTime.of(6, 0));
        rule.setRecipientEmail("   ");

        boolean result = notificationService.sendAlert(rule, spot, 85, "summary");

        assertFalse(result);
    }

    @Test
    @DisplayName("sendAlert catches exception and returns false")
    void sendAlert_mailException_returnsFalse() {
        var spot = new PhotoSpot("user", "Tower View", GF.createPoint(new Coordinate(116.4, 39.9)), null);
        spot.setId(1L);
        var rule = new AlertRule(spot, "sunrise", "{}", LocalTime.of(6, 0));
        rule.setRecipientEmail("user@example.com");
        doThrow(new MailSendException("SMTP down")).when(mailSender).send(any(MimeMessage.class));

        boolean result = notificationService.sendAlert(rule, spot, 85, "summary");

        assertFalse(result);
    }

    @Test
    @DisplayName("sendAlert handles null fields gracefully")
    void sendAlert_nullFields_doesNotThrow() {
        var spot = new PhotoSpot("user", null, GF.createPoint(new Coordinate(0.0, 0.0)), null);
        var rule = new AlertRule(spot, null, "{}", null);
        rule.setRecipientEmail("user@example.com");

        assertDoesNotThrow(() -> notificationService.sendAlert(rule, spot, 50, null));
    }
}
