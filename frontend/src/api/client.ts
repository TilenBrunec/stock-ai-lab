import { supabase } from '../config/supabase';
import type { Me, Price, Stock } from '../types/types';

const API_URL = import.meta.env.VITE_API_URL ?? 'http://localhost:8080';

export class ApiError extends Error {
  status: number;

  constructor(status: number, message: string) {
    super(message);
    this.status = status;
  }
}

async function authHeader(): Promise<Record<string, string>> {
  const { data } = await supabase.auth.getSession();   // po potrebi sam osveži žeton
  const token = data.session?.access_token;
  return token ? { Authorization: `Bearer ${token}` } : {};
}

async function request<T>(
  method: string,
  path: string,
  options: { params?: Record<string, string | undefined>; body?: unknown } = {},
): Promise<T> {
  const url = new URL(path, API_URL);
  Object.entries(options.params ?? {}).forEach(([k, v]) => {
    if (v) url.searchParams.set(k, v);
  });

  const headers: Record<string, string> = { ...(await authHeader()) };
  if (options.body !== undefined) {
    headers['Content-Type'] = 'application/json';
  }

  const res = await fetch(url, {
    method,
    headers,
    body: options.body !== undefined ? JSON.stringify(options.body) : undefined,
  });

  if (!res.ok) {
    throw new ApiError(res.status, `${res.status} ${res.statusText}`);
  }
  return (res.status === 204 ? undefined : await res.json()) as T;
}

export const api = {
  stocks: (q?: string) => request<Stock[]>('GET', '/api/stocks', { params: { q } }),
  prices: (id: number, from?: string, to?: string) =>
    request<Price[]>('GET', `/api/stocks/${id}/prices`, { params: { from, to } }),
  me: () => request<Me>('GET', '/api/me'),
};