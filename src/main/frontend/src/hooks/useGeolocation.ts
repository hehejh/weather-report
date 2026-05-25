import { useState, useEffect, useCallback } from 'react';

interface GeolocationState {
  latitude: number | null;
  longitude: number | null;
  error: string | null;
  loading: boolean;
}

const DEFAULT_CENTER = { lat: 39.9042, lng: 116.4074 };

export function useGeolocation() {
  const [state, setState] = useState<GeolocationState>({
    latitude: null,
    longitude: null,
    error: null,
    loading: true,
  });

  const request = useCallback(() => {
    if (!navigator.geolocation) {
      setState({ latitude: null, longitude: null, error: '浏览器不支持地理定位', loading: false });
      return;
    }

    setState((s) => ({ ...s, loading: true }));

    navigator.geolocation.getCurrentPosition(
      (position) => {
        setState({
          latitude: position.coords.latitude,
          longitude: position.coords.longitude,
          error: null,
          loading: false,
        });
      },
      (err) => {
        setState({
          latitude: null,
          longitude: null,
          error: getGeolocationError(err),
          loading: false,
        });
      },
      { enableHighAccuracy: false, timeout: 10000, maximumAge: 300000 },
    );
  }, []);

  useEffect(() => {
    request();
  }, [request]);

  const center = state.latitude != null && state.longitude != null
    ? { lat: state.latitude, lng: state.longitude }
    : DEFAULT_CENTER;

  return { ...state, center, request };
}

function getGeolocationError(error: GeolocationPositionError): string {
  switch (error.code) {
    case error.PERMISSION_DENIED:
      return '位置权限被拒绝，请在浏览器设置中允许访问位置';
    case error.POSITION_UNAVAILABLE:
      return '无法获取位置信息';
    case error.TIMEOUT:
      return '获取位置超时';
    default:
      return '获取位置时发生未知错误';
  }
}
