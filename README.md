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
 
# StockLab: navodila za namestitev in zagon

Navodila za postavitev projekta na novem računalniku (Windows). Ukaze iz sivih blokov lahko kopiraš in prilepiš v terminal.

V primerih je projekt v mapi `C:\RIRS`. Pri sebi to zamenjaj s potjo do svoje mape.

---

## 0. Potrebni programi

Najprej namesti oba urejevalnika:

| Program | Zakaj | Prenos |
|---|---|---|
| **IntelliJ IDEA** (Community ali Ultimate) | backend (Spring Boot) | https://www.jetbrains.com/idea/download/ |
| **VS Code** | frontend, skripte, terminal | https://code.visualstudio.com/ |

---

## 1. Preverjanje orodij

1. Na računalniku ustvari mapo, kjerkoli želiš (npr. `C:\RIRS`).
2. Odpri jo v VS Code (**File → Open Folder**).
3. Odpri terminal (**Terminal → New Terminal** ali `` Ctrl + ` ``) in zaženi:

```powershell
git --version
java -version
node -v
npm -v
python --version
```
![alt text](README-slike/image.png)  

Vsak ukaz mora izpisati verzijo.
Če katerega manjka ali je verzija prenizka, ga namesti:

| Orodje | Najmanj | Prenos | Opomba |
|---|---|---|---|
| Git | katerakoli | https://git-scm.com/downloads | privzete nastavitve so OK |
| Java (JDK) | **21** | https://adoptium.net/temurin/releases/?version=21 | izberi *Windows x64, JDK, .msi*; med namestitvijo vklopi *Set JAVA_HOME* |
| Node.js (vsebuje npm) | **20** | https://nodejs.org/ | izberi *LTS* verzijo |
| Python | **3.11** | https://www.python.org/downloads/ | na prvem zaslonu **obkljukaj "Add python.exe to PATH"** |

Po namestitvi **zapri in ponovno odpri VS Code**, da terminal vidi nove programe, in ukaze zaženi še enkrat.

---

## 2. Kloniranje projekta

V terminalu VS Code (v mapi, ki si jo ustvaril):

```powershell
git clone https://github.com/TilenBrunec/stock-ai-lab.git
cd stock-ai-lab
git checkout development
```

V VS Code zdaj odpri mapo `stock-ai-lab` (**File → Open Folder → stock-ai-lab**).

---

## 3. Supabase projekt (baza)

1. Pojdi na https://supabase.com in klikni **Start your project**. Prijaviš se lahko z GitHub računom ali emailom.
2. Če te vpraša, ustvari **organizacijo** (poljubno ime, plan **Free**).
3. Klikni **New project** in izpolni:
   - **Project name**: npr. `stocklab`
   - **Database Password**: klikni **Generate a password** in ga **takoj nekam shrani**, ker ga boš rabil kasneje in ga Supabase ne pokaže več.
     > Nasvet: geslo naj ima samo črke in številke. Posebni znaki (`#`, `!`, `@`, `*`) povzročajo težave v connection stringu.
   - **Region**: **Central EU (Frankfurt)**
4. Klikni **Create new project** in počakaj 1–2 minuti, da se projekt postavi.
5. Tabel **ne ustvarjaj ročno**, ker jih ob prvem zagonu backenda ustvari Flyway.

### Kje dobiš podatke za povezavo

![alt text](README-slike/image-1.png) 
![alt text](README-slike/image-2.png) 



1. V projektu klikni gumb **Connect** (zgoraj na sredini).
2. Izberi zavihek **Connection String**.
3. Nastavi **Method: Session pooler** (ne *Direct connection*, ker ta na večini omrežij ne deluje).
4. Prikaže se niz v tej obliki:

```
postgresql://postgres.abcdefghijklmnop:[YOUR-PASSWORD]@aws-1-eu-central-1.pooler.supabase.com:5432/postgres
```

Iz njega rabiš tri podatke:

| Podatek | Primer | Kje v nizu |
|---|---|---|
| **uporabnik** | `postgres.abcdefghijklmnop` | med `//` in `:` |
| **host** | `aws-1-eu-central-1.pooler.supabase.com` | med `@` in `:5432` |
| **geslo** | tisto, ki si ga shranil v koraku 3 | namesto `[YOUR-PASSWORD]` |

> Host se lahko pri tebi malo razlikuje (npr. `aws-0-...`). Vedno uporabi tistega, ki ti ga pokaže Supabase.

---

## 4. Konfiguracijske datoteke

Teh datotek ni v gitu, ker vsebujejo gesla, zato jih moraš ustvariti sam. Končna struktura:

```
stock-ai-lab/
├── .env                                              ← (A) za Python skripte
├── frontend/
│   └── .env.local                                    ← (B) za frontend
└── backend/
    └── src/
        └── main/
            └── resources/
                └── application-local.properties      ← (C) za backend
```

### (A) `stock-ai-lab/.env` za Python skripte

V VS Code desni klik na mapo `stock-ai-lab` → **New File** → `.env`. Vsebina:

```env
DATABASE_URL=postgresql://UPORABNIK:GESLO@HOST:5432/postgres?sslmode=require
```

Primer:

```env
DATABASE_URL=postgresql://postgres.abcdefghijklmnop:MojeGeslo123@aws-1-eu-central-1.pooler.supabase.com:5432/postgres?sslmode=require
```

> Če ima geslo posebne znake, jih zamenjaj: `#` → `%23`, `!` → `%21`, `*` → `%2A`, `@` → `%40`.

### (B) `stock-ai-lab/frontend/.env.local` za frontend

Desni klik na mapo `frontend` → **New File** → `.env.local`. Vsebina:

```env
VITE_API_URL=http://localhost:8080
```

### (C) `application-local.properties` za backend

![alt text](README-slike/image-3.png) 


1. Odpri **IntelliJ IDEA** → **Open** → izberi mapo `stock-ai-lab\backend` (samo `backend`, ne celega `stock-ai-lab`).
2. Če te vpraša *Trust project*, klikni **Trust Project**.
3. V drevesu projekta (levo) pojdi do:

```
backend\src\main\resources\
```

   Celotna pot je npr. `C:\RIRS\stock-ai-lab\backend\src\main\resources\`. V njej sta že `application.properties` in mapa `db`.

4. Desni klik na `resources` → **New → File** → ime `application-local.properties`.
5. Prilepi vanjo in vstavi svoje podatke iz koraka 3:

```properties
spring.datasource.url=jdbc:postgresql://HOST:5432/postgres?sslmode=require
spring.datasource.username=UPORABNIK
spring.datasource.password=GESLO
```

Primer:

```properties
spring.datasource.url=jdbc:postgresql://aws-1-eu-central-1.pooler.supabase.com:5432/postgres?sslmode=require
spring.datasource.username=postgres.abcdefghijklmnop
spring.datasource.password=MojeGeslo123
```

> Pri backendu je uporabnik ločen od URL-ja, na začetku pa je `jdbc:`. Pri Python `.env` je obratno, uporabnik in geslo sta v URL-ju.

---

## 5. Zagon backenda (prvič)

### Preveri Java SDK v IntelliJ

**File → Project Structure → Project → SDK**: izberi Javo **21 ali novejšo**. Če je ni na seznamu, izberi **Add SDK → Download JDK**.

### Zaženi

V IntelliJ odpri terminal (**View → Tool Windows → Terminal**). Biti moraš v mapi `backend`:

```powershell
.\mvnw.cmd spring-boot:run "-Dspring-boot.run.profiles=local"
```

Ob prvem zagonu Maven prenese vse knjižnice, kar traja nekaj minut. Flyway nato v bazi ustvari tabele. V logu poišči:

```
Successfully applied ... migration
```

### Če Maven nagaja

Simptomi: v `pom.xml` je verzija obarvana rdeče, importi v Javi so rdeči ali zagon ne najde knjižnic.

1. Odpri zavihek **Maven** (ikona **m** na desni strani) → klikni **Reload All Maven Projects** (ikona s krožnima puščicama).
2. Če še vedno ne gre, v terminalu v mapi `backend`:

```powershell
.\mvnw.cmd dependency:resolve
```

3. Nato backend ponovno zaženi:

```powershell
.\mvnw.cmd spring-boot:run "-Dspring-boot.run.profiles=local"
```

### Preveri, ali backend dela

V brskalniku odpri:

```
http://localhost:8080/api/actuator/health
```

Če vrne `{"status":"UP"}`, je vse OK. V Supabase pod **Table Editor** zdaj vidiš tabele.

Backend ustavi s **`Ctrl + C`** v terminalu.

---

## 6. Uvoz podatkov (Python skripte)

V **VS Code** odpri terminal v mapi `stock-ai-lab` (v rootu, ne v `backend` ali `frontend`).

### Virtualno okolje in knjižnice

```powershell
python -m venv .venv
.venv\Scripts\activate
pip install -r scripts/requirements.txt
```

Ko je okolje aktivno, je na začetku vrstice v terminalu napis `(.venv)`.

> Če PowerShell javi napako *"running scripts is disabled on this system"*, enkrat zaženi spodnji ukaz in ponovi `activate`:
> ```powershell
> Set-ExecutionPolicy -Scope CurrentUser RemoteSigned
> ```

### Zagon skript (po vrsti)

```powershell
python scripts/fetch_prices.py
```
Prenese cene z Yahoo Finance v `data/raw/prices.csv`. Traja nekaj minut.

```powershell
python scripts/clean_data.py
```
Očisti podatke v `data/clean/prices.csv` in ustvari poročilo `data/clean/report.csv`.

```powershell
python scripts/import_db.py
```
Uvozi podatke v Supabase (tabeli `stock` in `stock_price`) in dodeli anonimne oznake (S001 …).

> Vrstni red je pomemben, ker vsaka skripta potrebuje rezultat prejšnje. Uvoz lahko poženeš večkrat brez dvojnikov.

V Supabase **Table Editor** preveri, da imata tabeli `stock` in `stock_price` podatke.

---

## 7. Frontend (prvič)

V terminalu VS Code:

```powershell
cd frontend
npm install
```

---

## 8. Zagon celotne aplikacije

Potrebuješ **dva terminala hkrati**.

**Terminal 1: backend** (IntelliJ ali VS Code, mapa `stock-ai-lab\backend`):

```powershell
.\mvnw.cmd spring-boot:run "-Dspring-boot.run.profiles=local"
```

**Terminal 2: frontend** (VS Code, mapa `stock-ai-lab\frontend`):

```powershell
npm run dev
```

V brskalniku odpri:

```
http://localhost:5173
```

Aplikacija bi morala prikazati seznam delnic.

---

## Kontrolni seznam

- [ ] vsi ukazi v koraku 1 izpišejo verzijo
- [ ] `.env`, `frontend/.env.local` in `application-local.properties` so ustvarjeni in izpolnjeni
- [ ] http://localhost:8080/api/actuator/health vrne `UP`
- [ ] tabele `stock` in `stock_price` v Supabase imajo podatke
- [ ] http://localhost:5173 prikaže seznam delnic

## Pogoste težave

| Težava | Rešitev |
|---|---|
| `java`, `node` ali `python` *is not recognized* | program ni v PATH; ponovno namesti (pri Pythonu obkljukaj *Add to PATH*) in ponovno odpri VS Code |
| backend: *password authentication failed* | napačen uporabnik ali geslo v `application-local.properties`; uporabnik mora biti `postgres.<ref>`, ne samo `postgres` |
| backend: *connection timed out* ali *UnknownHost* | uporabljaš *Direct connection*; v Supabase izberi **Session pooler** |
| skripta: *password authentication failed* | posebni znaki v geslu niso kodirani (glej korak 4A) |
| *Port 8080 already in use* | backend že teče v drugem terminalu; ustavi ga s `Ctrl + C` |
| frontend se odpre, a nima podatkov | backend ne teče ali pa `VITE_API_URL` v `frontend/.env.local` ni pravilen |

 
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