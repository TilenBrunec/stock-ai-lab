# 02 – Funkcionalnosti in obseg

## Uporabniške vloge

| Uporabniška vloga | Kaj lahko počne (Pravice) | Česa ne more početi (Omejitve) |
|---|---|---|
| **Javni uporabnik / Raziskovalec** | Poganja nove eksperimente, pregleduje vse pretekle rezultate, bere utemeljitve AI agentov in analizira interaktivne grafe. | Ne more urejati baze instrumentov ali spreminjati sistemskih nastavitev. |
| **Administrator** (dostop preko okoljskih spremenljivk / zaščitenega API-ja) | Upravlja in posodablja bazo anonimiziranih finančnih instrumentov ter nastavitve AI agentov. | Ne more spreminjati že izvedenih zgodovinskih rezultatov eksperimentov. |

---

## Predvidene funkcionalnosti

1. **Konfiguracija in zagon eksperimenta (MVP)**  
   Nastavitev parametrov: izbira odločitvenega datuma, obdobja vrednotenja, kapitala, delnic in AI agentov.
2. **Generiranje zaprtega posnetka podatkov (MVP - Domensko)**  
   Izvoz finančnih kazalnikov striktno do odločitvenega datuma z avtomatsko anonimizacijo instrumentov (*data leakage protection*).
3. **Izvedba AI simulacije in utemeljitev (MVP - Domensko)**  
   Pošiljanje posnetka AI agentom, validacija odgovora (vsota uteži = 100 %) in shranjevanje tekstovne obrazložitve.
4. **Vrednotenje portfelja z realnimi cenami (MVP - Domensko)**  
   Izračun simulirane rasti portfelja, donosnosti (CAGR), nihajnosti in največjega padca na podlagi kasnejših cen.
5. **Primerjava z benchmark strategijami (MVP - Domensko)**  
   Izračun in prikaz uspešnosti referenčnih strategij (S&P 500, enakomerna porazdelitev) v istem obdobju.
6. **Interaktivni pregled rezultatov in grafov (MVP)**  
   Prikaz grafov rasti vrednosti portfeljev posameznih agentov skozi čas in lestvica uspešnosti.
7. **Pregled podrobnih utemeljitev agenta (MVP)**  
   Vpogled v tekstovno obrazložitev AI agenta, zakaj je posamezen instrument izbral ali zavrnil.
8. **Javni katalog izvedenih eksperimentov**  
   Skupni pregled vseh preteklih eksperimentov, dostopen vsem obiskovalcem.
9. **Upravljanje baze instrumentov**  
   Uvoz novih cenovnih serij in urejanje parametrov za anonimizacijo.
10. **Večkratni zagoni (Monte Carlo)**  
    Večkratna izvedba istega eksperimenta z istim LLM agentom za analizo nedeterminizma.
11. **Izvoz rezultatov in analitike**  
    Izvoz podatkov eksperimenta in utemeljitev v formatu JSON ali CSV za nadaljnjo raziskavo.

---

## Out of Scope (Izven obsega)

- **Avtentikacija in registracija uporabnikov:** Sistem nima uporabniških računov, prijave ali sej; vsi uporabniki imajo takojšen dostop do orodja.
- **Trgovanje z realnim denarjem ali povezava z borznimi posredniki:** Aplikacija ne omogoča izvajanja pravih finančnih transakcij.
- **Brskanje AI agentov po spletu (Real-time web search):** Agenti med odločanjem nimajo dostopa do interneta (*preprečevanje data leakage*).
- **Finančno svetovanje in napovedovanje prihodnosti:** Rešitev ne generira investicijskih priporočil za sedanjost ali prihodnost.