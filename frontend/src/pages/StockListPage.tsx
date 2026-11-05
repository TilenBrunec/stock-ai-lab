import { useEffect, useState } from 'react';
import { Link } from 'react-router-dom';
import { api } from '../api/client';
import type { Stock } from '../types/types';
import './StockListPage.css';

export default function StockListPage() {
  const [query, setQuery] = useState('');
  const [stocks, setStocks] = useState<Stock[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    const t = setTimeout(() => {
      setLoading(true);
      setError(null);
      api.stocks(query)
        .then(setStocks)
        .catch(e => setError(e.message))
        .finally(() => setLoading(false));
    }, 300);
    return () => clearTimeout(t);
  }, [query]);

  return (
    <main className="stock-list">
      <h1>Instrumenti</h1>

      <input
        className="stock-list__search"
        type="search"
        placeholder="Išči po imenu ali tickerju …"
        value={query}
        onChange={e => setQuery(e.target.value)}
      />

      {error && <p className="stock-list__error">Napaka pri nalaganju: {error}</p>}
      {loading && <p>Nalagam …</p>}

      {!loading && !error && (
        <>
          <p className="stock-list__count">{stocks.length} instrumentov</p>
          {stocks.length === 0 ? (
            <p>Ni zadetkov.</p>
          ) : (
            <table className="stock-list__table">
              <thead>
                <tr><th>Ticker</th><th>Ime</th><th>Tip</th></tr>
              </thead>
              <tbody>
                {stocks.map(s => (
                  <tr key={s.id}>
                    <td><Link to={`/stocks/${s.id}`}>{s.ticker}</Link></td>
                    <td><Link to={`/stocks/${s.id}`}>{s.ime}</Link></td>
                    <td>
                      <span className={`badge badge--${s.tip.toLowerCase()}`}>
                        {s.tip === 'DELNICA' ? 'Delnica' : 'Sklad'}
                      </span>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          )}
        </>
      )}
    </main>
  );
}