# 02 – Funkcionalnosti in obseg

## Uporabniške vloge

| Vloga | Kaj lahko počne | Česa ne more |
|---|---|---|
| **Obiskovalec** (neprijavljen) | Pregleduje seznam instrumentov in grafe zgodovinskih cen. | Ne more zaganjati eksperimentov ali videti rezultatov drugih. |
| **Raziskovalec** (prijavljen z Google računom) | Shrani svoj OpenRouter API ključ, ustvarja in zaganja eksperimente, pregleduje odločitve, utemeljitve in uspešnost modelov. | Ne more urejati baze instrumentov ali spreminjati rezultatov izvedenih eksperimentov. |
| **Administrator** | Uvaža cenovne serije in upravlja seznam razpoložljivih modelov. | Ne more spreminjati rezultatov izvedenih eksperimentov. |

---

## Funkcionalnosti

| Oznaka | Funkcionalnost | Obseg | Stanje |
|---|---|---|---|
| F1 | Pregled instrumentov in grafov cen | MVP | ✅ implementirano |
| F2 | Uvoz cenovnih serij | MVP (poenostavljeno s skriptami) | ✅ implementirano |
| F3 | Prijava z Google računom | MVP | načrtovano |
| F4 | Upravljanje lastnega OpenRouter API ključa | MVP | načrtovano |
| F5 | Konfiguracija eksperimenta | MVP | načrtovano |
| F6 | Zaprt in anonimiziran posnetek podatkov | MVP | načrtovano |
| F7 | Odločitev AI agenta in utemeljitev | MVP | načrtovano |
| F8 | Vrednotenje uspešnosti odločitev | MVP | načrtovano |
| F9 | Rezultati in primerjava modelov | MVP | načrtovano |
| F10 | Primerjava z benchmark strategijami | razširitev | – |
| F11 | Javni katalog eksperimentov | razširitev | – |
| F12 | Upravljanje modelov in odobritev objave | razširitev | – |
| F13 | Večkratni zagoni (Monte Carlo) | razširitev | – |
| F14 | Izvoz rezultatov (JSON, CSV) | razširitev | – |
| F15 | Uvoz cenovnih serij prek administratorskega API-ja | razširitev | – |

### MVP

1. **F1 – Pregled instrumentov in grafov cen**
   Seznam ~220 delnic in indeksnih skladov z iskanjem ter graf zgodovine cen z izbiro obdobja (1 leto, 5 let, vse).
2. **F2 – Uvoz cenovnih serij**
   Python skripte prenesejo ~15 let dnevnih cen (Yahoo Finance), jih očistijo in idempotentno uvozijo v Supabase. Vsak instrument dobi naključno anonimno oznako (npr. S042).
3. **F3 – Prijava z Google računom**
   Prijava prek Supabase Auth; Spring Boot preverja JWT žeton.
4. **F4 – Upravljanje lastnega OpenRouter API ključa**
   Raziskovalec vnese svoj OpenRouter ključ, ki se shrani šifrirano. Klici modelov gredo na njegov račun.
5. **F5 – Konfiguracija eksperimenta**
   Izbira instrumentov (ročno ali naključno), odločitvenega datuma, obdobja vrednotenja, kapitala in enega ali več modelov.
6. **F6 – Zaprt in anonimiziran posnetek podatkov**
   Model dobi samo cene do odločitvenega datuma in anonimne oznake, brez imen in tickerjev (*data leakage protection*).
7. **F7 – Odločitev AI agenta in utemeljitev**
   Model za vsak instrument odloči „kupi“ ali „ne kupi“ in odločitev utemelji. Backend odgovor preveri in shrani.
8. **F8 – Vrednotenje uspešnosti odločitev**
   Sistem iz cen po odločitvenem datumu izračuna, koliko bi portfelj izbranih instrumentov zaslužil, in delež pravilnih odločitev.
9. **F9 – Rezultati in primerjava modelov**
   Prikaz odločitev, utemeljitev in uspešnosti posameznega eksperimenta ter lestvica modelov čez vse eksperimente.

### Razširitve

10. **F10 – Benchmark strategije:** primerjava z S&P 500 in enakomerno porazdelitvijo v istem obdobju.
11. **F11 – Javni katalog:** pregled odobrenih eksperimentov za neprijavljene obiskovalce.
12. **F12 – Upravljanje modelov in odobritev objave:** administrator omogoča modele in odobri eksperimente za katalog.
13. **F13 – Monte Carlo:** večkratna izvedba istega eksperimenta za analizo nedeterminizma modelov.
14. **F14 – Izvoz rezultatov:** izvoz parametrov, odločitev, utemeljitev in metrik v JSON ali CSV.
15. **F15 – Uvoz prek API-ja:** zaščiten administratorski endpoint za uvoz CSV namesto skript.

---

## Out of Scope (izven obsega)

- **Lastna registracija z geslom:** prijava je mogoča samo z Google računom.
- **Plačevanje stroškov LLM klicev:** vsak raziskovalec uporablja svoj OpenRouter ključ in sam krije stroške.
- **Trgovanje z realnim denarjem ali povezava z borznimi posredniki:** aplikacija ne izvaja pravih transakcij.
- **Podatki v realnem času:** baza vsebuje samo zgodovinske dnevne cene.
- **Brskanje AI agentov po spletu:** modeli med odločanjem nimajo dostopa do interneta (preprečevanje *data leakage*).
- **Finančno svetovanje:** rešitev ne daje investicijskih priporočil za sedanjost ali prihodnost.
