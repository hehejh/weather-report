import { api } from './client';
import type { WeatherDashboard } from '../types';

export function getWeatherDashboard(spotId: number): Promise<WeatherDashboard> {
  return api.get<WeatherDashboard>(`/spots/${spotId}/weather`);
}

export function getWeatherForecast(spotId: number): Promise<WeatherDashboard> {
  return api.get<WeatherDashboard>(`/spots/${spotId}/weather/forecast`);
}
