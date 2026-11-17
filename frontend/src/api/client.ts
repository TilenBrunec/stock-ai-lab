import { supabase } from '../config/supabase';
import type {
  CreateExperimentRequest,
  DateRange,
  Experiment,
  ExperimentSummary,
  LlmModel,
  Me,
  Price,
  Stock,
} from '../types/types';

const API_URL = import.meta.env.VITE_API_URL ?? 'http://localhost:8080';

export class ApiError extends Error {
  status: number;
  /** napake po poljih pri 400 (npr. { kapital: "Kapital mora biti večji od 0 €" }) */
  napake: Record<string, string>;

  constructor(status: number, message: string, napake: Record<string, string> = {}) {
    super(message);
    this.status = status;
    this.napake = napake;
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
    // backend vrne {"message": "...", "napake": {...}}
    let message = `${res.status} ${res.statusText}`;
    let napake: Record<string, string> = {};
    try {
      const data = await res.json();
      if (data?.message) message = data.message;
      if (data?.napake) napake = data.napake;
    } catch {
      // telo ni JSON
    }
    throw new ApiError(res.status, message, napake);
  }
  return (res.status === 204 ? undefined : await res.json()) as T;
}

export const api = {
  stocks: (q?: string) => request<Stock[]>('GET', '/api/stocks', { params: { q } }),
  prices: (id: number, from?: string, to?: string) =>
    request<Price[]>('GET', `/api/stocks/${id}/prices`, { params: { from, to } }),
  stockRange: () => request<DateRange>('GET', '/api/stocks/range'),
  randomStocks: (n: number, od?: string) =>
    request<Stock[]>('GET', '/api/stocks/random', { params: { n: String(n), od } }),

  models: () => request<LlmModel[]>('GET', '/api/models'),

  me: () => request<Me>('GET', '/api/me'),
  saveKey: (kljuc: string) => request<Me>('PUT', '/api/me/openrouter-key', { body: { kljuc } }),
  deleteKey: () => request<Me>('DELETE', '/api/me/openrouter-key'),

  createExperiment: (body: CreateExperimentRequest) =>
    request<Experiment>('POST', '/api/experiments', { body }),
  experiments: () => request<ExperimentSummary[]>('GET', '/api/experiments'),
  experiment: (id: number) => request<Experiment>('GET', `/api/experiments/${id}`),
  runExperiment: (id: number) => request<Experiment>('POST', `/api/experiments/${id}/run`),
};
