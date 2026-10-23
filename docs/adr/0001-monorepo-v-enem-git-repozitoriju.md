---
status: "sprejeto"
date: 2026-10-23
---

# Backend, frontend in dokumentacija v enem git repozitoriju (monorepo)

## Kontekst in problem

Projekt StockLab sestavljata Spring Boot backend in React frontend, poleg njiju pa še dokumentacija za predmet. Odločiti se je treba, ali jih hraniti v enem ali v več repozitorijih.

## Dejavniki odločitve

* Projekt razvija ena oseba, zato je skrb za več repozitorijev odvečen strošek.
* Dokumentacija (`docs/`) mora biti ob kodi, da je ob oddaji vse na enem mestu.
* Sprememba API-ja in sprememba odjemalca sta pogosto povezani.

## Obravnavane alternative

* Monorepo (`stock-ai-lab`: `backend/`, `frontend/`, `docs/`)
* Ločena repozitorija za backend in frontend
* Tri repozitorije (backend, frontend, dokumentacija)

## Odločitev

Izbrana alternativa: "Monorepo", ker en razvijalec s tem dobi eno zgodovino, en issue tracker in atomarne commite, ki hkrati spremenijo API in njegovega odjemalca.

### Posledice

* Dobro: ena sprememba (npr. nov endpoint + njegova uporaba v Reactu) je en commit in en pull request.
* Dobro: dokumentacija in ADR-ji so vedno usklajeni z verzijo kode.
* Slabo: backend in frontend ne moreta imeti ločenih verzij ali pravic dostopa.
* Slabo: CI mora znati ločiti, kateri del se je spremenil, da ne gradi vsega ob vsaki spremembi.

## Prednosti in slabosti alternativ

### Ločena repozitorija

* Dobro: jasna ločnica med deloma, neodvisne verzije.
* Slabo: za enega razvijalca dvojno vzdrževanje, težje usklajevanje sprememb API-ja.

### Tri repozitorije

* Dobro: dokumentacija je neodvisna od kode.
* Slabo: največ režije, dokumentacija hitro zastara glede na kodo.
