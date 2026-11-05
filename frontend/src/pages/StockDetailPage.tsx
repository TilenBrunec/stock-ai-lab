import { useEffect, useMemo, useState } from 'react';
import { Link, useParams } from 'react-router-dom';
import ReactECharts from 'echarts-for-react';
import { format, subYears } from 'date-fns';
import { api } from '../api/client';
import type { Price, Stock } from '../types/types';
import './StockDetailPage.css';

type Period = '1L' | '5L' | 'VSE';

const PERIODS: { key: Period; label: string; years?: number }[] = [
  { key: '1L', label: '1 leto', years: 1 },
  { key: '5L', label: '5 let', years: 5 },
  { key: 'VSE', label: 'Vse' },
];

export default function StockDetailPage() {
  const { id } = useParams();
  const stockId = Number(id);

  const [stock, setStock] = useState<Stock | null>(null);
  const [prices, setPrices] = useState<Price[]>([]);
  const [period, setPeriod] = useState<Period>('1L');
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  // podatki o instrumentu (ime, ticker, tip)
  useEffect(() => {
    api.stocks()
      .then(list => setStock(list.find(s => s.id === stockId) ?? null))
      .catch(e => setError(e.message));
  }, [stockId]);

  // cene za izbrano obdobje
  useEffect(() => {
    const years = PERIODS.find(p => p.key === period)?.years;
    const from = years ? format(subYears(new Date(), years), 'yyyy-MM-dd') : undefined;

    setLoading(true);
    setError(null);
    api.prices(stockId, from)
      .then(setPrices)
      .catch(e => setError(e.message))
      .finally(() => setLoading(false));
  }, [stockId, period]);

  const change = useMemo(() => {
    if (prices.length < 2) return null;
    const first = prices[0].cena;
    const last = prices[prices.length - 1].cena;
    return ((last - first) / first) * 100;
  }, [prices]);

  const option = useMemo(() => ({
    grid: { left: 60, right: 20, top: 20, bottom: 60 },
    tooltip: {
      trigger: 'axis',
      valueFormatter: (v: number) => v.toFixed(2),
    },
    xAxis: { type: 'time' },
    yAxis: { type: 'value', scale: true },
    dataZoom: [{ type: 'inside' }, { type: 'slider', height: 20, bottom: 10 }],
    series: [{
      type: 'line',
      name: stock?.ticker ?? 'Cena',
      showSymbol: false,
      lineStyle: { width: 1.5 },
      areaStyle: { opacity: 0.08 },
      data: prices.map(p => [p.datum, p.cena]),
    }],
  }), [prices, stock]);

  return (
    <main className="stock-detail">
      <Link to="/" className="stock-detail__back">← Nazaj na seznam</Link>

      <header className="stock-detail__header">
        <div>
          <h1>{stock ? stock.ime : 'Instrument'}</h1>
          {stock && (
            <p className="stock-detail__meta">
              {stock.ticker} · {stock.tip === 'DELNICA' ? 'Delnica' : 'Sklad'}
            </p>
          )}
        </div>
        {change !== null && (
          <div className={`stock-detail__change ${change >= 0 ? 'up' : 'down'}`}>
            {change >= 0 ? '+' : ''}{change.toFixed(1)} %
          </div>
        )}
      </header>

      <div className="stock-detail__periods">
        {PERIODS.map(p => (
          <button
            key={p.key}
            className={p.key === period ? 'active' : ''}
            onClick={() => setPeriod(p.key)}
          >
            {p.label}
          </button>
        ))}
      </div>

      {error && <p className="stock-detail__error">Napaka pri nalaganju: {error}</p>}
      {loading && <p>Nalagam …</p>}
      {!loading && !error && prices.length === 0 && <p>Ni podatkov za izbrano obdobje.</p>}
      {!loading && !error && prices.length > 0 && (
        <ReactECharts option={option} style={{ height: 420 }} notMerge />
      )}
    </main>
  );
}