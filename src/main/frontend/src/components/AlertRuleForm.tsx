import { useState } from 'react';
import type { AlertRuleRequest } from '../types';

const DEFAULT_FORM: AlertRuleRequest = {
  alertType: 'GLOW_PROBABILITY',
  glowProbability: 70,
  maxCloud: null,
  maxWind: null,
  minVisibility: null,
  minTemp: null,
  maxTemp: null,
  pushTime: '06:00',
};

interface SliderFieldProps {
  label: string;
  value: number | null;
  min: number;
  max: number;
  step: number;
  unit?: string;
  onChange: (v: number | null) => void;
}

function SliderField({ label, value, min, max, step, unit, onChange }: SliderFieldProps) {
  return (
    <div className="space-y-2">
      <div className="flex justify-between items-center">
        <label className="text-sm text-text-secondary">{label}</label>
        <span className="text-sm font-mono text-text-primary">
          {value != null ? `${value}${unit ?? ''}` : '不限'}
        </span>
      </div>
      <input
        type="range"
        min={min}
        max={max}
        step={step}
        value={value ?? min}
        onChange={(e) => onChange(Number(e.target.value))}
        className="w-full h-1.5 bg-surface rounded-full appearance-none cursor-pointer
                   accent-accent [&::-webkit-slider-thumb]:appearance-none
                   [&::-webkit-slider-thumb]:w-4 [&::-webkit-slider-thumb]:h-4
                   [&::-webkit-slider-thumb]:rounded-full [&::-webkit-slider-thumb]:bg-accent"
      />
      <div className="flex justify-between text-xs text-text-muted">
        <span>{min}{unit ?? ''}</span>
        <span>{max}{unit ?? ''}</span>
      </div>
    </div>
  );
}

interface AlertRuleFormProps {
  initial?: Partial<AlertRuleRequest>;
  onSubmit: (data: AlertRuleRequest) => void;
  onCancel?: () => void;
  loading?: boolean;
}

export default function AlertRuleForm({ initial, onSubmit, onCancel, loading }: AlertRuleFormProps) {
  const [form, setForm] = useState<AlertRuleRequest>({
    ...DEFAULT_FORM,
    ...initial,
  });

  const update = <K extends keyof AlertRuleRequest>(key: K, value: AlertRuleRequest[K]) => {
    setForm((f) => ({ ...f, [key]: value }));
  };

  const handleSubmit = (e: React.FormEvent) => {
    e.preventDefault();
    onSubmit(form);
  };

  return (
    <form onSubmit={handleSubmit} className="bg-surface-card rounded-xl p-6 border border-white/5 space-y-5 animate-fade-in">
      <h3 className="font-semibold text-text-primary">
        {initial ? '编辑提醒规则' : '创建提醒规则'}
      </h3>

      {/* Alert type */}
      <div className="space-y-1">
        <label className="text-sm text-text-secondary">提醒类型</label>
        <select
          value={form.alertType}
          onChange={(e) => update('alertType', e.target.value)}
          className="input-field w-full"
        >
          <option value="GLOW_PROBABILITY">霞光概率</option>
          <option value="PHOTOGRAPHY_INDEX">摄影指数</option>
        </select>
      </div>

      {/* Push time */}
      <div className="space-y-1">
        <label className="text-sm text-text-secondary">推送时间</label>
        <input
          type="time"
          value={form.pushTime}
          onChange={(e) => update('pushTime', e.target.value)}
          className="input-field w-full"
        />
      </div>

      {/* Glow probability */}
      <SliderField
        label="霞光概率阈值"
        value={form.glowProbability}
        min={0}
        max={100}
        step={5}
        unit="%"
        onChange={(v) => update('glowProbability', v ?? 0)}
      />

      {/* Max cloud */}
      <SliderField
        label="最大云量"
        value={form.maxCloud}
        min={0}
        max={100}
        step={5}
        unit="%"
        onChange={(v) => update('maxCloud', v)}
      />

      {/* Max wind */}
      <SliderField
        label="最大风速"
        value={form.maxWind}
        min={0}
        max={12}
        step={1}
        unit="级"
        onChange={(v) => update('maxWind', v)}
      />

      {/* Min visibility */}
      <SliderField
        label="最低能见度"
        value={form.minVisibility}
        min={0}
        max={100}
        step={5}
        unit="km"
        onChange={(v) => update('minVisibility', v)}
      />

      {/* Min temp */}
      <SliderField
        label="最低温度"
        value={form.minTemp}
        min={-30}
        max={50}
        step={1}
        unit="°"
        onChange={(v) => update('minTemp', v)}
      />

      {/* Max temp */}
      <SliderField
        label="最高温度"
        value={form.maxTemp}
        min={-30}
        max={50}
        step={1}
        unit="°"
        onChange={(v) => update('maxTemp', v)}
      />

      {/* Actions */}
      <div className="flex gap-3 pt-2">
        {onCancel && (
          <button type="button" onClick={onCancel} className="btn-secondary flex-1">
            取消
          </button>
        )}
        <button type="submit" disabled={loading} className="btn-primary flex-1">
          {loading ? '保存中...' : '保存'}
        </button>
      </div>
    </form>
  );
}
