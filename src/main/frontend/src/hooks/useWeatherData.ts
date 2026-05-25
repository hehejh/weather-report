import { useState, useEffect, useCallback } from 'react';
import { getWeatherDashboard, getWeatherForecast } from '../api/weather';
import type { WeatherDashboard } from '../types';

export function useWeatherData(spotId: number | null) {
  const [dashboard, setDashboard] = useState<WeatherDashboard | null>(null);
  const [forecast, setForecast] = useState<WeatherDashboard | null>(null);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);

  const fetchDashboard = useCallback(async () => {
    if (!spotId) return;
    setLoading(true);
    setError(null);
    try {
      const [d, f] = await Promise.all([
        getWeatherDashboard(spotId),
        getWeatherForecast(spotId),
      ]);
      setDashboard(d);
      setForecast(f);
    } catch (err) {
      setError(err instanceof Error ? err.message : '获取天气数据失败');
    } finally {
      setLoading(false);
    }
  }, [spotId]);

  useEffect(() => {
    fetchDashboard();
  }, [fetchDashboard]);

  return { dashboard, forecast, loading, error, refresh: fetchDashboard };
}
