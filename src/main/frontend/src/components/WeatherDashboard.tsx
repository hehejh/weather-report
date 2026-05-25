import type { CurrentWeather, GlowForecast, SolarTimes } from '../types';
import { getPhotoIndexColor } from '../utils/photoIndexColor';

function formatTemp(c: number) {
  return `${c > 0 ? '+' : ''}${Math.round(c)}°`;
}

function windLabel(deg: string | undefined) {
  const map: Record<string, string> = {
    N: '北', NE: '东北', E: '东', SE: '东南',
    S: '南', SW: '西南', W: '西', NW: '西北',
  };
  if (!deg) return '';
  for (const key of Object.keys(map)) {
    if (deg.startsWith(key)) return map[key] + '风';
  }
  return deg;
}

interface MetricCardProps {
  label: string;
  value: string | number;
  unit?: string;
  sub?: string;
}

function MetricCard({ label, value, unit, sub }: MetricCardProps) {
  return (
    <div className="metric-card">
      <div className="metric-value">
        {value}
        {unit && <span className="text-sm font-normal text-text-secondary ml-0.5">{unit}</span>}
      </div>
      <div className="metric-label">{label}</div>
      {sub && <div className="text-xs text-text-muted mt-0.5">{sub}</div>}
    </div>
  );
}

interface WeatherDashboardProps {
  photographyIndex: number | null | undefined;
  indexLabel: string | null | undefined;
  current: CurrentWeather | null;
  solar: SolarTimes | null;
  glow: GlowForecast | null;
  spotName: string;
}

export default function WeatherDashboard({
  photographyIndex,
  indexLabel,
  current,
  glow,
  spotName,
}: WeatherDashboardProps) {
  const { bg, text } = getPhotoIndexColor(photographyIndex);

  return (
    <div className="animate-fade-in">
      {/* Index hero */}
      <div className={`${bg} rounded-2xl p-6 mb-6 text-center`}>
        <p className="text-sm text-text-secondary mb-1">{spotName} · 摄影指数</p>
        <div className={`text-5xl font-bold font-mono ${text}`}>
          {photographyIndex ?? '—'}
        </div>
        <div className={`text-lg font-medium mt-1 ${text}`}>{indexLabel ?? '加载中'}</div>
        {glow && (
          <p className="text-sm text-text-secondary mt-2">
            {glow.type} · 概率 {glow.probability}% · {glow.quality}
          </p>
        )}
      </div>

      {/* Metric grid */}
      {current && (
        <div className="grid grid-cols-2 gap-3 mb-6">
          <MetricCard label="温度" value={formatTemp(current.temperature)} sub={`体感 ${formatTemp(current.feelsLike)}`} />
          <MetricCard label="湿度" value={current.humidity} unit="%" sub={current.humidity > 70 ? '偏高' : current.humidity < 40 ? '偏低' : '适中'} />
          <MetricCard label="风速" value={current.windSpeed} unit="km/h" sub={windLabel(current.windDirection)} />
          <MetricCard label="能见度" value={current.visibility} unit="km" sub={current.visibility >= 15 ? '极好' : current.visibility >= 10 ? '良好' : '一般'} />
          <MetricCard label="空气质量" value={current.aqi} sub={current.aqi <= 50 ? '优' : current.aqi <= 100 ? '良' : '差'} />
          <MetricCard label="云量" value={current.totalCloud} unit="%" sub={current.totalCloud < 30 ? '少云' : current.totalCloud < 70 ? '多云' : '阴天'} />
          <MetricCard label="降水概率" value={current.precipitationProbability} unit="%" />
        </div>
      )}

      {/* Breakdown */}
      {glow?.breakdown && (
        <div className="bg-surface-card rounded-xl p-4 border border-white/5 mb-6">
          <h3 className="text-sm font-semibold text-text-secondary uppercase tracking-wider mb-3">评分细分</h3>
          <div className="space-y-2">
            <BreakdownRow label="云层" score={glow.breakdown.cloudScore} />
            <BreakdownRow label="湿度" score={glow.breakdown.humidityScore} />
            <BreakdownRow label="能见度" score={glow.breakdown.visibilityScore} />
            <BreakdownRow label="空气质量" score={glow.breakdown.aqiScore} />
            <BreakdownRow label="风力" score={glow.breakdown.windScore} />
          </div>
          {glow.breakdown.notes && (
            <p className="text-xs text-text-muted mt-3">{glow.breakdown.notes}</p>
          )}
        </div>
      )}
    </div>
  );
}

function BreakdownRow({ label, score }: { label: string; score: number }) {
  const pct = `${score}%`;
  const color = score >= 70 ? 'bg-accent-good' : score >= 40 ? 'bg-accent-caution' : 'bg-accent-bad';
  return (
    <div className="flex items-center gap-3">
      <span className="text-sm text-text-secondary w-16">{label}</span>
      <div className="flex-1 h-2 bg-surface rounded-full overflow-hidden">
        <div className={`h-full ${color} rounded-full transition-all duration-500`} style={{ width: pct }} />
      </div>
      <span className="text-sm font-mono text-text-primary w-10 text-right">{score}</span>
    </div>
  );
}
