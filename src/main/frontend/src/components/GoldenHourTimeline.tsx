import type { SolarTimes } from '../types';

function formatTime(iso: string): string {
  try {
    return new Date(iso).toLocaleTimeString('zh-CN', { hour: '2-digit', minute: '2-digit' });
  } catch {
    return '--:--';
  }
}

interface GoldenHourTimelineProps {
  solar: SolarTimes | null;
}

const phases = [
  { key: 'blueHourMorning' as const, label: '蓝色时刻', color: 'bg-blue-500', icon: '🌌' },
  { key: 'goldenHourMorning' as const, label: '金色时刻', color: 'bg-amber-500', icon: '🌅' },
  { key: 'sunrise' as const, label: '日出', color: 'bg-orange-500', icon: '☀️' },
  { key: 'sunset' as const, label: '日落', color: 'bg-orange-600', icon: '🌇' },
  { key: 'goldenHourEvening' as const, label: '金色时刻', color: 'bg-amber-500', icon: '🌄' },
  { key: 'blueHourEvening' as const, label: '蓝色时刻', color: 'bg-indigo-500', icon: '🌃' },
];

export default function GoldenHourTimeline({ solar }: GoldenHourTimelineProps) {
  if (!solar) {
    return (
      <div className="bg-surface-card rounded-xl p-6 border border-white/5 text-center text-text-muted">
        暂无晨昏数据
      </div>
    );
  }

  return (
    <div className="bg-surface-card rounded-xl p-6 border border-white/5">
      <h3 className="text-sm font-semibold text-text-secondary uppercase tracking-wider mb-4">
        今日光影时间线
      </h3>
      <div className="relative">
        {/* Line */}
        <div className="absolute top-4 left-0 right-0 h-0.5 bg-white/10" />

        {/* Phases */}
        <div className="relative flex justify-between">
          {phases.map((phase) => {
            const time = solar[phase.key];
            if (!time) return null;
            return (
              <div key={phase.key} className="flex flex-col items-center gap-2">
                <div className={`timeline-dot ${phase.color}`} />
                <span className="text-xs font-mono text-text-primary">{formatTime(time)}</span>
                <span className="text-xs text-text-muted text-center leading-tight">
                  {phase.icon}<br />{phase.label}
                </span>
              </div>
            );
          })}
        </div>
      </div>
    </div>
  );
}
