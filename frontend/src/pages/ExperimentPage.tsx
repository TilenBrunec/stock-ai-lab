import { useCallback, useEffect, useState } from 'react';
import { Link, useParams } from 'react-router-dom';
import { api } from '../api/client';
import { useAuth } from '../hooks/useAuth';
import type { Experiment, Status } from '../types/types';
import './Experiment.css';

const STATUS: Record<Status, string> = {
  CAKA: 'čaka na zagon',
  V_TEKU: 'v teku',
  KONCANO: 'končano',
  NEUSPESNO: 'neuspešno',
};

const OSVEZEVANJE_MS = 3000;

const eur = (v: number | null) =>
  v === null ? '–' : `${v.toLocaleString('sl-SI', { minimumFractionDigits: 2, maximumFractionDigits: 2 })} €`;

/** Število s predznakom (+/−), ne samo barvo. */
const odstotek = (v: number | null) =>
  v === null ? '–' : `${v > 0 ? '+' : v < 0 ? '−' : ''}${Math.abs(v).toLocaleString('sl-SI', { maximumFractionDigits: 2 })} %`;

const zasluzekText = (v: number | null) =>
  v === null ? '–' : `${v > 0 ? '+' : v < 0 ? '−' : ''}${eur(Math.abs(v))}`;

const barva = (v: number | null) => (v === null || v === 0 ? '' : v > 0 ? 'exp__up' : 'exp__down');

/** #67: podrobnosti eksperimenta, zagon in spremljanje statusa. Podrobne odločitve pridejo v #69. */
export default function ExperimentPage() {
  const { id } = useParams();
  const { me } = useAuth();
  const [exp, setExp] = useState<Experiment | null>(null);
  const [error, setError] = useState<string | null>(null);
  const [zaganjam, setZaganjam] = useState(false);

  const nalozi = useCallback(() => {
    api.experiment(Number(id))
      .then(e => { setExp(e); setError(null); })
      .catch(e => setError(e.message));
  }, [id]);

  useEffect(nalozi, [nalozi]);

  // med izvajanjem osvežuj status vsake 3 s
  useEffect(() => {
    if (exp?.status !== 'V_TEKU') return;
    const t = setInterval(nalozi, OSVEZEVANJE_MS);
    return () => clearInterval(t);
  }, [exp?.status, nalozi]);

  async function zazeni() {
    setZaganjam(true);
    setError(null);
    try {
      setExp(await api.runExperiment(Number(id)));
    } catch (e) {
      setError(e instanceof Error ? e.message : 'Zagon ni uspel');
    } finally {
      setZaganjam(false);
    }
  }

  if (!exp) {
    return (
      <main className="exp">
        {error
          ? <p className="exp__error">Eksperimenta ni mogoče naložiti: {error}</p>
          : <p>Nalagam …</p>}
      </main>
    );
  }

  const brezKljuca = me !== null && !me.imaKljuc;
  const placljivi = exp.zagoni.filter(z => z.placljiv).length;

  return (
    <main className="exp">
      <h1>Eksperiment #{exp.id}</h1>

      <div className="exp__actions">
        <span className={`exp__status exp__status--${exp.status.toLowerCase()}`}>
          {exp.status === 'V_TEKU' && <span className="exp__spinner" />}
          {STATUS[exp.status]}
        </span>

        {exp.status === 'CAKA' && (
          <button type="button" className="exp__run" onClick={zazeni} disabled={zaganjam || brezKljuca}>
            {zaganjam ? 'Zaganjam …' : 'Zaženi eksperiment'}
          </button>
        )}
        {exp.status === 'CAKA' && placljivi > 0 && (
          <span className="exp__muted">{placljivi} plačljivi model(i) bodo porabili kredit na tvojem OpenRouter računu.</span>
        )}
        {exp.status === 'V_TEKU' && (
          <span className="exp__muted">Modeli se kličejo eden za drugim, kar lahko traja nekaj minut. Stran se osvežuje sama.</span>
        )}
      </div>

      {brezKljuca && exp.status === 'CAKA' && (
        <p className="exp__warning">
          Za zagon potrebuješ OpenRouter ključ. <Link to="/nastavitve">Shrani ga v nastavitvah.</Link>
        </p>
      )}
      {error && <p className="exp__error">{error}</p>}

      <section className="exp__card">
        <h2>Modeli</h2>
        <table className="exp__table">
          <thead>
            <tr>
              <th>Model</th>
              <th>Status</th>
              <th className="exp__num">Končna vrednost</th>
              <th className="exp__num">Zaslužek</th>
              <th className="exp__num">Donos</th>
              <th className="exp__num">CAGR</th>
              <th className="exp__num">Pravilnih</th>
            </tr>
          </thead>
          <tbody>
            {exp.zagoni.map(z => (
              <tr key={z.id}>
                <td>
                  {z.model}{' '}
                  <span className={`exp__badge ${z.placljiv ? 'exp__badge--paid' : 'exp__badge--free'}`}>
                    {z.placljiv ? 'plačljiv' : 'brezplačen'}
                  </span>
                </td>
                <td>
                  {z.status === 'V_TEKU' && <span className="exp__spinner" />}
                  {STATUS[z.status]}
                  {z.napaka && <div className="exp__error">{z.napaka}</div>}
                </td>
                <td className="exp__num">{eur(z.koncnaVrednost)}</td>
                <td className={`exp__num ${barva(z.zasluzek)}`}>{zasluzekText(z.zasluzek)}</td>
                <td className={`exp__num ${barva(z.donos)}`}>{odstotek(z.donos)}</td>
                <td className={`exp__num ${barva(z.cagr)}`}>{odstotek(z.cagr)}</td>
                <td className="exp__num">
                  {z.delezPravilnih === null ? '–' : `${z.delezPravilnih.toLocaleString('sl-SI')} %`}
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      </section>

      <section className="exp__card">
        <h2>Parametri</h2>
        <dl className="exp__params">
          <dt>Odločitveni datum</dt><dd>{exp.odlocitveniDatum}</dd>
          <dt>Zgodovina za model</dt><dd>{exp.letaZgodovine} {exp.letaZgodovine === 1 ? 'leto' : 'let'}</dd>
          <dt>Konec vrednotenja</dt><dd>{exp.konecVrednotenja}</dd>
          <dt>Kapital</dt><dd>{eur(exp.kapital)}</dd>
        </dl>
      </section>

      <section className="exp__card">
        <h2>Instrumenti ({exp.instrumenti.length})</h2>
        <div className="exp__chips">
          {exp.instrumenti.map(s => (
            <Link key={s.id} to={`/stocks/${s.id}`} className="exp__chip" title={s.ime}>{s.ticker}</Link>
          ))}
        </div>
      </section>
    </main>
  );
}
