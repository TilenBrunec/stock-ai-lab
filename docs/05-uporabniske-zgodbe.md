# 05 – Uporabniške zgodbe

Vloge: **Obiskovalec** (neprijavljen), **Raziskovalec** (prijavljen z Google računom), **Administrator**.
Funkcionalnosti (F1–F15) so opisane v [02 – Funkcionalnosti in obseg](02-funkcionalnosti-in-obseg.md).

## Pregled

| Zgodba | Naslov | Funkcionalnost | Obseg | Stanje |
|---|---|---|---|---|
| US-16 | Pregled instrumentov in graf cene | F1 | MVP | ✅ implementirano |
| US-12 | Uvoz cenovnih serij | F2 | MVP (poenostavljeno) | ✅ implementirano |
| US-01 | Prijava z Google računom | F3 | MVP | načrtovano |
| US-02 | Shranjevanje OpenRouter API ključa | F4 | MVP | načrtovano |
| US-03 | Izbira instrumentov in obdobja | F5 | MVP | načrtovano |
| US-04 | Izbira modelov | F5 | MVP | načrtovano |
| US-05 | Zaprt in anonimiziran posnetek podatkov | F6 | MVP | načrtovano |
| US-06 | Odločitev in utemeljitev agenta | F7 | MVP | načrtovano |
| US-07 | Vrednotenje uspešnosti odločitev | F8 | MVP | načrtovano |
| US-08 | Rezultati eksperimenta | F9 | MVP | načrtovano |
| US-09 | Primerjava modelov | F9 | MVP | načrtovano |
| US-10 | Primerjava z benchmark strategijami | F10 | razširitev | – |
| US-11 | Javni katalog eksperimentov | F11 | razširitev | – |
| US-13 | Upravljanje modelov in odobritev objave | F12 | razširitev | – |
| US-14 | Večkratni zagoni (Monte Carlo) | F13 | razširitev | – |
| US-15 | Izvoz rezultatov | F14 | razširitev | – |

---

## US-16 – Pregled instrumentov in graf cene ✅
**Funkcionalnost:** F1 | **Vloga:** Obiskovalec

Kot obiskovalec želim videti seznam instrumentov in graf zgodovine cene, da preverim, katere podatke ima sistem.

**Pogoji sprejemljivosti**
- Seznam prikaže vse instrumente z imenom, tickerjem in tipom (delnica/sklad), z iskanjem po imenu ali tickerju.
- Klik na instrument odpre graf zapiralnih cen z izbiro obdobja (1 leto, 5 let, vse).
- Endpointa `GET /api/stocks` in `GET /api/stocks/{id}/prices` sta javna; neobstoječ instrument vrne 404, neveljaven datum 400.

## US-12 – Uvoz cenovnih serij ✅
**Funkcionalnost:** F2 | **Vloga:** Administrator

Kot administrator želim uvoziti zgodovinske cene v bazo, da imajo eksperimenti na voljo podatke.

*MVP poenostavljeno: uvoz s Python skriptami namesto administratorskega API-ja (API je razširitev F15).*

**Pogoji sprejemljivosti**
- Seznam instrumentov je v `data/tickers.csv`; vključuje tudi slabše uspešna podjetja, ne samo zmagovalcev.
- Skripta prenese ~15 let dnevnih prilagojenih zapiralnih cen; tickerji brez podatkov so izpisani v `missing_tickers.txt`.
- Prazne, negativne, neveljavne in podvojene vrstice so odstranjene; izpiše se poročilo o manjkajočih dnevih.
- Ponoven uvoz ne ustvari dvojnikov (unikatnost `stock_id` + `datum`).
- Vsak instrument dobi naključno unikatno anonimno oznako (S001 …), ki se ob ponovnem uvozu ne spremeni.

---

## US-01 – Prijava z Google računom
**Funkcionalnost:** F3 | **Vloga:** Obiskovalec

Kot obiskovalec se želim prijaviti z Google računom, da lahko zaganjam eksperimente brez ustvarjanja novega gesla.

**Pogoji sprejemljivosti**
- Gumb „Prijava z Google“ je viden neprijavljenim obiskovalcem na vsaki strani.
- Po prijavi frontend prejme JWT žeton (Supabase Auth) ter prikaže ime in sliko računa.
- Ob prvem klicu `GET /api/me` backend ustvari uporabnika z vlogo „raziskovalec“.
- Klic zaščitenega endpointa (npr. zagon eksperimenta) brez veljavnega žetona vrne 401.
- Po odjavi je žeton izbrisan in zaščiteni endpointi niso dosegljivi brez nove prijave.

## US-02 – Shranjevanje OpenRouter API ključa
**Funkcionalnost:** F4 | **Vloga:** Raziskovalec

Kot raziskovalec želim v aplikacijo shraniti svoj OpenRouter API ključ, da se klici modelov izvajajo na mojem računu.

**Pogoji sprejemljivosti**
- V nastavitvah profila je polje za vnos ključa; ob shranjevanju backend s testnim klicem na OpenRouter preveri, da je ključ veljaven, sicer vrne napako.
- Ključ je v bazi shranjen šifrirano (ključ za šifriranje je v okoljski spremenljivki, ne v kodi ali bazi).
- API nikoli ne vrne celotnega ključa; prikazani so samo zadnji 4 znaki (npr. `••••a1b2`).
- Raziskovalec lahko ključ zamenja ali izbriše.
- Brez shranjenega ključa je gumb „Zaženi eksperiment“ onemogočen, z napotkom na nastavitve.

## US-03 – Izbira instrumentov in obdobja
**Funkcionalnost:** F5 | **Vloga:** Raziskovalec

Kot raziskovalec želim izbrati instrumente, odločitveni datum, obdobje vrednotenja in kapital, da natančno določim, kaj eksperiment preverja.

**Pogoji sprejemljivosti**
- Instrumente lahko izberem ročno (iskanje po seznamu) ali naključno (vnesem število, sistem izbere toliko naključnih instrumentov).
- Eksperiment ima od 1 do 20 instrumentov.
- Odločitveni datum mora pustiti dovolj zgodovine pred njim (najmanj 1 leto) in vsaj 1 leto podatkov po njem; dovoljen razpon je izpisan ob polju.
- Konec vrednotenja je po odločitvenem datumu in ne kasnejši od zadnje cene v bazi.
- Kapital mora biti večji od 0 €.
- Ob veljavnih podatkih se eksperiment shrani s statusom „čaka na zagon“; ob neveljavnih se napaka izpiše ob polju.

## US-04 – Izbira modelov
**Funkcionalnost:** F5 | **Vloga:** Raziskovalec

Kot raziskovalec želim izbrati enega ali več AI modelov, da jih lahko na istem eksperimentu primerjam.

**Pogoji sprejemljivosti**
- Seznam prikazuje razpoložljive modele (npr. Claude, GPT, brezplačni modeli) z oznako „plačljiv“ ali „brezplačen“.
- Izbran mora biti vsaj en model, sicer je gumb „Zaženi eksperiment“ onemogočen.
- Vsi izbrani modeli dobijo enak posnetek podatkov in enaka navodila.

## US-05 – Zaprt in anonimiziran posnetek podatkov
**Funkcionalnost:** F6 | **Vloga:** Raziskovalec

Kot raziskovalec želim, da model dobi samo podatke do odločitvenega datuma in brez pravih imen, da rezultat ni popačen z znanjem o prihodnosti.

**Pogoji sprejemljivosti**
- Najpoznejši datum cene v posnetku ni kasnejši od odločitvenega datuma (preverjeno z avtomatskim testom).
- Zahtevek proti OpenRouterju ne vsebuje tickerjev ali imen, samo anonimne oznake (npr. S042).
- Cene so normalizirane (indeks 100 na prvi dan) in povzete (npr. tedenske vrednosti), ne surove dnevne.
- Isti instrument ima znotraj eksperimenta vedno isto oznako.

## US-06 – Odločitev in utemeljitev agenta
**Funkcionalnost:** F7 | **Vloga:** Raziskovalec

Kot raziskovalec želim, da model za vsak instrument odloči, ali bi ga kupil, in odločitev utemelji, da lahko ocenim njegovo razmišljanje.

**Pogoji sprejemljivosti**
- Model vrne JSON, v katerem ima vsak instrument iz posnetka natanko eno odločitev (`KUPI` ali `NE_KUPI`) in besedilno utemeljitev.
- Odgovor z manjkajočim instrumentom, neznano oznako ali neveljavno odločitvijo je zavrnjen.
- Ob zavrnjenem odgovoru ali napaki OpenRouterja backend zahtevek ponovi največ 2-krat; nato ima model status „neuspešno“ z razlogom, ki je viden uporabniku.
- Veljavne odločitve in utemeljitve se shranijo v bazo skupaj z modelom in eksperimentom.
- Zagon ne blokira uporabnika: eksperiment ima status („v teku“, „končano“, „neuspešno“), ki ga frontend osvežuje.

## US-07 – Vrednotenje uspešnosti odločitev
**Funkcionalnost:** F8 | **Vloga:** Raziskovalec

Kot raziskovalec želim vedeti, koliko bi odločitve modela v resnici zaslužile, da lahko ocenim kakovost njegovega nasveta.

**Pogoji sprejemljivosti**
- Kapital se enakomerno razdeli med instrumente z odločitvijo `KUPI`; če model ne izbere nobenega, ostane kapital nespremenjen.
- Iz cen med odločitvenim datumom in koncem vrednotenja sistem izračuna končno vrednost, zaslužek v €, donos v % in CAGR.
- Za vsak instrument se izračuna njegov donos; odločitev je „pravilna“, če je `KUPI` in donos > 0 ali `NE_KUPI` in donos ≤ 0. Shrani se delež pravilnih odločitev.
- Če na določen dan ni cene, se uporabi zadnja znana cena.
- Izračun za preprost primer (1 instrument, znani ceni) se ujema z ročnim izračunom (unit test).

## US-08 – Rezultati eksperimenta
**Funkcionalnost:** F9 | **Vloga:** Raziskovalec

Kot raziskovalec želim na enem mestu videti odločitve, utemeljitve in uspešnost vseh modelov v eksperimentu.

**Pogoji sprejemljivosti**
- Tabela za vsak model prikaže končno vrednost, zaslužek v € s predznakom (+/−), donos, CAGR in delež pravilnih odločitev; privzeto razvrščeno po donosu.
- Za vsak model so vidne odločitve po instrumentih z utemeljitvami.
- Po zaključku so pri instrumentih razkriti pravi tickerji in imena ter njihov dejanski donos.
- Graf prikazuje rast vrednosti portfelja vsakega modela od odločitvenega datuma do konca vrednotenja.
- Rezultate vidi samo avtor eksperimenta; utemeljitev in rezultatov ni mogoče urejati.

## US-09 – Primerjava modelov
**Funkcionalnost:** F9 | **Vloga:** Raziskovalec

Kot raziskovalec želim primerjati modele čez vse svoje eksperimente, da vidim, kateri se na splošno odloča najbolje.

**Pogoji sprejemljivosti**
- Lestvica za vsak model prikaže število eksperimentov, povprečni donos in povprečni delež pravilnih odločitev.
- Upoštevani so samo uspešno zaključeni eksperimenti.
- Klik na model prikaže seznam eksperimentov, v katerih je sodeloval.

---

## Razširitve

## US-10 – Primerjava z benchmark strategijami
**Funkcionalnost:** F10 | **Vloga:** Raziskovalec

Kot raziskovalec želim modele primerjati s S&P 500 in enakomerno porazdelitvijo, da vidim, ali je AI sploh boljši od preproste strategije.

**Pogoji sprejemljivosti**
- Za vsak eksperiment se izračunata oba benchmarka v istem obdobju in z istim kapitalom.
- Benchmarka sta v tabeli in na grafu jasno ločena od modelov (črtkana črta, oznaka „referenca“).
- Ob vsakem modelu je prikazana razlika donosa do S&P 500 v odstotnih točkah.

## US-11 – Javni katalog eksperimentov
**Funkcionalnost:** F11 | **Vloga:** Obiskovalec

Kot obiskovalec želim brskati po preteklih eksperimentih, da lahko analiziram rezultate brez lastnega zagona.

**Pogoji sprejemljivosti**
- Katalog brez prijave prikaže odobrene eksperimente (odločitveni datum, obdobje, število modelov, najboljši model), največ 20 na stran.
- Seznam je mogoče filtrirati po modelu in razvrstiti po datumu zagona.
- Prazen katalog prikaže sporočilo „Še ni eksperimentov“.

## US-13 – Upravljanje modelov in odobritev objave
**Funkcionalnost:** F12 | **Vloga:** Administrator

Kot administrator želim upravljati seznam modelov in odobriti objavo eksperimentov, da nadzorujem kakovost kataloga.

**Pogoji sprejemljivosti**
- Administrator lahko model doda, omogoči ali onemogoči; onemogočen model se ne prikaže v obrazcu za eksperiment.
- Administrator lahko eksperiment odobri ali zavrne za javni katalog.
- Rezultatov izvedenih eksperimentov ne more spreminjati (API vrne 403 ali 405).

## US-14 – Večkratni zagoni (Monte Carlo)
**Funkcionalnost:** F13 | **Vloga:** Raziskovalec

Kot raziskovalec želim isti eksperiment z istim modelom ponoviti večkrat, da vidim, kako se odločitve spreminjajo zaradi nedeterminizma.

**Pogoji sprejemljivosti**
- Ob zagonu je mogoče izbrati od 1 do 10 ponovitev.
- Vsaka ponovitev se shrani kot ločen zagon z enako konfiguracijo.
- Za vsak model so prikazani povprečje, najmanjša in največja vrednost donosa čez ponovitve.

## US-15 – Izvoz rezultatov
**Funkcionalnost:** F14 | **Vloga:** Raziskovalec

Kot raziskovalec želim izvoziti podatke eksperimenta v JSON ali CSV, da jih lahko analiziram v svojih orodjih.

**Pogoji sprejemljivosti**
- Na strani eksperimenta sta gumba „Izvozi JSON“ in „Izvozi CSV“.
- Izvoz vsebuje parametre eksperimenta, odločitve, utemeljitve in metrike vseh modelov.
- Datoteka je v UTF-8; šumniki (č, š, ž) se pravilno prikažejo.
