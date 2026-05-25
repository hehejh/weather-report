import type { DailyForecast } from '../types';
import { getPhotoIndexColor } from '../utils/photoIndexColor';

function formatDay(iso: string): string {
  try {
    const d = new Date(iso);
    const today = new Date();
    const tomorrow = new Date(today);
    tomorrow.setDate(tomorrow.getDate() + 1);

    if (d.toDateString() === today.toDateString()) return '今天';
    if (d.toDateString() === tomorrow.toDateString()) return '明天';

    const days = ['周日', '周一', '周二', '周三', '周四', '周五', '周六'];
    return days[d.getDay()];
  } catch {
    return '—';
  }
}

function formatDateShort(iso: string): string {
  try {
    const d = new Date(iso);
    return `${d.getMonth() + 1}/${d.getDate()}`;
  } catch {
    return '—';
  }
}

interface SevenDayForecastProps {
  forecast: DailyForecast[] | null | undefined;
}

export default function SevenDayForecast({ forecast }: SevenDayForecastProps) {
  if (!forecast || forecast.length === 0) {
    return (
      <div className="bg-surface-card rounded-xl p-6 border border-white/5 text-center text-text-muted">
        暂无预报数据
      </div>
    );
  }

  return (
    <div className="bg-surface-card rounded-xl p-6 border border-white/5">
      <h3 className="text-sm font-semibold text-text-secondary uppercase tracking-wider mb-4">
        7日预报
      </h3>
      <div className="flex gap-3 overflow-x-auto no-scrollbar pb-1">
        {forecast.map((day, i) => {
          const { bg, text } = getPhotoIndexColor(day.photographyIndex);
          return (
            <div
              key={i}
              className="flex-shrink-0 w-[100px] bg-surface rounded-xl p-3 text-center
                         border border-white/5 hover:border-white/10 transition-colors"
            >
              <p className="text-xs font-medium text-text-secondary">{formatDay(day.date)}</p>
              <p className="text-xs text-text-muted">{formatDateShort(day.date)}</p>
              <div className={`mt-2 text-2xl font-bold font-mono ${text}`}>
                {day.photographyIndex}
              </div>
              <div className={`inline-block px-2 py-0.5 rounded-full text-xs mt-1 ${bg} ${text}`}>
                {day.photographyIndex >= 70 ? '极佳' : day.photographyIndex >= 40 ? '一般' : '不佳'}
              </div>
              <div className="mt-2 flex justify-center gap-1 text-xs text-text-secondary">
                <span className="text-accent-warm">{Math.round(day.tempMax)}°</span>
                <span className="text-text-muted">/</span>
                <span className="text-accent">{Math.round(day.tempMin)}°</span>
              </div>
              {day.precipitationProbability > 0 && (
                <p className="text-xs text-accent mt-1">💧{day.precipitationProbability}%</p>
              )}
              {day.morningGlow && day.morningGlow.probability > 60 && (
                <p className="text-xs text-accent-warm mt-0.5">🌅{day.morningGlow.probability}%</p>
              )}
              {day.eveningGlow && day.eveningGlow.probability > 60 && (
                <p className="text-xs text-accent-warm mt-0.5">🌇{day.eveningGlow.probability}%</p>
              )}
            </div>
          );
        })}
      </div>
    </div>
  );
}
