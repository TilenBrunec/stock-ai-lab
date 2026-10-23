# 05 – Uporabniške zgodbe
 
## Funkcionalnosti (iz dokumenta 02)
 
| Oznaka | Funkcionalnost |
|---|---|
| F1 | Konfiguracija in zagon eksperimenta (MVP) |
| F2 | Generiranje zaprtega posnetka podatkov (MVP) |
| F3 | Izvedba AI simulacije in utemeljitev (MVP) |
| F4 | Vrednotenje portfelja z realnimi cenami (MVP) |
| F5 | Primerjava z benchmark strategijami (MVP) |
| F6 | Interaktivni pregled rezultatov in grafov (MVP) |
| F7 | Pregled podrobnih utemeljitev agenta (MVP) |
| F8 | Javni katalog izvedenih eksperimentov |
| F9 | Upravljanje baze instrumentov |
| F10 | Večkratni zagoni (Monte Carlo) |
| F11 | Izvoz rezultatov in analitike |
| F12 | Uvoz cenovnih serij v bazo instrumentov | razširitev |
| F13 | Upravljanje agentov in odobritev objave | razširitev |
| F14 | Večkratni zagoni (Monte Carlo) | razširitev |
| F15 | Izvoz rezultatov (JSON, CSV) | razširitev |

 
Vloge: **Obiskovalec** (neprijavljen), **Raziskovalec** (prijavljen z Google računom), **Administrator**.
 
---
 
## US-01 – Prijava z Google računom
**Funkcionalnost:** F12 | **Vloga:** Obiskovalec
 
Kot obiskovalec se želim prijaviti z Google računom, da lahko zaganjam eksperimente brez ustvarjanja novega gesla.
 
**Acceptance criteria**
- Gumb „Prijava z Google“ je viden neprijavljenim obiskovalcem na vsaki strani.
- Po uspešni prijavi frontend prejme JWT žeton in prikaže ime ter sliko računa.
- Klic API-ja za zagon eksperimenta brez veljavnega žetona vrne 401.
- Po odjavi je žeton izbrisan in zagon eksperimenta ni mogoč brez nove prijave.
## US-02 – Nastavitev parametrov eksperimenta
**Funkcionalnost:** F1 | **Vloga:** Raziskovalec
 
Kot raziskovalec želim nastaviti odločitveni datum, obdobje vrednotenja, kapital in nabor instrumentov, da natančno določim, kaj eksperiment preverja.
 
**Acceptance criteria**
- Obrazec zahteva odločitveni datum, konec vrednotenja, kapital večji od 0 € in vsaj en instrument.
- Konec vrednotenja mora biti vsaj eno leto po odločitvenem datumu, sicer se ob polju izpiše napaka.
- Odločitveni datum mora biti znotraj obdobja, za katerega so v bazi cene; dovoljen razpon je izpisan ob polju.
- Ob veljavnih podatkih se eksperiment shrani s statusom „čaka na zagon“.
## US-03 – Izbira agentov in ocena stroška
**Funkcionalnost:** F1 | **Vloga:** Raziskovalec
 
Kot raziskovalec želim izbrati AI agente in videti ocenjen strošek pred zagonom, da ne porabim kreditov po nesreči.
 
**Acceptance criteria**
- Vsak agent v seznamu ima ime modela in oznako „plačljiv“ ali „brezplačen“.
- Ocenjen strošek v $ se ob vsaki spremembi izbire agentov ali ponovitev preračuna (vsota po agentih × število ponovitev).
- Gumb „Zaženi eksperiment“ je onemogočen, dokler ni izbran vsaj en agent.
- Če uporabnik preseže dnevno omejitev zagonov, backend vrne 429, UI pa izpiše, kdaj lahko znova zažene.
## US-04 – Zaprt in anonimiziran posnetek podatkov
**Funkcionalnost:** F2 | **Vloga:** Raziskovalec
 
Kot raziskovalec želim, da agent dobi samo podatke do odločitvenega datuma in brez pravih imen, da rezultat ni popačen z znanjem o prihodnosti.
 
**Acceptance criteria**
- Najpoznejši datum cene v posnetku ni kasnejši od odločitvenega datuma (preverjeno z avtomatskim testom).
- Zahtevek proti LLM API-ju ne vsebuje polj `ticker` in `ime`, samo anonimne oznake (npr. S042).
- Cene so normalizirane (indeks 100 na prvi dan) in povzete, ne surove.
- Isti instrument ima znotraj istega eksperimenta vedno isto anonimno oznako.
## US-05 – Odločitev agenta in validacija odgovora
**Funkcionalnost:** F3 | **Vloga:** Raziskovalec
 
Kot raziskovalec želim, da sistem preveri odgovor agenta, da v primerjavo vstopijo samo veljavni portfelji.
 
**Acceptance criteria**
- Backend sprejme odgovor samo v obliki JSON z utežmi po anonimnih oznakah in besedilom utemeljitve.
- Vsota uteži je 100 % (toleranca ±0,1 o. t.), sicer je odgovor zavrnjen.
- Vse oznake v odgovoru morajo obstajati v posnetku, sicer je odgovor zavrnjen.
- Ob zavrnjenem odgovoru backend ponovi zahtevek največ 2-krat; nato ima agent status „neuspešno“ z razlogom, viden uporabniku.
- Veljaven odgovor se shrani v `Napoved` (`razporeditev_json`, `utemeljitev_tekst`).
## US-06 – Vrednotenje portfelja z realnimi cenami
**Funkcionalnost:** F4 | **Vloga:** Raziskovalec
 
Kot raziskovalec želim videti, koliko bi portfelj agenta v resnici prinesel, da lahko ocenim kakovost njegovega nasveta.
 
**Acceptance criteria**
- Za vsako veljavno napoved sistem izračuna CAGR, nihajnost in največji padec iz zapiralnih cen med odločitvenim datumom in koncem vrednotenja.
- Metrike so shranjene v `koncni_donos_cagr`, `nihajnost` in `max_padec`, zaokrožene na 2 decimalki.
- Izračun za preprost primer (1 instrument, znani ceni) se ujema z ročnim izračunom (unit test).
- Če za instrument na določen dan ni cene, se uporabi zadnja znana cena, pri rezultatu pa je to označeno.
## US-07 – Primerjava z benchmark strategijami
**Funkcionalnost:** F5 | **Vloga:** Raziskovalec
 
Kot raziskovalec želim agente primerjati s S&P 500 in enakomerno porazdelitvijo, da vidim, ali je AI sploh boljši od preproste strategije.
 
**Acceptance criteria**
- Za vsak eksperiment se izračunata oba benchmarka v istem obdobju in z istim kapitalom.
- Benchmarka sta v lestvici in na grafu jasno ločena od agentov (črtkana črta, oznaka „referenca“).
- Ob vsakem agentu je prikazana razlika CAGR do S&P 500 v odstotnih točkah.
## US-08 – Graf rasti vrednosti portfeljev
**Funkcionalnost:** F6 | **Vloga:** Obiskovalec
 
Kot obiskovalec želim videti graf rasti vrednosti vseh portfeljev skozi čas, da hitro vidim, kdo je kdaj vodil.
 
**Acceptance criteria**
- Graf prikazuje vrednost portfelja vsakega agenta in benchmarka od kapitala na odločitveni datum do konca vrednotenja.
- Ob premiku miške na točko se prikažeta datum in vrednost v € za vsako serijo.
- Klik na element legende skrije ali prikaže pripadajočo serijo.
- Graf z 6 serijami in 6 leti podatkov se prikaže v manj kot 3 sekundah.
## US-09 – Lestvica uspešnosti
**Funkcionalnost:** F6 | **Vloga:** Obiskovalec
 
Kot obiskovalec želim lestvico agentov z ključnimi kazalniki, da jih lahko primerjam na enem mestu.
 
**Acceptance criteria**
- Tabela za vsakega agenta prikaže končno vrednost, zaslužek v €, CAGR, nihajnost in največji padec.
- Privzeta razvrstitev je po CAGR padajoče; klik na glavo stolpca spremeni razvrstitev.
- Najboljši agent je izpostavljen na vrhu nadzorne plošče.
- Zaslužek ima predznak (+ ali −), ne samo barve.
## US-10 – Pregled utemeljitve agenta
**Funkcionalnost:** F7 | **Vloga:** Obiskovalec
 
Kot obiskovalec želim prebrati, zakaj je agent izbral ali zavrnil posamezne instrumente, da ocenim kakovost njegovega razmišljanja.
 
**Acceptance criteria**
- Pri vsakem agentu je povezava na celotno besedilno utemeljitev.
- Prikazane so uteži izbranih instrumentov (vsota 100 %) in seznam zavrnjenih z razlogom.
- Utemeljitev je samo za branje; API ne ponuja urejanja ali brisanja.
## US-11 – Javni katalog eksperimentov
**Funkcionalnost:** F8 | **Vloga:** Obiskovalec
 
Kot obiskovalec želim brskati po vseh preteklih eksperimentih, da lahko analiziram rezultate brez lastnega zagona.
 
**Acceptance criteria**
- Katalog brez prijave prikaže eksperimente (datum odločitve, obdobje, število agentov, najboljši agent), največ 20 na stran.
- Seznam je mogoče filtrirati po agentu in razvrstiti po datumu zagona.
- Eksperiment je viden v katalogu šele, ko ga administrator odobri; do takrat ga vidi samo avtor.
- Prazen katalog prikaže sporočilo „Še ni eksperimentov“ in povezavo na nov eksperiment.
## US-12 – Uvoz cenovnih serij
**Funkcionalnost:** F9 | **Vloga:** Administrator
 
Kot administrator želim uvoziti nove cenovne serije v bazo, da lahko eksperimenti zajamejo več instrumentov.
 
**Acceptance criteria**
- Uvoz CSV (datum, zapiralna cena) poteka prek zaščitenega API-ja; brez veljavnega administratorskega ključa vrne 401 ali 403.
- Datoteka z neveljavnim datumom ali negativno ceno je zavrnjena, odgovor navede številko vrstice z napako.
- Po uvozu je izpisano število uvoženih vrstic; ponoven uvoz iste datoteke ne ustvari dvojnikov (unikatnost `STOCK_id` + `datum`).
- Novemu instrumentu se samodejno dodeli unikatna anonimna oznaka.
## US-13 – Upravljanje agentov in odobritev objave
**Funkcionalnost:** F9, F8 | **Vloga:** Administrator
 
Kot administrator želim urejati nastavitve agentov in odobriti objavo eksperimentov, da nadzorujem stroške in kakovost kataloga.
 
**Acceptance criteria**
- Administrator lahko agenta omogoči ali onemogoči; onemogočen agent se ne prikaže v obrazcu za nov eksperiment.
- Pri agentu je mogoče nastaviti oznako „plačljiv“ ali „brezplačen“ in ocenjen strošek zagona.
- Administrator lahko eksperiment odobri ali zavrne za javno objavo.
- Rezultatov že izvedenih eksperimentov administrator ne more spreminjati (API vrne 403 ali 405).
## US-14 – Večkratni zagoni (Monte Carlo)
**Funkcionalnost:** F10 | **Vloga:** Raziskovalec
 
Kot raziskovalec želim isti eksperiment z istim agentom ponoviti večkrat, da vidim, kako se odločitve spreminjajo zaradi nedeterminizma.
 
**Acceptance criteria**
- Ob zagonu je mogoče izbrati od 1 do 10 ponovitev.
- Vsaka ponovitev se shrani kot ločena napoved z enako konfiguracijo.
- Za vsakega agenta so prikazani povprečje, najmanjša in največja vrednost CAGR čez ponovitve.
- Ocenjen strošek se pomnoži s številom ponovitev (enako kot v US-03).
## US-15 – Izvoz rezultatov
**Funkcionalnost:** F11 | **Vloga:** Obiskovalec
 
Kot obiskovalec želim izvoziti podatke eksperimenta v JSON ali CSV, da jih lahko dodatno analiziram v svojih orodjih.
 
**Acceptance criteria**
- Na strani eksperimenta sta gumba „Izvozi JSON“ in „Izvozi CSV“.
- Izvoz vsebuje parametre eksperimenta, uteži, metrike in utemeljitve vseh agentov.
- Datoteka je v UTF-8, šumniki (č, š, ž) se pravilno prikažejo.
- Izvoz je na voljo tudi neprijavljenim obiskovalcem za javne eksperimente.