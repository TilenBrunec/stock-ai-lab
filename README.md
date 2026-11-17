# StockLab
 
Raziskovalna spletna aplikacija, v kateri LLM agenti ob izbranem datumu v preteklosti odločijo, v katere delnice in indeksne sklade bi vlagali, nato pa sistem z realnimi kasnejšimi cenami izračuna, kateri agent bi se odrezal najbolje.
 
## Kako deluje
 
1. Aplikacija hrani približno 15 let dnevnih cen za okoli 220 znanih delnic in indeksnih skladov ter jih prikaže na grafih.
2. Raziskovalec se prijavi z Google računom in v nastavitvah shrani svoj OpenRouter API ključ.
3. Ustvari eksperiment: izbere instrumente (ročno ali naključno), odločitveni datum, koliko let zgodovine vidi model, konec vrednotenja, kapital in enega ali več AI modelov.
4. Vsak model dobi samo tedenske cene do odločitvenega datuma, z anonimnimi oznakami (npr. `S042`), brez imen, tickerjev in koledarskih datumov, da ne more uporabiti znanja o prihodnosti.
5. Model za vsak instrument odloči **KUPI** ali **NE_KUPI** in odločitev utemelji.
6. Sistem kapital enakomerno razdeli med izbrane instrumente in iz cen po odločitvenem datumu izračuna končno vrednost, zaslužek, donos, CAGR in delež pravilnih odločitev.
Vloge: obiskovalec (pregled podatkov), raziskovalec (eksperimenti), administrator (uvoz podatkov, modeli).
 
## Uporabljene tehnologije
 
| Del | Tehnologija |
|---|---|
| Backend | Java 21, Spring Boot 4, Spring Security (OAuth2 Resource Server), Flyway |
| Frontend | React (TypeScript), Vite, navaden CSS, ECharts |
| Baza | Supabase (PostgreSQL) |
| Uvoz podatkov | Python (yfinance, pandas, psycopg) |
| Prijava | Supabase Auth (Google), JWT preverja Spring Boot |
| AI modeli | OpenRouter (ključ vsakega uporabnika, v bazi šifriran z AES-GCM) |
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
 
## Stanje projekta
 
- [x] Uvoz podatkov (US-12, poenostavljeno s skriptami)
- [x] Seznam instrumentov in graf cene (US-16)
- [x] Prijava z Google (US-01)
- [x] Shranjevanje OpenRouter ključa (US-02)
- [x] Konfiguracija eksperimenta in izbira modelov (US-03, US-04)
- [x] Zaprt posnetek, odločitev AI agenta in vrednotenje (US-05, US-06, US-07)
- [ ] Prikaz odločitev z utemeljitvami in graf rasti portfeljev (US-08)
- [ ] Zgodovina eksperimentov in primerjava modelov (US-09)
## Struktura repozitorija
 
```
stock-ai-lab/
├── backend/                         Spring Boot aplikacija
│   └── src/main/
│       ├── java/.../stocklab/       controller, service, model, dto, repository, config
│       └── resources/
│           ├── application.properties          skupne nastavitve (v gitu)
│           ├── application-local.properties    baza in ključ za šifriranje (NI v gitu)
│           └── db/migration/                   Flyway migracije (V1__, V2__ ... in R__llm_modeli.sql)
├── frontend/                        React aplikacija
│   └── .env.local                   naslov API-ja in Supabase ključ (NI v gitu)
├── data/
│   ├── tickers.csv                  seznam instrumentov (v gitu)
│   ├── raw/                         surove cene iz fetch_prices.py (NI v gitu)
│   └── clean/                       očiščene cene in poročilo (NI v gitu)
├── scripts/                         Python skripte za uvoz podatkov
├── docs/                            dokumentacija, wireframes in ADR
├── .env.example                     predloga za .env (v gitu)
└── .env                             DATABASE_URL za Python skripte (NI v gitu)
```
 
---
 
# Navodila za namestitev in zagon
 
Navodila za postavitev projekta na novem računalniku (Windows). Ukaze iz sivih blokov lahko kopiraš in prilepiš v terminal.
 
V primerih je projekt v mapi `C:\RIRS`. Pri sebi to zamenjaj s potjo do svoje mape.
 
> **Imaš projekt že postavljen na drugem računalniku z isto bazo?** Preskoči korake 3, 6 in 7 (baza, uvoz in Google sta že nastavljena). Konfiguracijske datoteke iz koraka 4 prekopiraj z drugega računalnika in pazi, da je `app.encryption-key` **enak**, sicer shranjenih OpenRouter ključev ni mogoče dešifrirati.
 
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
 
### Ključa za frontend
 
V Supabase pojdi na **Project Settings → API Keys** in si zapiši:
 
| Podatek | Primer |
|---|---|
| **Project URL** | `https://abcdefghijklmnop.supabase.co` |
| **Publishable key** (ali legacy *anon* key) | `sb_publishable_...` |
 
> Oba sta javna in namenjena frontendu. **Nikoli** ne uporabi `service_role` ali *secret* ključa v frontendu.
 
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
 
Desni klik na mapo `frontend` → **New File** → `.env.local`. Vsebina (podatki iz koraka 3, *Ključa za frontend*):
 
```env
VITE_API_URL=http://localhost:8080
VITE_SUPABASE_URL=https://abcdefghijklmnop.supabase.co
VITE_SUPABASE_ANON_KEY=sb_publishable_...
```
 
> Deluje tudi ime `frontend/.env`. Po vsaki spremembi te datoteke ponovno zaženi `npm run dev`.
 
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
5. **Ustvari ključ za šifriranje** OpenRouter ključev. V PowerShell (katerakoli mapa) zaženi:
```powershell
$b = New-Object byte[] 32; [Security.Cryptography.RNGCryptoServiceProvider]::Create().GetBytes($b); [Convert]::ToBase64String($b)
```
 
   Izpiše niz, npr. `k3J9aBc...=`. Shrani ga tudi v upravitelj gesel.
 
6. V `application-local.properties` prilepi in vstavi svoje podatke:
```properties
spring.datasource.url=jdbc:postgresql://HOST:5432/postgres?sslmode=require
spring.datasource.username=UPORABNIK
spring.datasource.password=GESLO
 
app.encryption-key=KLJUC_IZ_KORAKA_5
```
 
Primer:
 
```properties
spring.datasource.url=jdbc:postgresql://aws-1-eu-central-1.pooler.supabase.com:5432/postgres?sslmode=require
spring.datasource.username=postgres.abcdefghijklmnop
spring.datasource.password=MojeGeslo123
 
app.encryption-key=k3J9aBcDeFgHiJkLmNoPqRsTuVwXyZ0123456789abc=
```
 
> Pri backendu je uporabnik ločen od URL-ja, na začetku pa je `jdbc:`. Pri Python `.env` je obratno, uporabnik in geslo sta v URL-ju.
 
> **`app.encryption-key` ne menjaj in ga ne izgubi.** Z njim so v bazi šifrirani OpenRouter ključi uporabnikov. Z drugim ključem jih ni mogoče več prebrati in jih morajo uporabniki vnesti znova. Na vseh računalnikih, ki uporabljajo isto bazo, mora biti enak. Brez njega se backend ne zažene.
 
---
 
## 5. Zagon backenda (prvič)
 
### Preveri Java SDK v IntelliJ
 
**File → Project Structure → Project → SDK**: izberi Javo **21 ali novejšo**. Če je ni na seznamu, izberi **Add SDK → Download JDK**.
 
### Zaženi
 
V IntelliJ odpri terminal (**View → Tool Windows → Terminal**). Biti moraš v mapi `backend`:
 
```powershell
.\mvnw.cmd spring-boot:run "-Dspring-boot.run.profiles=local"
```
 
> Če backend zaganjaš z gumbom **Run** v IntelliJ, v **Run → Edit Configurations** pri `StocklabApplication` nastavi **Active profiles: `local`**.
 
Ob prvem zagonu Maven prenese vse knjižnice, kar traja nekaj minut. Flyway nato v bazi ustvari tabele in seznam AI modelov. V logu poišči:
 
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
 
### Testi
 
```powershell
.\mvnw.cmd test
```
 
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
Uvozi podatke v Supabase (tabeli `stock` in `stock_price`) in dodeli anonimne oznake (S001 …) v naključnem vrstnem redu.
 
> Vrstni red je pomemben, ker vsaka skripta potrebuje rezultat prejšnje. Uvoz lahko poženeš večkrat brez dvojnikov.
 
V Supabase **Table Editor** preveri, da imata tabeli `stock` in `stock_price` podatke.
 
> **Znana omejitev:** Yahoo Finance nima zgodovine delistanih podjetij, zato tickerji iz `data/raw/missing_tickers.txt` niso uvoženi (delni survivorship bias).
 
---
 
## 7. Prijava z Google (Supabase Auth)
 
Enkratna nastavitev (skrivnosti niso v repozitoriju):
 
1. V Supabase odpri **Authentication → Sign In / Providers → Google** in kopiraj **Callback URL** (`https://<ref>.supabase.co/auth/v1/callback`).
2. V [Google Cloud Console](https://console.cloud.google.com) ustvari nov projekt in nastavi **OAuth consent screen**:
   - Audience **External**
   - pod **Test users** dodaj svoj Gmail (dokler je aplikacija v načinu *Testing*, se lahko prijavijo samo testni uporabniki)
3. **Clients → Create client → Web application**:
   - Authorized JavaScript origins: `http://localhost:5173`
   - Authorized redirect URIs: Callback URL iz koraka 1
4. V Supabase pri Google providerju vklopi **Enable**, vpiši **Client ID** in **Client secret** in shrani.
5. Supabase → **Authentication → URL Configuration**:
   - Site URL: `http://localhost:5173`
   - Redirect URLs: `http://localhost:5173/**`
6. Supabase → **Project Settings → JWT Keys**: projekt mora uporabljati asimetrične ključe. Preveri v brskalniku, da `https://<ref>.supabase.co/auth/v1/.well-known/jwks.json` ni prazen in ima `"alg":"ES256"`.
---
 
## 8. Frontend (prvič)
 
V terminalu VS Code:
 
```powershell
cd frontend
npm install
```
 
---
 
## 9. Zagon celotne aplikacije
 
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
 
## 10. Prvi eksperiment
 
1. **OpenRouter ključ:** ustvari račun na https://openrouter.ai, nato na https://openrouter.ai/keys ustvari ključ.
2. V aplikaciji klikni **Prijava z Google**, nato **Nastavitve**, prilepi ključ in klikni **Shrani**. Prikazati se mora `••••xxxx`.
3. Klikni **Nov eksperiment**:
   - izberi nekaj instrumentov (iskanje ali gumb **Naključno izberi**)
   - nastavi odločitveni datum, leta zgodovine, konec vrednotenja in kapital
   - izberi vsaj en model in klikni **Ustvari eksperiment**
4. Na strani eksperimenta klikni **Zaženi eksperiment**. Modeli se kličejo eden za drugim (običajno 10–60 s na model), stran pa se osvežuje sama.
5. Ko je status **končano**, tabela pokaže končno vrednost, zaslužek, donos, CAGR in delež pravilnih odločitev za vsak model.
### Kaj pomenijo rezultati
 
| Stolpec | Pomen |
|---|---|
| Končna vrednost | koliko denarja bi imel na koncu; kapital se enakomerno razdeli med instrumente z odločitvijo KUPI |
| Zaslužek | končna vrednost − kapital |
| Donos | zaslužek v % za celotno obdobje |
| CAGR | povprečna letna rast (primerljiva med eksperimenti z različno dolgimi obdobji) |
| Pravilnih | delež pravilnih odločitev: KUPI, če je instrument zrasel, NE_KUPI, če je padel |
 
### AI modeli
 
Seznam modelov je v `backend/src/main/resources/db/migration/R__llm_modeli.sql`. To je *repeatable* migracija, ki se ponovno izvede ob vsaki spremembi datoteke.
 
- Za nov model dodaj vrstico s pravim ID-jem z https://openrouter.ai/models in ponovno zaženi backend.
- Model izklopiš z `aktiven = FALSE`.
- **Brezplačni modeli** (`:free`) imajo omejitve (približno 20 zahtev na minuto in 50 na dan, dokler na OpenRouterju ni kupljenih 10 $ kreditov) in so pogosto preobremenjeni (*rate limit*). Za zanesljivo delovanje na OpenRouter naloži nekaj kreditov in vklopi poceni plačljiv model.
---
 
## Kontrolni seznam
 
- [ ] vsi ukazi v koraku 1 izpišejo verzijo
- [ ] `.env`, `frontend/.env.local` in `application-local.properties` (z `app.encryption-key`) so ustvarjeni in izpolnjeni
- [ ] http://localhost:8080/api/actuator/health vrne `UP`
- [ ] tabele `stock` in `stock_price` v Supabase imajo podatke
- [ ] http://localhost:5173 prikaže seznam delnic
- [ ] prijava z Google deluje, v glavi je ime in oznaka *raziskovalec*
- [ ] OpenRouter ključ je shranjen in eksperiment se zaključi s statusom *končano*
## Pogoste težave
 
| Težava | Rešitev |
|---|---|
| `java`, `node` ali `python` *is not recognized* | program ni v PATH; ponovno namesti (pri Pythonu obkljukaj *Add to PATH*) in ponovno odpri VS Code |
| backend: *password authentication failed* | napačen uporabnik ali geslo v `application-local.properties`; uporabnik mora biti `postgres.<ref>`, ne samo `postgres` |
| backend: *connection timed out* ali *UnknownHost* | uporabljaš *Direct connection*; v Supabase izberi **Session pooler** |
| backend: *Could not resolve placeholder 'DB_URL'* | profil `local` ni vklopljen ali manjka `application-local.properties` |
| backend: *Manjka app.encryption-key* | v `application-local.properties` dodaj `app.encryption-key` (korak 4C) |
| backend: *checksum mismatch* | spremenjena je že izvedena migracija; spremembe sheme vedno v novo `V<n>__` datoteko |
| skripta: *password authentication failed* | posebni znaki v geslu niso kodirani (glej korak 4A) |
| *Port 8080 already in use* | backend že teče v drugem terminalu; ustavi ga s `Ctrl + C` |
| frontend: *Manjka VITE_SUPABASE_URL ...* | manjka ali je nepopoln `frontend/.env.local`; po popravku ponovno zaženi `npm run dev` |
| frontend se odpre, a nima podatkov | backend ne teče ali pa `VITE_API_URL` v `frontend/.env.local` ni pravilen |
| Google: *redirect_uri_mismatch* | redirect URI v Google Cloud ni točno Supabase Callback URL |
| Google: *access blocked* | tvoj Gmail ni med **Test users** v Google Cloud |
| Nastavitve: *Dešifriranje ni uspelo* | `app.encryption-key` ni enak kot ob shranjevanju ključa; vnesi OpenRouter ključ znova |
| model: *Omejitev zahtevkov (rate limit)* | brezplačni model je preobremenjen ali je porabljena dnevna omejitev; poskusi kasneje, drug model ali plačljiv model |
| eksperiment ostane *v teku* | backend je bil med izvajanjem ustavljen; ustvari nov eksperiment (zagon teče v pomnilniku backenda) |
 
---
 
## Razvoj
 
### Veje
 
```
main          ← samo delujoča koda, zaščitena (samo prek PR)
 └─ development   ← integracija (privzeta veja), sem se mergajo feature veje
     ├─ feat/<issue>-<opis>      nova funkcionalnost   (feat/6-import-db)
     ├─ feat/us-<nn>-<opis>      sklop issuejev ene zgodbe (feat/us-02-openrouter-key)
     ├─ fix/<issue>-<opis>       popravek
     ├─ docs/<issue>-<opis>      dokumentacija         (docs/8-import-readme)
     └─ test/<issue>-<opis>      testi
```
 
Ena veja je praviloma en issue. Manjše issueje iste uporabniške zgodbe lahko združiš v eno vejo, pri čemer ima vsak commit svojo številko issueja.
 
### Potek dela
 
```bash
git checkout development
git pull
git checkout -b feat/<issue>-<opis>
# ... delo, sproti majhni commiti ...
git push -u origin feat/<issue>-<opis>
```
 
1. Na Project boardu prestavi issue v **Doing**.
2. Odpri PR **v `development`** z opisom sprememb in `Closes #<issue>` (pri sklopu za vsak issue svojo vrstico). Preveri, da je **base `development`**.
3. Preveri pogoje sprejemljivosti iz uporabniške zgodbe, nato merge (merge commit) in izbriši vejo.
4. Issue prestavi v **Done**, če se ni zaprl sam.
Ko je sklop funkcionalnosti končan in deluje, gre PR **`development → main`**. Veje `development` se ne briše. Po mergu uskladi `development` z `main`:
 
```bash
git checkout development
git pull
git merge --ff-only origin/main
git push
```
 
### Commit sporočila
 
[Conventional Commits](https://www.conventionalcommits.org/) s številko issueja:
 
```
feat(data): idempotenten uvoz instrumentov in cen v Supabase (#6)
fix(api): pravilen format datuma v /prices (#10)
docs: navodila za lokalni zagon (#8)
```
 
Pred commitom vedno `git status`, da v commit ne gre `.env`, `.env.local`, `application-local.properties`, `.venv/` ali `data/raw/`.
 
### Kam kaj dodati
 
| Kaj dodajaš | Kam |
|---|---|
| Nova tabela ali sprememba sheme | nova datoteka `backend/src/main/resources/db/migration/V<n>__opis.sql` |
| Nov ali spremenjen AI model | vrstica v `backend/src/main/resources/db/migration/R__llm_modeli.sql` |
| JPA entiteta | `backend/.../model/` |
| Repository | `backend/.../repository/` |
| Poslovna logika | `backend/.../service/` |
| REST endpoint | `backend/.../controller/` (in pravilo dostopa v `config/SecurityConfig.java`) |
| Objekt za API odgovor/zahtevek | `backend/.../dto/` |
| CORS, Security, konfiguracija | `backend/.../config/` |
| Nova okoljska spremenljivka | v `.env.example` (brez vrednosti) in v `.env` |
| Nov instrument | vrstica v `data/tickers.csv`, nato ponoven uvoz |
| Arhitekturna odločitev | nov `docs/adr/00NN-opis.md` po predlogi `adr-template.md` |
 
**Migracij, ki so že izvedene, se ne spreminja.** Vsaka sprememba sheme gre v novo `V<n>__` datoteko, sicer Flyway ob zagonu javi *checksum mismatch*. Shema se ne spreminja ročno v Supabase SQL Editorju; tam samo preverjaš podatke.
 
**Nov endpoint mora imeti pravilo v `SecurityConfig`**, ker so vsi neopredeljeni endpointi zavrnjeni (`denyAll`).
 
### Znane omejitve
 
- Zagon eksperimenta teče v pomnilniku backenda; če se backend ustavi med izvajanjem, eksperiment ostane *v teku*.
- Yahoo Finance nima zgodovine delistanih podjetij (delni survivorship bias).
- Brezplačni OpenRouter modeli so pogosto omejeni (*rate limit*) in se menjajo.
 
