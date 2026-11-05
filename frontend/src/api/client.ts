import type { Price, Stock } from '../types/types';

const API_URL = import.meta.env.VITE_API_URL ?? 'http://localhost:8080';

async function get<T>(path: string, params?: Record<string, string | undefined>): Promise<T> {
  const url = new URL(path, API_URL);
  Object.entries(params ?? {}).forEach(([k, v]) => {
    if (v) url.searchParams.set(k, v);
  });
  const res = await fetch(url);
  if (!res.ok) {
    throw new Error(`${res.status} ${res.statusText}`);
  }
  return res.json() as Promise<T>;
}

export const api = {
  stocks: (q?: string) => get<Stock[]>('/api/stocks', { q }),
  prices: (id: number, from?: string, to?: string) =>
    get<Price[]>(`/api/stocks/${id}/prices`, { from, to }),
};