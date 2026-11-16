import { useEffect, useMemo, useState, type FormEvent } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { addYears, format, parseISO, subYears } from 'date-fns';
import { api, ApiError } from '../api/client';
import { useAuth } from '../hooks/useAuth';
import type { DateRange, LlmModel, Stock } from '../types/types';
import './Experiment.css';

const MAX_INSTRUMENTOV = 20;
const iso = (d: Date) => format(d, 'yyyy-MM-dd');

export default function NewExperimentPage() {
  const { session, loading: authLoading, me } = useAuth();
  const navigate = useNavigate();

  const [range, setRange] = useState<DateRange | null>(null);
  const [models, setModels] = useState<LlmModel[]>([]);
  const [loadError, setLoadError] = useState<string | null>(null);

  // instrumenti
  const [izbrani, setIzbrani] = useState<Stock[]>([]);
  const [iskanje, setIskanje] = useState('');
  const [zadetki, setZadetki] = useState<Stock[]>([]);
  const [steviloNakljucnih, setSteviloNakljucnih] = useState(5);

  // parametri
  const [odlocitveniDatum, setOdlocitveniDatum] = useState('');
  const [letaZgodovine, setLetaZgodovine] = useState(3);
  const [konecVrednotenja, setKonecVrednotenja] = useState('');
  const [kapital, setKapital] = useState(10000);
  const [modelIds, setModelIds] = useState<number[]>([]);

  const [napake, setNapake] = useState<Record<string, string>>({});
  const [busy, setBusy] = useState(false);

  // razpon podatkov in modeli; privzeto odločitev 5 let pred koncem podatkov
  useEffect(() => {
    Promise.all([api.stockRange(), api.models()])
      .then(([r, m]) => {
        setRange(r);
        setModels(m);
        const konec = parseISO(r.konec);
        setOdlocitveniDatum(iso(subYears(konec, 5)));
        setKonecVrednotenja(r.konec);
      })
      .catch(e => setLoadError(e.message));
  }, []);

  // iskanje instrumentov (300 ms po zadnjem tipkanju)
  useEffect(() => {
    if (iskanje.trim().length < 1) {
      setZadetki([]);
      return;
    }
    const t = setTimeout(() => {
      api.stocks(iskanje).then(s => setZadetki(s.slice(0, 8))).catch(() => setZadetki([]));
    }, 300);
    return () => clearTimeout(t);
  }, [iskanje]);

  // dovoljeni razponi datumov (enaka pravila kot na backendu)
  const meje = useMemo(() => {
    if (!range) return null;
    const zacetek = parseISO(range.zacetek);
    const konec = parseISO(range.konec);
    return {
      odlMin: iso(addYears(zacetek, letaZgodovine)),
      odlMax: iso(subYears(konec, 1)),
      konecMin: odlocitveniDatum ? iso(addYears(parseISO(odlocitveniDatum), 1)) : range.zacetek,
      konecMax: range.konec,
    };
  }, [range, letaZgodovine, odlocitveniDatum]);

  const zacetekZgodovine = odlocitveniDatum
    ? iso(subYears(parseISO(odlocitveniDatum), letaZgodovine))
    : undefined;

  if (authLoading) return <main className="exp"><p>Nalagam …</p></main>;
  if (!session) {
    return (
      <main className="exp">
        <h1>Nov eksperiment</h1>
        <p>Za ustvarjanje eksperimenta se prijavi z Google računom.</p>
      </main>
    );
  }

  function dodaj(s: Stock) {
    setIzbrani(prev =>
      prev.some(p => p.id === s.id) || prev.length >= MAX_INSTRUMENTOV ? prev : [...prev, s]);
    setIskanje('');
    setZadetki([]);
  }

  function odstrani(id: number) {
    setIzbrani(prev => prev.filter(p => p.id !== id));
  }

  async function nakljucno() {
    try {
      setIzbrani(await api.randomStocks(steviloNakljucnih, zacetekZgodovine));
      setNapake(n => ({ ...n, instrumentIds: '' }));
    } catch (e) {
      setNapake(n => ({ ...n, instrumentIds: e instanceof Error ? e.message : 'Napaka' }));
    }
  }

  function preklopiModel(id: number) {
    setModelIds(prev => (prev.includes(id) ? prev.filter(m => m !== id) : [...prev, id]));
  }

  async function oddaj(e: FormEvent) {
    e.preventDefault();
    setBusy(true);
    setNapake({});
    try {
      const eksperiment = await api.createExperiment({
        instrumentIds: izbrani.map(s => s.id),
        odlocitveniDatum,
        letaZgodovine,
        konecVrednotenja,
        kapital,
        modelIds,
      });
      navigate(`/eksperimenti/${eksperiment.id}`);
    } catch (err) {
      if (err instanceof ApiError && Object.keys(err.napake).length > 0) {
        setNapake(err.napake);
      } else {
        setNapake({ splosno: err instanceof Error ? err.message : 'Shranjevanje ni uspelo' });
      }
    } finally {
      setBusy(false);
    }
  }

  const brezKljuca = me !== null && !me.imaKljuc;
  const lahkoOddam = !busy && !brezKljuca && izbrani.length > 0 && modelIds.length > 0;

  return (
    <main className="exp">
      <h1>Nov eksperiment</h1>
      {loadError && <p className="exp__error">Napaka pri nalaganju: {loadError}</p>}

      {brezKljuca && (
        <p className="exp__warning">
          Za zagon eksperimenta potrebuješ OpenRouter ključ. <Link to="/nastavitve">Shrani ga v nastavitvah.</Link>
        </p>
      )}

      <form onSubmit={oddaj}>
        {/* ---------- instrumenti ---------- */}
        <section className="exp__card">
          <h2>1. Instrumenti <span className="exp__muted">({izbrani.length}/{MAX_INSTRUMENTOV})</span></h2>

          <div className="exp__row">
            <div className="exp__search">
              <input
                type="search"
                placeholder="Išči po imenu ali tickerju …"
                value={iskanje}
                onChange={e => setIskanje(e.target.value)}
              />
              {zadetki.length > 0 && (
                <ul className="exp__results">
                  {zadetki.map(s => (
                    <li key={s.id}>
                      <button type="button" onClick={() => dodaj(s)}
                              disabled={izbrani.some(p => p.id === s.id)}>
                        <strong>{s.ticker}</strong> {s.ime}
                      </button>
                    </li>
                  ))}
                </ul>
              )}
            </div>

            <div className="exp__random">
              <input
                type="number"
                min={1}
                max={MAX_INSTRUMENTOV}
                value={steviloNakljucnih}
                onChange={e => setSteviloNakljucnih(Number(e.target.value))}
              />
              <button type="button" onClick={nakljucno}>Naključno izberi</button>
            </div>
          </div>

          <div className="exp__chips">
            {izbrani.length === 0 && <span className="exp__muted">Ni izbranih instrumentov.</span>}
            {izbrani.map(s => (
              <span key={s.id} className="exp__chip" title={s.ime}>
                {s.ticker}
                <button type="button" onClick={() => odstrani(s.id)} aria-label={`Odstrani ${s.ticker}`}>×</button>
              </span>
            ))}
          </div>
          {napake.instrumentIds && <p className="exp__error">{napake.instrumentIds}</p>}
        </section>

        {/* ---------- obdobje in kapital ---------- */}
        <section className="exp__card">
          <h2>2. Obdobje in kapital</h2>
          {range && (
            <p className="exp__muted">Podatki v bazi: {range.zacetek} – {range.konec}</p>
          )}

          <div className="exp__grid">
            <label>
              Odločitveni datum
              <input type="date" value={odlocitveniDatum}
                     min={meje?.odlMin} max={meje?.odlMax}
                     onChange={e => setOdlocitveniDatum(e.target.value)} />
              {meje && <small>dovoljeno {meje.odlMin} – {meje.odlMax}</small>}
              {napake.odlocitveniDatum && <span className="exp__error">{napake.odlocitveniDatum}</span>}
            </label>

            <label>
              Leta zgodovine, ki jih vidi model
              <select value={letaZgodovine} onChange={e => setLetaZgodovine(Number(e.target.value))}>
                {[1, 2, 3, 4, 5].map(l => <option key={l} value={l}>{l}</option>)}
              </select>
              {zacetekZgodovine && <small>model vidi cene od {zacetekZgodovine}</small>}
              {napake.letaZgodovine && <span className="exp__error">{napake.letaZgodovine}</span>}
            </label>

            <label>
              Konec vrednotenja
              <input type="date" value={konecVrednotenja}
                     min={meje?.konecMin} max={meje?.konecMax}
                     onChange={e => setKonecVrednotenja(e.target.value)} />
              {meje && <small>dovoljeno {meje.konecMin} – {meje.konecMax}</small>}
              {napake.konecVrednotenja && <span className="exp__error">{napake.konecVrednotenja}</span>}
            </label>

            <label>
              Kapital (€)
              <input type="number" min={1} step={100} value={kapital}
                     onChange={e => setKapital(Number(e.target.value))} />
              {napake.kapital && <span className="exp__error">{napake.kapital}</span>}
            </label>
          </div>
        </section>

        {/* ---------- modeli ---------- */}
        <section className="exp__card">
          <h2>3. Modeli</h2>
          {models.length === 0 && <p className="exp__muted">Ni aktivnih modelov.</p>}
          <div className="exp__models">
            {models.map(m => (
              <label key={m.id} className="exp__model">
                <input type="checkbox" checked={modelIds.includes(m.id)} onChange={() => preklopiModel(m.id)} />
                {m.ime}
                <span className={`exp__badge ${m.placljiv ? 'exp__badge--paid' : 'exp__badge--free'}`}>
                  {m.placljiv ? 'plačljiv' : 'brezplačen'}
                </span>
              </label>
            ))}
          </div>
          {napake.modelIds && <p className="exp__error">{napake.modelIds}</p>}
        </section>

        {napake.kljuc && <p className="exp__error">{napake.kljuc}</p>}
        {napake.splosno && <p className="exp__error">{napake.splosno}</p>}

        <button type="submit" className="exp__submit" disabled={!lahkoOddam}>
          {busy ? 'Shranjujem …' : 'Ustvari eksperiment'}
        </button>
      </form>
    </main>
  );
}
