## Ime rešitve
 
**StockLab**
 
## Namen in problem
namen: Preverjanje kakovosti finančnih nasvetov jezikovnih modelov (LLM). aplikacija ima raziskovalni namen.
problem: Premalo znanja o investiranju posameznikov, preveliko/premajhno zaupanje AI-ju.

## Ciljni uporabniki
- Študent / raziskovalec
- Radoveden vlagatelj

## Kratek opis delovanja

1. **Nastavitev:** Uporabnik izbere odločitveni datum, obdobje vrednotenja, kapital, nabor instrumentov in AI agente.
2. **Zaprt posnetek:** Strežnik pripravi podatke striktno do odločitvenega datuma in anonimizira instrumente. Agent nima dostopa do spleta (*data leakage protection*).
3. **Odločitev in utemeljitev:** Agent vrne:
   - **Portfelj:** Uteži po anonimnih instrumentih (vsota = 100 %).
   - **Utemeljitev:** Razlago, zakaj je izbral določene delnice in zakaj je ostale zavrnil.
4. **Vrednotenje:** Strežnik ovrednoti portfelj z dejanskimi kasnejšimi cenami (CAGR, nihajnost, padec).
5. **Primerjava:** Sistem izriše grafe uspešnosti ter agente primerja med seboj.
6. **Objava:** Uporabniki analizirajo rezultate in utemeljitve; javna objava sledi po odobritvi administratorja.

## Omejitve in znana tveganja

- **Pristranskost preživelih (*survivorship bias*):** Baze brezplačnih podatkov večinoma ne vključujejo propadlih podjetij. Za ublažitev nabor vsebuje tudi znane neuspešne primere.
- **Dosegljivost anonimizacije:** Čeprav so imena skrita, lahko napredni modeli delno prepoznajo instrumente iz vzorcev cen, zato se uporabljajo povzetki in normalizacija.
- **Nedeterminizem LLM-jev:** Isti agent lahko ob ponovitvi poda drugačne odločitve ali utemeljitve. Sistem zato omogoča večkratne ponovitve eksperimentov.
- **Omejitev odgovornosti:** Rešitev je namenjena izključno raziskovalno-izobraževalnim namenom in ne predstavlja finančnega svetovanja.