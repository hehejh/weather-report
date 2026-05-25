import type { ApiResponse } from '../types';

const BASE_URL = '/api';

class ApiError extends Error {
  constructor(
    public status: number,
    message: string,
  ) {
    super(message);
    this.name = 'ApiError';
  }
}

async function request<T>(path: string, options?: RequestInit): Promise<T> {
  const res = await fetch(`${BASE_URL}${path}`, {
    headers: { 'Content-Type': 'application/json' },
    ...options,
  });

  if (!res.ok) {
    let errorMessage = `HTTP ${res.status}`;
    try {
      const body: ApiResponse<null> = await res.json();
      if (body.error) errorMessage = body.error;
    } catch {
      // response not JSON, use default message
    }
    throw new ApiError(res.status, errorMessage);
  }

  if (res.status === 204) return undefined as T;

  const body: ApiResponse<T> = await res.json();
  if (!body.success) {
    throw new ApiError(res.status, body.error ?? 'Unknown error');
  }
  return body.data;
}

export const api = {
  get: <T>(path: string) => request<T>(path),
  post: <T>(path: string, body: unknown) =>
    request<T>(path, { method: 'POST', body: JSON.stringify(body) }),
  put: <T>(path: string, body: unknown) =>
    request<T>(path, { method: 'PUT', body: JSON.stringify(body) }),
  delete: (path: string) => request<void>(path, { method: 'DELETE' }),
};

export { ApiError };
