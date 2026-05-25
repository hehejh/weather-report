import { useState, useCallback } from 'react';
import { Link } from 'react-router-dom';
import MapView from '../components/MapView';
import { useGeolocation } from '../hooks/useGeolocation';
import { listSpots, createSpot } from '../api/spots';
import type { PhotoSpot, PhotoSpotRequest, MapBounds } from '../types';
import { getPhotoIndexColor } from '../utils/photoIndexColor';

export default function HomePage() {
  const { center, error: geoError } = useGeolocation();
  const [spots, setSpots] = useState<PhotoSpot[]>([]);
  const [loading, setLoading] = useState(false);
  const [selectedSpotId, setSelectedSpotId] = useState<number | null>(null);
  const [showAddForm, setShowAddForm] = useState(false);
  const [newSpotPos, setNewSpotPos] = useState<{ lat: number; lng: number } | null>(null);
  const [formData, setFormData] = useState({ name: '', tags: '', notes: '' });

  const onBoundsChange = useCallback(async (bounds: MapBounds) => {
    setLoading(true);
    try {
      const data = await listSpots(bounds);
      setSpots(data);
    } catch {
      // silently fail — map still works without spots
    } finally {
      setLoading(false);
    }
  }, []);

  const onMapClick = useCallback((latlng: { lat: number; lng: number }) => {
    setNewSpotPos(latlng);
    setShowAddForm(true);
  }, []);

  const handleAddSpot = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!newSpotPos || !formData.name.trim()) return;

    const request: PhotoSpotRequest = {
      name: formData.name.trim(),
      latitude: newSpotPos.lat,
      longitude: newSpotPos.lng,
      tags: formData.tags ? formData.tags.split(',').map((t) => t.trim()).filter(Boolean) : [],
      notes: formData.notes || null,
      photoUrl: null,
    };

    try {
      const spot = await createSpot(request);
      setSpots((prev) => [spot, ...prev]);
      setSelectedSpotId(spot.id);
      setShowAddForm(false);
      setFormData({ name: '', tags: '', notes: '' });
      setNewSpotPos(null);
    } catch (err) {
      // handle error
    }
  };

  const sortedSpots = [...spots].sort((a, b) => (b.photographyIndex ?? -1) - (a.photographyIndex ?? -1));

  return (
    <div className="relative h-[100dvh] flex flex-col">
      {/* Map area — 60% of screen */}
      <div className="flex-1 relative">
        <MapView
          spots={spots}
          center={center}
          zoom={10}
          onBoundsChange={onBoundsChange}
          onMapClick={onMapClick}
          selectedSpotId={selectedSpotId}
        />
        {loading && (
          <div className="absolute top-3 left-1/2 -translate-x-1/2 z-[1000] bg-surface-card/90 backdrop-blur px-3 py-1 rounded-full text-xs text-text-secondary">
            加载中...
          </div>
        )}
      </div>

      {/* Bottom sheet — 40% */}
      <div className="h-[40%] flex flex-col sheet">
        <div className="p-4 pb-2 flex items-center justify-between">
          <h2 className="font-semibold">我的拍摄点</h2>
          <span className="text-xs text-text-muted">{spots.length} 个地点</span>
        </div>

        <div className="flex-1 overflow-y-auto px-4 pb-4">
          {geoError && (
            <p className="text-sm text-accent-bad mb-3">{geoError}，使用默认位置</p>
          )}

          {sortedSpots.length === 0 ? (
            <div className="text-center text-text-muted py-8">
              <p className="text-4xl mb-2">📸</p>
              <p className="text-sm">点击地图添加你的第一个拍摄点</p>
              <p className="text-xs mt-1">长按或点击地图任意位置开始</p>
            </div>
          ) : (
            <div className="space-y-2">
              {sortedSpots.map((spot) => {
                const { bg, text, label } = getPhotoIndexColor(spot.photographyIndex);
                return (
                  <Link
                    key={spot.id}
                    to={`/spot/${spot.id}`}
                    className="flex items-center gap-3 p-3 rounded-xl bg-surface-card
                               border border-white/5 hover:border-white/10 transition-colors"
                    onClick={() => setSelectedSpotId(spot.id)}
                  >
                    <div className={`index-badge text-xs ${bg} ${text} min-w-[56px] justify-center`}>
                      {spot.photographyIndex ?? '?'}
                    </div>
                    <div className="flex-1 min-w-0">
                      <p className="text-sm font-medium truncate">{spot.name}</p>
                      <p className="text-xs text-text-muted">
                        {spot.latitude.toFixed(4)}, {spot.longitude.toFixed(4)}
                      </p>
                    </div>
                    <span className={`text-xs font-medium ${text}`}>{label}</span>
                  </Link>
                );
              })}
            </div>
          )}
        </div>
      </div>

      {/* Add spot modal */}
      {showAddForm && newSpotPos && (
        <div className="fixed inset-0 z-[2000] flex items-end justify-center bg-black/50 animate-fade-in"
             onClick={() => setShowAddForm(false)}>
          <form
            onSubmit={handleAddSpot}
            onClick={(e) => e.stopPropagation()}
            className="sheet w-full max-w-lg p-6 animate-slide-up space-y-4"
          >
            <h3 className="font-semibold">添加拍摄点</h3>
            <p className="text-xs text-text-muted">
              位置: {newSpotPos.lat.toFixed(6)}, {newSpotPos.lng.toFixed(6)}
            </p>
            <input
              className="input-field w-full"
              placeholder="地点名称 *"
              value={formData.name}
              onChange={(e) => setFormData((f) => ({ ...f, name: e.target.value }))}
              required
            />
            <input
              className="input-field w-full"
              placeholder="标签（逗号分隔，如：日出,海滩,城市）"
              value={formData.tags}
              onChange={(e) => setFormData((f) => ({ ...f, tags: e.target.value }))}
            />
            <textarea
              className="input-field w-full resize-none"
              placeholder="备注（可选）"
              rows={2}
              value={formData.notes}
              onChange={(e) => setFormData((f) => ({ ...f, notes: e.target.value }))}
            />
            <div className="flex gap-3">
              <button type="button" onClick={() => setShowAddForm(false)} className="btn-secondary flex-1">
                取消
              </button>
              <button type="submit" className="btn-primary flex-1">
                保存
              </button>
            </div>
          </form>
        </div>
      )}
    </div>
  );
}
