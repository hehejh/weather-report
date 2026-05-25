import { api } from './client';
import type { AlertRule, AlertRuleRequest, AlertHistory } from '../types';

export function listAlerts(spotId: number): Promise<AlertRule[]> {
  return api.get<AlertRule[]>(`/spots/${spotId}/alerts`);
}

export function createAlert(spotId: number, data: AlertRuleRequest): Promise<AlertRule> {
  return api.post<AlertRule>(`/spots/${spotId}/alerts`, data);
}

export function getAlert(id: number): Promise<AlertRule> {
  return api.get<AlertRule>(`/alerts/${id}`);
}

export function updateAlert(id: number, data: AlertRuleRequest): Promise<AlertRule> {
  return api.put<AlertRule>(`/alerts/${id}`, data);
}

export function deleteAlert(id: number): Promise<void> {
  return api.delete(`/alerts/${id}`);
}

export function getAlertHistory(id: number): Promise<AlertHistory[]> {
  return api.get<AlertHistory[]>(`/alerts/${id}/history`);
}

export function testAlert(id: number): Promise<AlertHistory | null> {
  return api.post<AlertHistory | null>(`/alerts/${id}/test`, {});
}
