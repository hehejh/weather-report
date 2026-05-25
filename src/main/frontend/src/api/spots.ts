import { api } from './client';
import type { PhotoSpot, PhotoSpotRequest } from '../types';

export function listSpots(): Promise<PhotoSpot[]>;
export function listSpots(bounds: { swLat: number; swLng: number; neLat: number; neLng: number }): Promise<PhotoSpot[]>;
export function listSpots(query: string): Promise<PhotoSpot[]>;
export function listSpots(
  arg?: string | { swLat: number; swLng: number; neLat: number; neLng: number },
): Promise<PhotoSpot[]> {
  if (!arg) return api.get<PhotoSpot[]>('/spots');
  if (typeof arg === 'string') return api.get<PhotoSpot[]>(`/spots?q=${encodeURIComponent(arg)}`);
  const { swLat, swLng, neLat, neLng } = arg;
  return api.get<PhotoSpot[]>(`/spots?swLat=${swLat}&swLng=${swLng}&neLat=${neLat}&neLng=${neLng}`);
}

export function getSpot(id: number): Promise<PhotoSpot> {
  return api.get<PhotoSpot>(`/spots/${id}`);
}

export function createSpot(data: PhotoSpotRequest): Promise<PhotoSpot> {
  return api.post<PhotoSpot>('/spots', data);
}

export function updateSpot(id: number, data: PhotoSpotRequest): Promise<PhotoSpot> {
  return api.put<PhotoSpot>(`/spots/${id}`, data);
}

export function deleteSpot(id: number): Promise<void> {
  return api.delete(`/spots/${id}`);
}
