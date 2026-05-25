export interface PhotoSpot {
  id: number;
  name: string;
  latitude: number;
  longitude: number;
  tags: string[];
  notes: string | null;
  photoUrl: string | null;
  photographyIndex: number | null;
  createdAt: string;
  updatedAt: string;
}

export interface PhotoSpotRequest {
  name: string;
  latitude: number;
  longitude: number;
  tags: string[];
  notes: string | null;
  photoUrl: string | null;
}

export interface WeatherDashboard {
  spotId: number;
  spotName: string;
  photographyIndex: number | null;
  indexLabel: string | null;
  current: CurrentWeather | null;
  solar: SolarTimes | null;
  glow: GlowForecast | null;
  weekForecast: DailyForecast[] | null;
}

export interface CurrentWeather {
  temperature: number;
  feelsLike: number;
  humidity: number;
  windSpeed: number;
  windDirection: string;
  visibility: number;
  aqi: number;
  totalCloud: number;
  precipitationProbability: number;
}

export interface SolarTimes {
  sunrise: string;
  sunset: string;
  goldenHourMorning: string;
  goldenHourEvening: string;
  blueHourMorning: string;
  blueHourEvening: string;
}

export interface GlowForecast {
  type: string;
  probability: number;
  quality: string;
  breakdown: PhotographyIndexBreakdown | null;
}

export interface PhotographyIndexBreakdown {
  cloudScore: number;
  humidityScore: number;
  visibilityScore: number;
  aqiScore: number;
  windScore: number;
  notes: string;
}

export interface DailyForecast {
  date: string;
  photographyIndex: number;
  tempMax: number;
  tempMin: number;
  weatherIcon: string;
  precipitationProbability: number;
  morningGlow: GlowForecast | null;
  eveningGlow: GlowForecast | null;
}

export interface AlertRule {
  id: number;
  alertType: string;
  thresholds: string;
  pushTime: string;
  enabled: boolean;
  createdAt: string;
}

export interface AlertRuleRequest {
  alertType: string;
  glowProbability: number;
  maxCloud: number | null;
  maxWind: number | null;
  minVisibility: number | null;
  minTemp: number | null;
  maxTemp: number | null;
  pushTime: string;
}

export interface AlertHistory {
  id: number;
  ruleId: number;
  spotId: number;
  triggeredAt: string;
  weatherSnapshotId: number | null;
  score: number;
  sent: boolean;
}

export interface ApiResponse<T> {
  success: boolean;
  data: T;
  error: string | null;
}

export interface MapBounds {
  swLat: number;
  swLng: number;
  neLat: number;
  neLng: number;
}
