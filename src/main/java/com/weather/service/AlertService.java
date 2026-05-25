package com.weather.service;

import com.weather.dto.AlertRuleRequest;
import com.weather.exception.SpotNotFoundException;
import com.weather.model.AlertRule;
import com.weather.repository.AlertRuleRepository;
import com.weather.repository.PhotoSpotRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalTime;
import java.util.List;

@Service
@Transactional
public class AlertService {

    private final AlertRuleRepository alertRuleRepository;
    private final PhotoSpotRepository spotRepository;

    public AlertService(AlertRuleRepository alertRuleRepository, PhotoSpotRepository spotRepository) {
        this.alertRuleRepository = alertRuleRepository;
        this.spotRepository = spotRepository;
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
}
