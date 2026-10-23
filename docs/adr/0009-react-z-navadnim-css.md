---
status: "sprejeto"
date: 2026-10-23
---

# React frontend z navadnim CSS (brez Tailwinda)

## Kontekst in problem

Frontend prikazuje grafe, tabele in obrazce ter komunicira z backendom prek REST API-ja. Odločiti se je treba o ogrodju in načinu stiliranja.

## Dejavniki odločitve

* Interaktivni grafi in stanje (izbrane delnice, obdobje, status eksperimenta) zahtevajo komponentni pristop.
* Čim manj odvisnosti in orodij za konfiguracijo.
* Videz je sekundaren, pomembna je jasnost wireframe-ov (`docs/06-wireframes.md`).

## Obravnavane alternative

* React + navaden CSS
* React + Tailwind CSS
* Vue ali Angular

## Odločitev

Izbrana alternativa: "React + navaden CSS", ker React najbolj ustreza interaktivnim grafom in stanju, navaden CSS pa ne zahteva dodatne odvisnosti in konfiguracije.

### Posledice

* Dobro: manj odvisnosti, preprostejša gradnja in poln nadzor nad slogi.
* Slabo: več ročnega pisanja CSS-ja in večje tveganje za nedosledno stiliranje.
* Slabo: ni vnaprej pripravljenega sistema oblikovanja (razmiki, barve), zato jih moramo določiti sami.

## Prednosti in slabosti alternativ

### React + Tailwind

* Dobro: hitrejše stiliranje, dosledni razmiki in barve.
* Slabo: dodaten korak gradnje in konfiguracije, dolge sezname razredov v kodi.

### Vue ali Angular

* Dobro: vsak ima lasten, dobro dokumentiran ekosistem.
* Slabo: manj poznano kot React, brez prednosti za ta projekt.
