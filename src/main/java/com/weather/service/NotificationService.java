package com.weather.service;

import com.weather.model.AlertRule;
import com.weather.model.PhotoSpot;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.format.DateTimeFormatter;

/**
 * Formats and logs notification payloads for alert triggers.
 * MVP: logs payload + stores via AlertHistory; Web Push deferred to Phase 5.
 */
@Service
public class NotificationService {

    private static final Logger log = LoggerFactory.getLogger(NotificationService.class);

    /**
     * Builds a notification payload JSON string and logs it.
     * Errors are caught and logged — never propagate to the scheduler.
     *
     * @param rule             the alert rule that triggered
     * @param spot             the photo spot the alert is for
     * @param score            photography index score at trigger time
     * @param conditionSummary human-readable condition description
     * @return JSON payload string, or null on error
     */
    public String buildPayload(AlertRule rule, PhotoSpot spot, int score, String conditionSummary) {
        try {
            String spotName = spot.getName() != null ? spot.getName() : "Unknown";
            String alertType = rule.getAlertType() != null ? rule.getAlertType() : "";
            String pushTime = rule.getPushTime() != null
                    ? rule.getPushTime().format(DateTimeFormatter.ofPattern("HH:mm")) : "";

            String payload = String.format(
                    "{\"spotName\":\"%s\",\"spotId\":%d,\"alertType\":\"%s\",\"score\":%d,\"condition\":\"%s\",\"pushTime\":\"%s\"}",
                    spotName, spot.getId(), alertType, score,
                    conditionSummary != null ? conditionSummary : "", pushTime);

            log.info("Alert notification payload: {}", payload);
            return payload;
        } catch (Exception e) {
            log.warn("Failed to build notification payload: {}", e.getMessage());
            return null;
        }
    }
}
