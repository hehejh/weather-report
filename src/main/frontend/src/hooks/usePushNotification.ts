import { useState, useCallback, useEffect } from 'react';

interface PushState {
  supported: boolean;
  permission: NotificationPermission;
  subscription: PushSubscription | null;
  loading: boolean;
  error: string | null;
}

function urlBase64ToUint8Array(base64String: string): Uint8Array {
  const padding = '='.repeat((4 - (base64String.length % 4)) % 4);
  const base64 = (base64String + padding).replace(/-/g, '+').replace(/_/g, '/');
  const rawData = window.atob(base64);
  const outputArray = new Uint8Array(rawData.length);
  for (let i = 0; i < rawData.length; ++i) {
    outputArray[i] = rawData.charCodeAt(i);
  }
  return outputArray;
}

export function usePushNotification() {
  const [state, setState] = useState<PushState>({
    supported: 'serviceWorker' in navigator && 'PushManager' in window,
    permission: 'Notification' in window ? Notification.permission : 'denied',
    subscription: null,
    loading: false,
    error: null,
  });

  const register = useCallback(async () => {
    if (!state.supported) {
      setState((s) => ({ ...s, error: '此浏览器不支持推送通知' }));
      return;
    }

    setState((s) => ({ ...s, loading: true, error: null }));

    try {
      const permission = await Notification.requestPermission();
      setState((s) => ({ ...s, permission }));

      if (permission !== 'granted') {
        setState((s) => ({ ...s, loading: false, error: '通知权限被拒绝' }));
        return;
      }

      const registration = await navigator.serviceWorker.ready;
      const existing = await registration.pushManager.getSubscription();
      if (existing) {
        setState((s) => ({ ...s, subscription: existing, loading: false }));
        return;
      }

      // VAPID key placeholder — replace with real key in production
      const vapidPublicKey = 'BEl62iUvw2G6LqAqGXaLdP6sLkZx8vK0mJ1nR3tSwDc=';
      const subscription = await registration.pushManager.subscribe({
        userVisibleOnly: true,
        applicationServerKey: urlBase64ToUint8Array(vapidPublicKey) as BufferSource,
      });

      setState((s) => ({ ...s, subscription, loading: false }));
    } catch (err) {
      setState((s) => ({
        ...s,
        loading: false,
        error: err instanceof Error ? err.message : '启用推送通知失败',
      }));
    }
  }, [state.supported]);

  const unregister = useCallback(async () => {
    if (!state.subscription) return;
    try {
      await state.subscription.unsubscribe();
      setState((s) => ({ ...s, subscription: null }));
    } catch (err) {
      setState((s) => ({
        ...s,
        error: err instanceof Error ? err.message : '取消推送通知失败',
      }));
    }
  }, [state.subscription]);

  useEffect(() => {
    if (!state.supported) return;
    navigator.serviceWorker.ready.then((registration) => {
      registration.pushManager.getSubscription().then((sub) => {
        if (sub) setState((s) => ({ ...s, subscription: sub }));
      });
    });
  }, [state.supported]);

  return { ...state, register, unregister };
}
