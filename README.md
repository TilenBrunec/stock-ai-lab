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
| Backend | Java, Spring Boot, Flyway |
| Frontend | React (TypeScript), navaden CSS |
| Baza | Supabase (PostgreSQL) |
| Uvoz podatkov | Python (yfinance, pandas, psycopg) |
| Prijava | Supabase Auth (Google), JWT preverja Spring Boot |
| LLM | OpenRouter |
| Repozitorij | Monorepo na GitHubu |
 
Razlogi za posamezne izbire so zapisani v [arhitekturnih odločitvah](docs/adr/README.md).
 
## Dokumentacija
 
| Dokument | Vsebina |
|---|---|
| [01 – Zasnova rešitve](docs/01-zasnova-resitve.md) | Ideja, cilji in uporabniki |
| [02 – Funkcionalnosti in obseg](docs/02-funkcionalnosti-in-obseg.md) | Seznam funkcionalnosti in obseg MVP |
| [03 – Podatkovni model](docs/03-podatkovni-model.md) | Entitete in povezave |
| [04 – Arhitektura in tehnološki sklad](docs/04-arhitektura-in-tehnoloski-sklad.md) | Zgradba sistema in tehnologije |
| [05 – Uporabniške zgodbe](docs/05-uporabniske-zgodbe.md) | Uporabniške zgodbe s pogoji sprejemljivosti |
| [06 – Wireframes](docs/06-wireframes.md) | Osnutki ključnih zaslonov |
| [Arhitekturne odločitve (ADR)](docs/adr/README.md) | Zakaj smo izbrali posamezne rešitve |
 
## Struktura repozitorija
 
```
stock-ai-lab/
├── backend/                         Spring Boot aplikacija
│   └── src/main/
│       ├── java/.../stocklab/       controller, service, model, dto, repository, config
│       └── resources/
│           ├── application.properties          skupne nastavitve (v gitu)
│           ├── application-local.properties    lokalna povezava na bazo (NI v gitu)
│           └── db/migration/                   Flyway migracije (V1__init.sql, V2__...)
├── frontend/                        React aplikacija
├── data/
│   ├── tickers.csv                  seznam instrumentov (v gitu)
│   ├── raw/                         surove cene iz fetch_prices.py (NI v gitu)
│   └── clean/                       očiščene cene in poročilo (NI v gitu)
├── scripts/                         Python skripte za uvoz podatkov
├── docs/                            dokumentacija, wireframes in ADR
├── .env.example                     predloga za .env (v gitu)
└── .env                             DATABASE_URL za Python skripte (NI v gitu)
```
 
## Konfiguracija (skrivnosti in povezave)
 
Gesla in connection stringi **nikoli** ne gredo v git. Vsak del ima svojo datoteko:
 
| Datoteka | Kdo jo bere | Kaj vsebuje | V gitu |
|---|---|---|---|
| `backend/src/main/resources/application.properties` | Spring Boot | skupne nastavitve, sklic na `${DB_URL}`, `${DB_USER}`, `${DB_PASSWORD}` | da |
| `backend/src/main/resources/application-local.properties` | Spring Boot (profil `local`) | povezava na Supabase (JDBC URL, uporabnik, geslo) | **ne** |
| `.env` (root) | Python skripte v `scripts/` | `DATABASE_URL=postgresql://...` | **ne** |
| `.env.example` (root) | ljudje | predloga za `.env` brez gesla | da |
| `frontend/.env.local` | Vite (React) | `VITE_API_URL=http://localhost:8080` | **ne** |
 
Connection string dobiš v Supabase: **Connect → Session pooler** (port 5432).
 
- **Backend (Java)** rabi JDBC obliko, uporabnik in geslo sta ločena:
  `jdbc:postgresql://aws-1-eu-central-1.pooler.supabase.com:5432/postgres?sslmode=require`
- **Python** rabi obliko brez `jdbc:`, uporabnik in geslo sta v URL-ju:
  `postgresql://postgres.<ref>:<geslo>@aws-1-eu-central-1.pooler.supabase.com:5432/postgres?sslmode=require`
  Posebne znake v geslu je treba kodirati (`#` → `%23`, `!` → `%21`, `*` → `%2A`, `@` → `%40`).
## Lokalni zagon
 
Predpogoji: Java 21+, Node.js 20+, Python 3.11+, Supabase projekt.
 
### 1. Backend
 
1. Ustvari `backend/src/main/resources/application-local.properties` s povezavo na Supabase.
2. Zaženi s profilom `local`:
```bash
   cd backend
   ./mvnw spring-boot:run -Dspring-boot.run.profiles=local      # Windows: .\mvnw.cmd ...
```
3. Ob zagonu Flyway sam ustvari tabele (`db/migration`). V logu poišči `Successfully applied ... migration`.
4. Preveri: <http://localhost:8080/api/actuator/health> mora vrniti `"status":"UP"`.
### 2. Uvoz podatkov
 
Shema mora že obstajati, zato najprej enkrat zaženi backend.
 
```bash
python -m venv .venv
.venv\Scripts\activate            # Windows  (Linux/macOS: source .venv/bin/activate)
pip install -r scripts/requirements.txt
 
cp .env.example .env              # vpiši DATABASE_URL
 
python scripts/fetch_prices.py    # → data/raw/prices.csv, data/raw/missing_tickers.txt
python scripts/clean_data.py      # → data/clean/prices.csv, data/clean/report.csv
python scripts/import_db.py       # → tabeli stock in stock_price + anonimne oznake
```
 
- Uvoz je idempotenten: ponoven zagon ne ustvari dvojnikov.
- Anonimne oznake (S001 …) se dodelijo v naključnem vrstnem redu, da AI iz oznake ne more uganiti tickerja, in se ob ponovnem uvozu ne spremenijo.
**Znana omejitev:** Yahoo Finance nima zgodovine delistanih podjetij, zato tickerji iz `missing_tickers.txt` niso uvoženi (delni survivorship bias).
 
Stanje po uvozu: <N> instrumentov, <M> cen, velikost baze <X> MB.
 
### 3. Frontend
 
```bash
cd frontend
npm install
npm run dev                       # http://localhost:5173
```
 
## Razvoj
 
### Veje
 
```
main          ← samo delujoča koda, zaščitena (samo prek PR)
 └─ development   ← integracija, sem se mergajo feature veje
     ├─ feat/<issue>-<opis>      nova funkcionalnost   (feat/6-import-db)
     ├─ fix/<issue>-<opis>       popravek
     ├─ docs/<issue>-<opis>      dokumentacija         (docs/8-import-readme)
     └─ test/<issue>-<opis>      testi
```
 
Ena veja = en issue. Ime veje je zapisano v vsakem issueju (vrstica **Veja:**).
 
### Potek dela za en issue
 
```bash
git checkout development
git pull
git checkout -b feat/<issue>-<opis>
# ... delo, sproti majhni commiti ...
git push -u origin feat/<issue>-<opis>
```
 
1. Na Project boardu prestavi issue v **Doing**.
2. Odpri PR **v `development`** z opisom sprememb in `Closes #<issue>`.
3. Preveri pogoje sprejemljivosti iz uporabniške zgodbe, nato merge (merge commit) in izbriši vejo.
4. Issue prestavi v **Done** (merge v `development` ga ne zapre samodejno).
Ko je sklop funkcionalnosti končan in deluje, gre PR **`development → main`** s seznamom vseh `Closes #...`. Veje `development` se ne briše.
 
### Commit sporočila
 
[Conventional Commits](https://www.conventionalcommits.org/) s številko issueja:
 
```
feat(data): idempotenten uvoz instrumentov in cen v Supabase (#6)
fix(api): pravilen format datuma v /prices (#10)
docs: navodila za lokalni zagon (#8)
```
 
Pred commitom vedno `git status`, da v commit ne gre `.env`, `.venv/` ali `data/raw/`.
 
### Kam kaj dodati
 
| Kaj dodajaš | Kam |
|---|---|
| Nova tabela ali sprememba sheme | nova datoteka `backend/src/main/resources/db/migration/V<n>__opis.sql` |
| JPA entiteta | `backend/.../model/` |
| Repository | `backend/.../repository/` |
| Poslovna logika | `backend/.../service/` |
| REST endpoint | `backend/.../controller/` |
| Objekt za API odgovor/zahtevek | `backend/.../dto/` |
| CORS, Security, konfiguracija | `backend/.../config/` |
| Nova okoljska spremenljivka | v `.env.example` (brez vrednosti) in v `.env` |
| Nov instrument | vrstica v `data/tickers.csv`, nato ponoven uvoz |
| Arhitekturna odločitev | nov `docs/adr/00NN-opis.md` po predlogi `adr-template.md` |
 
**Migracij, ki so že izvedene, se ne spreminja.** Vsaka sprememba sheme gre v novo `V<n>__` datoteko, sicer Flyway ob zagonu javi *checksum mismatch*. Shema se ne spreminja ročno v Supabase SQL Editorju; tam samo preverjaš podatke.