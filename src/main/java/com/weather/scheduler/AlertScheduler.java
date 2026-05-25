package com.weather.scheduler;

import com.weather.repository.AlertRuleRepository;
import com.weather.service.AlertService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Scheduled evaluator that scans all enabled alert rules twice daily and checks
 * current weather conditions against configured thresholds.
 *
 * <p>Schedule: daily at 06:00 and 18:00 (local server time).</p>
 */
@Service
public class AlertScheduler {

    private static final Logger log = LoggerFactory.getLogger(AlertScheduler.class);

    private final AlertRuleRepository alertRuleRepository;
    private final AlertService alertService;

    public AlertScheduler(AlertRuleRepository alertRuleRepository, AlertService alertService) {
        this.alertRuleRepository = alertRuleRepository;
        this.alertService = alertService;
    }

    /**
     * Scans all enabled alert rules, evaluates current weather against thresholds,
     * and records/dispatches matched alerts.
     *
     * <p>Each rule is evaluated independently — a failure in one rule does not block others.</p>
     */
    @Scheduled(cron = "${weather.alert.schedule:0 0 6,18 * * *}")
    @Transactional
    public void evaluateAlerts() {
        var rules = alertRuleRepository.findAllEnabledWithSpot();
        log.info("Starting alert evaluation: {} enabled rules", rules.size());

        int triggered = 0;
        for (var rule : rules) {
            try {
                var result = alertService.evaluateAndRecord(rule);
                if (result != null) {
                    triggered++;
                }
            } catch (Exception e) {
                log.warn("Failed to evaluate alert rule {}: {}", rule.getId(), e.getMessage());
            }
        }

        log.info("Alert evaluation complete: {} triggered out of {} rules", triggered, rules.size());
    }
}
