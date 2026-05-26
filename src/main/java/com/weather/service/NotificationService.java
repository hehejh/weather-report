package com.weather.service;

import com.weather.model.AlertRule;
import com.weather.model.PhotoSpot;
import jakarta.mail.internet.MimeMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Builds and sends email notifications for triggered alert rules.
 * Failures are caught and logged — never propagate to the scheduler.
 */
@Service
public class NotificationService {

    private static final Logger log = LoggerFactory.getLogger(NotificationService.class);

    private final JavaMailSender mailSender;
    private final String mailFrom;
    private final String subjectPrefix;

    public NotificationService(JavaMailSender mailSender,
                               @Value("${weather.mail.from}") String mailFrom,
                               @Value("${weather.mail.subject-prefix}") String subjectPrefix) {
        this.mailSender = mailSender;
        this.mailFrom = mailFrom;
        this.subjectPrefix = subjectPrefix;
    }

    /**
     * Sends an HTML email notification for a triggered alert.
     *
     * @param rule             the alert rule that triggered
     * @param spot             the photo spot
     * @param score            photography index score at trigger time
     * @param conditionSummary human-readable condition description
     * @return true if email was sent, false if skipped (no recipient) or failed
     */
    public boolean sendAlert(AlertRule rule, PhotoSpot spot, int score, String conditionSummary) {
        String recipient = rule.getRecipientEmail();
        if (recipient == null || recipient.isBlank()) {
            log.info("No recipient email configured for rule {} — skipping email", rule.getId());
            return false;
        }
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setFrom(mailFrom);
            helper.setTo(recipient);
            String alertType = rule.getAlertType() != null ? rule.getAlertType() : "";
            helper.setSubject(subjectPrefix + " " + alertType + " alert for " + spot.getName());
            helper.setText(buildHtmlBody(rule, spot, score, conditionSummary), true);
            mailSender.send(message);
            log.info("Alert email sent to {} for rule {} (spot: {})", recipient, rule.getId(), spot.getName());
            return true;
        } catch (Exception e) {
            log.warn("Failed to send alert email to {} for rule {}: {}", recipient, rule.getId(), e.getMessage());
            return false;
        }
    }

    private String buildHtmlBody(AlertRule rule, PhotoSpot spot, int score, String conditionSummary) {
        String spotName = spot.getName() != null ? spot.getName() : "Unknown";
        String alertType = rule.getAlertType() != null ? rule.getAlertType() : "";
        String pushTime = rule.getPushTime() != null
                ? rule.getPushTime().format(DateTimeFormatter.ofPattern("HH:mm")) : "";
        String now = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));

        return """
                <!DOCTYPE html>
                <html>
                <head><meta charset="UTF-8"><style>
                    body { font-family: Arial, sans-serif; color: #333; }
                    .container { max-width: 600px; margin: 0 auto; padding: 20px; }
                    .header { background: #1a73e8; color: white; padding: 20px; border-radius: 8px 8px 0 0; }
                    .content { background: #f8f9fa; padding: 20px; border: 1px solid #e0e0e0; }
                    .detail { margin: 10px 0; }
                    .label { font-weight: bold; color: #555; }
                    .score { font-size: 36px; font-weight: bold; color: #1a73e8; text-align: center; margin: 15px 0; }
                    .footer { font-size: 12px; color: #999; text-align: center; margin-top: 20px; }
                </style></head>
                <body>
                <div class="container">
                    <div class="header">
                        <h1>%s Alert</h1>
                    </div>
                    <div class="content">
                        <div class="score">%d</div>
                        <p style="text-align:center;color:#666;">Photography Index</p>
                        <hr>
                        <div class="detail"><span class="label">Location:</span> %s</div>
                        <div class="detail"><span class="label">Alert Type:</span> %s</div>
                        <div class="detail"><span class="label">Condition:</span> %s</div>
                        <div class="detail"><span class="label">Push Time:</span> %s</div>
                        <div class="detail"><span class="label">Evaluated At:</span> %s</div>
                    </div>
                    <div class="footer">
                        This is an automated alert from Photo Weather Assistant.<br>
                        To manage your alerts, visit the application.
                    </div>
                </div>
                </body>
                </html>
                """.formatted(
                !alertType.isEmpty() ? alertType : "Photography",
                score,
                spotName,
                !alertType.isEmpty() ? alertType : "N/A",
                conditionSummary != null ? conditionSummary : "",
                pushTime,
                now
        );
    }
}
