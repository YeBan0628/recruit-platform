export function formatSalary(min?: number, max?: number): string {
  if (min == null && max == null) return '面议'
  if (min != null && max != null) return `${min}-${max}K`
  if (min != null) return `${min}K 起`
  return `${max}K 以内`
}

export function formatDateTime(value?: string): string {
  if (!value) return '-'
  return value.replace('T', ' ').slice(0, 16)
}
