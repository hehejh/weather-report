export interface IndexColor {
  bg: string;
  text: string;
  label: string;
}

const GOOD: IndexColor = { bg: 'bg-accent-good/15', text: 'text-accent-good', label: '极佳' };
const CAUTION: IndexColor = { bg: 'bg-accent-caution/15', text: 'text-accent-caution', label: '一般' };
const BAD: IndexColor = { bg: 'bg-accent-bad/15', text: 'text-accent-bad', label: '不佳' };

export function getPhotoIndexColor(index: number | null | undefined): IndexColor {
  if (index == null) return { bg: 'bg-white/5', text: 'text-text-muted', label: '无数据' };
  if (index >= 70) return GOOD;
  if (index >= 40) return CAUTION;
  return BAD;
}

export function getIndexLabel(index: number | null | undefined): string {
  if (index == null) return '无数据';
  if (index >= 70) return '极佳';
  if (index >= 40) return '一般';
  return '不佳';
}
