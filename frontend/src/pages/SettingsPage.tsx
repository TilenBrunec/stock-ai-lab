import { useState, type FormEvent } from 'react';
import { api } from '../api/client';
import { useAuth } from '../hooks/useAuth';
import './SettingsPage.css';

export default function SettingsPage() {
  const { session, loading, me, setMe } = useAuth();
  const [kljuc, setKljuc] = useState('');
  const [busy, setBusy] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [info, setInfo] = useState<string | null>(null);

  if (loading) return <main className="settings"><p>Nalagam …</p></main>;
  if (!session) {
    return (
      <main className="settings">
        <h1>Nastavitve</h1>
        <p>Za nastavitve se prijavi z Google računom.</p>
      </main>
    );
  }

  async function shrani(e: FormEvent) {
    e.preventDefault();
    setBusy(true);
    setError(null);
    setInfo(null);
    try {
      setMe(await api.saveKey(kljuc));
      setKljuc('');
      setInfo('Ključ je preverjen in shranjen.');
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Shranjevanje ni uspelo');
    } finally {
      setBusy(false);
    }
  }

  async function izbrisi() {
    setBusy(true);
    setError(null);
    setInfo(null);
    try {
      setMe(await api.deleteKey());
      setInfo('Ključ je izbrisan.');
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Brisanje ni uspelo');
    } finally {
      setBusy(false);
    }
  }

  return (
    <main className="settings">
      <h1>Nastavitve</h1>

      <section className="settings__card">
        <h2>OpenRouter API ključ</h2>
        <p className="settings__help">
          Eksperimenti kličejo AI modele prek tvojega računa na{' '}
          <a href="https://openrouter.ai/keys" target="_blank" rel="noreferrer">openrouter.ai/keys</a>.
          Ključ je v bazi shranjen šifrirano in se nikoli ne prikaže v celoti.
        </p>

        <p>
          Trenutni ključ:{' '}
          {me?.imaKljuc
            ? <strong>••••{me.kljucZadnji4}</strong>
            : <span className="settings__none">ni shranjen</span>}
        </p>

        <form onSubmit={shrani} className="settings__form">
          <input
            type="password"
            placeholder="sk-or-v1-…"
            value={kljuc}
            onChange={e => setKljuc(e.target.value)}
            autoComplete="off"
            disabled={busy}
          />
          <button type="submit" disabled={busy || kljuc.trim().length === 0}>
            {busy ? 'Preverjam …' : me?.imaKljuc ? 'Zamenjaj' : 'Shrani'}
          </button>
          {me?.imaKljuc && (
            <button type="button" className="settings__danger" onClick={izbrisi} disabled={busy}>
              Izbriši
            </button>
          )}
        </form>

        {error && <p className="settings__error">{error}</p>}
        {info && <p className="settings__info">{info}</p>}
      </section>
    </main>
  );
}
