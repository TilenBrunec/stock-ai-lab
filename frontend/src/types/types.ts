export type InstrumentType = 'DELNICA' | 'SKLAD';

export interface Stock {
  id: number;
  ticker: string;
  ime: string;
  tip: InstrumentType;
}

export interface Price {
  datum: string;   // "2024-01-02"
  cena: number;
}

export interface Me {
  id: string;
  email: string;
  ime: string | null;
  slikaUrl: string | null;
  vloga: 'RAZISKOVALEC' | 'ADMINISTRATOR';
  imaKljuc: boolean;
  kljucZadnji4: string | null;
}

export interface LlmModel {
  id: number;
  ime: string;
  placljiv: boolean;
}

export interface DateRange {
  zacetek: string;   // prvi datum cen v bazi
  konec: string;     // zadnji datum cen v bazi
}

export type Status = 'CAKA' | 'V_TEKU' | 'KONCANO' | 'NEUSPESNO';

export interface CreateExperimentRequest {
  instrumentIds: number[];
  odlocitveniDatum: string;
  letaZgodovine: number;
  konecVrednotenja: string;
  kapital: number;
  modelIds: number[];
}

export interface Zagon {
  id: number;
  modelId: number;
  model: string;
  placljiv: boolean;
  status: Status;
  napaka: string | null;
  // metrike so null, dokler zagon ni končan
  koncnaVrednost: number | null;
  zasluzek: number | null;
  donos: number | null;         // %
  cagr: number | null;          // %
  delezPravilnih: number | null; // %
}

export interface Experiment {
  id: number;
  odlocitveniDatum: string;
  letaZgodovine: number;
  konecVrednotenja: string;
  kapital: number;
  status: Status;
  ustvarjen: string;
  instrumenti: Stock[];
  zagoni: Zagon[];
}

export interface ExperimentSummary {
  id: number;
  odlocitveniDatum: string;
  konecVrednotenja: string;
  kapital: number;
  status: Status;
  steviloInstrumentov: number;
  modeli: string[];
  ustvarjen: string;
}
