import { useEffect, useMemo } from 'react';
import { MapContainer, TileLayer, Marker, Popup, useMap, useMapEvents } from 'react-leaflet';
import L from 'leaflet';
import type { PhotoSpot } from '../types';
import { getPhotoIndexColor } from '../utils/photoIndexColor';

function createIndexIcon(index: number | null | undefined) {
  const { text } = getPhotoIndexColor(index);
  const colorMap: Record<string, string> = {
    'text-accent-good': '#22c55e',
    'text-accent-caution': '#eab308',
    'text-accent-bad': '#ef4444',
    'text-text-muted': '#64748b',
  };
  const color = colorMap[text] || '#64748b';
  const html = `<div style="
    width:32px;height:32px;border-radius:50%;
    background:${color};border:3px solid #fff;
    box-shadow:0 2px 8px rgba(0,0,0,0.4);
    display:flex;align-items:center;justify-content:center;
    font-size:12px;font-weight:700;color:#fff;
  ">${index != null ? index : '?'}</div>`;

  return L.divIcon({
    html,
    className: '',
    iconSize: [32, 32],
    iconAnchor: [16, 16],
    popupAnchor: [0, -20],
  });
}

interface MapViewProps {
  spots: PhotoSpot[];
  center: { lat: number; lng: number };
  zoom?: number;
  onBoundsChange?: (bounds: { swLat: number; swLng: number; neLat: number; neLng: number }) => void;
  onMapClick?: (latlng: { lat: number; lng: number }) => void;
  selectedSpotId?: number | null;
}

function MapEvents({
  onBoundsChange,
  onMapClick,
}: {
  onBoundsChange?: MapViewProps['onBoundsChange'];
  onMapClick?: MapViewProps['onMapClick'];
}) {
  const map = useMapEvents({
    moveend: () => {
      if (!onBoundsChange) return;
      const b = map.getBounds();
      onBoundsChange({
        swLat: b.getSouthWest().lat,
        swLng: b.getSouthWest().lng,
        neLat: b.getNorthEast().lat,
        neLng: b.getNorthEast().lng,
      });
    },
    click: (e) => {
      if (onMapClick) {
        onMapClick({ lat: e.latlng.lat, lng: e.latlng.lng });
      }
    },
  });
  return null;
}

function FlyTo({ center, zoom }: { center: { lat: number; lng: number }; zoom: number }) {
  const map = useMap();
  useEffect(() => {
    map.flyTo(center, zoom, { duration: 0.8 });
  }, [center.lat, center.lng, zoom, map]);
  return null;
}

function MapView({ spots, center, zoom = 10, onBoundsChange, onMapClick, selectedSpotId }: MapViewProps) {
  const markers = useMemo(
    () =>
      spots.map((spot) => (
        <Marker
          key={spot.id}
          position={[spot.latitude, spot.longitude]}
          icon={createIndexIcon(spot.photographyIndex)}
        >
          <Popup>
            <div className="min-w-[160px]">
              <p className="font-semibold text-surface">{spot.name}</p>
              {spot.photographyIndex != null && (
                <p className="text-sm mt-1">
                  摄影指数: <span className="font-mono font-bold">{spot.photographyIndex}</span>
                </p>
              )}
              <a
                href={`/spot/${spot.id}`}
                className="inline-block mt-2 text-sm text-accent hover:underline"
              >
                查看详情 →
              </a>
            </div>
          </Popup>
        </Marker>
      )),
    [spots],
  );

  return (
    <div className="w-full h-full relative">
      <MapContainer
        center={center}
        zoom={zoom}
        className="w-full h-full z-0"
        zoomControl={false}
      >
        <TileLayer
          attribution='&copy; <a href="https://carto.com/">CARTO</a>'
          url="https://{s}.basemaps.cartocdn.com/dark_all/{z}/{x}/{y}{r}.png"
        />
        <MapEvents onBoundsChange={onBoundsChange} onMapClick={onMapClick} />
        <FlyTo center={center} zoom={zoom} />
        {markers}
      </MapContainer>

      {selectedSpotId && spots.length > 0 && (
        <div className="absolute bottom-4 left-4 right-4 z-[1000]">
          {spots
            .filter((s) => s.id === selectedSpotId)
            .map((spot) => {
              const { bg, text, label } = getPhotoIndexColor(spot.photographyIndex);
              return (
                <div key={spot.id} className="sheet p-4 animate-slide-up">
                  <div className="flex items-center justify-between">
                    <div>
                      <h3 className="font-semibold">{spot.name}</h3>
                      <p className="text-sm text-text-secondary mt-0.5">
                        {spot.latitude.toFixed(4)}, {spot.longitude.toFixed(4)}
                      </p>
                    </div>
                    <div className={`index-badge ${bg} ${text}`}>
                      {spot.photographyIndex ?? '?'} · {label}
                    </div>
                  </div>
                  <a
                    href={`/spot/${spot.id}`}
                    className="block mt-3 text-center btn-primary text-sm"
                  >
                    查看天气详情
                  </a>
                </div>
              );
            })}
        </div>
      )}
    </div>
  );
}

export default MapView;
