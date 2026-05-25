package com.weather.service;

import com.weather.dto.AlertRuleRequest;
import com.weather.exception.SpotNotFoundException;
import com.weather.model.AlertHistory;
import com.weather.model.AlertRule;
import com.weather.repository.AlertHistoryRepository;
import com.weather.repository.AlertRuleRepository;
import com.weather.repository.PhotoSpotRepository;
import com.weather.service.WeatherService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalTime;
import java.util.List;
import java.util.Map;

@Service
@Transactional
public class AlertService {

    private static final Logger log = LoggerFactory.getLogger(AlertService.class);
    private static final ObjectMapper MAPPER = new ObjectMapper();

    private final AlertRuleRepository alertRuleRepository;
    private final PhotoSpotRepository spotRepository;
    private final AlertHistoryRepository alertHistoryRepository;
    private final WeatherService weatherService;
    private final NotificationService notificationService;

    public AlertService(AlertRuleRepository alertRuleRepository,
                        PhotoSpotRepository spotRepository,
                        AlertHistoryRepository alertHistoryRepository,
                        WeatherService weatherService,
                        NotificationService notificationService) {
        this.alertRuleRepository = alertRuleRepository;
        this.spotRepository = spotRepository;
        this.alertHistoryRepository = alertHistoryRepository;
        this.weatherService = weatherService;
        this.notificationService = notificationService;
    }

    public AlertRule create(Long spotId, AlertRuleRequest request) {
        var spot = spotRepository.findById(spotId)
                .orElseThrow(() -> new SpotNotFoundException(spotId));
        var rule = new AlertRule(spot, request.alertType(),
                request.toThresholdsJson(), LocalTime.parse(request.pushTime()));
        return alertRuleRepository.save(rule);
    }

    @Transactional(readOnly = true)
    public List<AlertRule> listBySpot(Long spotId) {
        return alertRuleRepository.findBySpotIdOrderByCreatedAtDesc(spotId);
    }

    public AlertRule update(Long alertId, AlertRuleRequest request) {
        var rule = alertRuleRepository.findById(alertId)
                .orElseThrow(() -> new SpotNotFoundException(alertId));
        rule.setAlertType(request.alertType());
        rule.setThresholds(request.toThresholdsJson());
        rule.setPushTime(LocalTime.parse(request.pushTime()));
        return alertRuleRepository.save(rule);
    }

    public void delete(Long alertId) {
        if (!alertRuleRepository.existsById(alertId)) {
            throw new SpotNotFoundException(alertId);
        }
        alertRuleRepository.deleteById(alertId);
    }

    @Transactional(readOnly = true)
    public AlertRule getById(Long alertId) {
        return alertRuleRepository.findById(alertId)
                .orElseThrow(() -> new SpotNotFoundException(alertId));
    }

    /**
     * Evaluates an alert rule against current weather conditions and records the result.
     *
     * @param rule the alert rule to evaluate (must have spot eagerly loaded)
     * @return AlertHistory record if triggered, null otherwise
     */
    public AlertHistory evaluateAndRecord(AlertRule rule) {
        var spot = rule.getSpot();
        var dashboard = weatherService.getDashboard(spot);
        var thresholds = parseThresholds(rule.getThresholds());

        int glowProbability = dashboard.glow() != null ? dashboard.glow().probability() : 0;
        int requiredGlow = getInt(thresholds, "glow_probability", 60);

        if (glowProbability < requiredGlow) {
            return null;
        }

        var current = dashboard.current();
        if (current != null) {
            if (!checkThreshold(thresholds, "max_cloud", toDouble(current.totalCloud()), true)) return null;
            if (!checkThreshold(thresholds, "max_wind", current.windSpeed(), true)) return null;
            if (!checkThreshold(thresholds, "min_visibility", toDouble(current.visibility()), false)) return null;
            if (!checkTemperature(thresholds, current.temperature())) return null;
        }

        var history = new AlertHistory(rule.getId(), spot.getId(), dashboard.photographyIndex());
        alertHistoryRepository.save(history);

        String summary = String.format("📍 %s 可能有高质量%s（概率 %d%%），综合指数 %d",
                spot.getName(), rule.getAlertType(), glowProbability, dashboard.photographyIndex());
        notificationService.buildPayload(rule, spot, dashboard.photographyIndex(), summary);

        log.info("Alert triggered: rule={} spot={} type={} probability={}",
                rule.getId(), spot.getName(), rule.getAlertType(), glowProbability);
        return history;
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> parseThresholds(String thresholdsJson) {
        try {
            return MAPPER.readValue(thresholdsJson, Map.class);
        } catch (Exception e) {
            return Map.of();
        }
    }

    private int getInt(Map<String, Object> thresholds, String key, int defaultVal) {
        var value = thresholds.get(key);
        if (value instanceof Number n) return n.intValue();
        return defaultVal;
    }

    private boolean checkThreshold(Map<String, Object> thresholds, String key,
                                    Double actual, boolean actualMustBeLower) {
        if (actual == null) return true;
        int limit = getInt(thresholds, key, -1);
        if (limit < 0) return true;
        return actualMustBeLower ? actual <= limit : actual >= limit;
    }

    private boolean checkTemperature(Map<String, Object> thresholds, Double temp) {
        if (temp == null) return true;
        int minTemp = getInt(thresholds, "min_temp", Integer.MIN_VALUE);
        int maxTemp = getInt(thresholds, "max_temp", Integer.MAX_VALUE);
        if (minTemp == Integer.MIN_VALUE && maxTemp == Integer.MAX_VALUE) return true;
        if (minTemp != Integer.MIN_VALUE && temp < minTemp) return false;
        if (maxTemp != Integer.MAX_VALUE && temp > maxTemp) return false;
        return true;
    }

    private static Double toDouble(Integer value) {
        return value != null ? value.doubleValue() : null;
    }
}
