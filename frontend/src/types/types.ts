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
}