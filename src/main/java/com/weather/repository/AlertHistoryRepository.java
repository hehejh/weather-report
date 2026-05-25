package com.weather.repository;

import com.weather.model.AlertHistory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AlertHistoryRepository extends JpaRepository<AlertHistory, Long> {

    List<AlertHistory> findByRuleIdOrderByTriggeredAtDesc(Long ruleId);
}
