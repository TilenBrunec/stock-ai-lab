# StockLab

Raziskovalna spletna aplikacija, v kateri LLM agenti ob izbranem datumu v preteklosti odločijo, v katere delnice in indeksne sklade bi vlagali, nato pa primerjamo, kateri agent bi se do danes odrezal najbolje.

## Kako deluje

1. Aplikacija hrani približno 15 let dnevnih vrednosti za okoli 200 znanih delnic in indeksnih skladov ter jih prikaže na grafih z možnostjo primerjave.
2. Raziskovalec ustvari eksperiment: izbere datum odločitve, začetni kapital, nabor delnic in enega ali več LLM agentov.
3. Agent dobi samo podatke do izbranega datuma in anonimne ID-je delnic (da ne prepozna podjetij in ne uporabi znanja o prihodnosti).
4. Sistem izračuna donos predlaganega portfelja do danes in agente primerja med seboj ter z referenčnim portfeljem.

Vloge: obiskovalec (pregled podatkov), raziskovalec (eksperimenti), administrator (agenti, uvoz podatkov).

## Uporabljene tehnologije

| Del | Tehnologija |
|---|---|
| Backend | Java, Spring Boot |
| Frontend | React, navaden CSS |
| Baza | Supabase (PostgreSQL) |
| Prijava | Supabase Auth (Google), JWT preverja Spring Boot |
| LLM | OpenRouter |
| Repozitorij | Monorepo na GitHubu (`backend/`, `frontend/`, `docs/`) |

Razlogi za posamezne izbire so zapisani v [arhitekturnih odločitvah](docs/adr/README.md).

## Dokumentacija

| Dokument | Vsebina |
|---|---|
| [01 – Zasnova rešitve](docs/01-zasnova-resitve.md) | Ideja, cilji in uporabniki |
| [02 – Funkcionalnosti in obseg](docs/02-funkcionalnosti-in-obseg.md) | Seznam funkcionalnosti in obseg MVP |
| [03 – Podatkovni model](docs/03-podatkovni-model.md) | Entitete in povezave |
| [04 – Arhitektura in tehnološki sklad](docs/04-arhitektura-in-tehnoloski-sklad.md) | Zgradba sistema in tehnologije |
| [05 – Uporabniške zgodbe](docs/05-uporabniske-zgodbe.md) | 15 uporabniških zgodb z acceptance criteria |
| [05 – Wireframes](docs/05-wireframes.md) | Osnutki ključnih zaslonov |
| [Arhitekturne odločitve (ADR)](docs/adr/README.md) | Zakaj smo izbrali posamezne rešitve |

## Struktura repozitorija

```
stock-ai-lab/
├── backend/     Spring Boot aplikacija
├── frontend/    React aplikacija
└── docs/        dokumentacija, wireframes in ADR
```

## Stanje projekta

Projekt je v fazi načrtovanja in postavljanja osnove (backend scaffold je pripravljen, uvoz podatkov in frontend še ne).