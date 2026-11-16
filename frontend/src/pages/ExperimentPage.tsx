import { useEffect, useState } from 'react';
import { Link, useParams } from 'react-router-dom';
import { api } from '../api/client';
import type { Experiment, Status } from '../types/types';
import './Experiment.css';

const STATUS: Record<Status, string> = {
  CAKA: 'čaka na zagon',
  V_TEKU: 'v teku',
  KONCANO: 'končano',
  NEUSPESNO: 'neuspešno',
};

/** Podrobnosti eksperimenta. Zagon in rezultati pridejo v naslednjih sklopih (#67, #69). */
export default function ExperimentPage() {
  const { id } = useParams();
  const [exp, setExp] = useState<Experiment | null>(null);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    api.experiment(Number(id))
      .then(setExp)
      .catch(e => setError(e.message));
  }, [id]);

  if (error) {
    return (
      <main className="exp">
        <p className="exp__error">Eksperimenta ni mogoče naložiti: {error}</p>
        <Link to="/eksperimenti/nov">Nov eksperiment</Link>
      </main>
    );
  }
  if (!exp) return <main className="exp"><p>Nalagam …</p></main>;

  return (
    <main className="exp">
      <h1>Eksperiment #{exp.id}</h1>
      <p>
        <span className={`exp__status exp__status--${exp.status.toLowerCase()}`}>{STATUS[exp.status]}</span>
      </p>

      <section className="exp__card">
        <h2>Parametri</h2>
        <dl className="exp__params">
          <dt>Odločitveni datum</dt><dd>{exp.odlocitveniDatum}</dd>
          <dt>Zgodovina za model</dt><dd>{exp.letaZgodovine} {exp.letaZgodovine === 1 ? 'leto' : 'let'}</dd>
          <dt>Konec vrednotenja</dt><dd>{exp.konecVrednotenja}</dd>
          <dt>Kapital</dt><dd>{exp.kapital.toLocaleString('sl-SI')} €</dd>
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

      <section className="exp__card">
        <h2>Modeli</h2>
        <table className="exp__table">
          <thead>
            <tr><th>Model</th><th>Status</th></tr>
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
                  {STATUS[z.status]}
                  {z.napaka && <div className="exp__error">{z.napaka}</div>}
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      </section>
    </main>
  );
}
