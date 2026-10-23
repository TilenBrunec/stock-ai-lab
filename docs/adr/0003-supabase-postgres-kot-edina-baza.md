---
status: "sprejeto"
date: 2026-10-23
---

# Supabase (PostgreSQL) kot edina podatkovna baza

## Kontekst in problem

Aplikacija hrani približno 15 let dnevnih vrednosti za okoli 200 delnic in indeksnih skladov (okoli 750.000 vrstic) ter podatke o uporabnikih, eksperimentih, agentih in portfeljih. Rešitev mora biti brezplačna, relacijska (entitete so med seboj povezane) in dovolj učinkovita za časovne vrste.

## Dejavniki odločitve

* Rešitev mora biti brezplačna ves čas razvoja, ne samo prvi mesec.
* Podatki so relacijski: uporabnik, eksperiment, agent, portfelj, delnica.
* Okoli 750.000 vrstic dnevnih vrednosti (ime, datum, vrednost) mora ostati pod brezplačno omejitvijo 500 MB.
* Čim manj različnih sistemov, ki jih je treba vzdrževati.
* Zmanjšanje kompleksnosti arhitekture: uporaba enega samega sistema za podatkovno shrambo in avtentikacijo (Supabase Auth) odpravlja potrebo po sinhronizaciji več storitev.

## Obravnavane alternative

* Supabase (PostgreSQL) kot edina baza
* Supabase (PostgreSQL) + DuckDB za analitiko časovnih vrst
* CockroachDB Cloud
* Lokalni PostgreSQL v Dockerju

## Odločitev

Izbrana alternativa: "Supabase (PostgreSQL) kot edina baza", ker je trajno brezplačen, relacijski, že vključuje Supabase Auth (ADR-0004) in podatki ustrezajo v 500 MB, hkrati pa ne uvaja drugega podatkovnega sistema.

### Posledice

* Dobro: ena baza za vse podatke in za avtentikacijo, brez sinhronizacije med sistemi.
* Dobro: Postgres z indeksom (delnica, datum) zadošča za grafe in izračune donosov na tej količini podatkov.
* Slabo: brezplačni paket ima omejitev 500 MB in ni primeren za bistveno več podatkov (npr. več delnic ali minutni podatki).
* Slabo: odvisnost od enega zunanjega ponudnika; projekt na brezplačnem paketu je lahko ob neaktivnosti začasno ustavljen.

## Prednosti in slabosti alternativ

### Supabase + DuckDB

* Dobro: hitrejše analitične poizvedbe nad časovnimi vrstami.
* Slabo: dva podatkovna sistema in sinhronizacija; pri 750.000 vrsticah korist ni potrebna.

### CockroachDB Cloud

* Dobro: Postgres-združljiv in porazdeljen.
* Slabo: brezplačen le en mesec, kar ne ustreza zahtevi po brezplačni rešitvi skozi celoten projekt.

### Lokalni PostgreSQL v Dockerju

* Dobro: brez omejitev velikosti in brez zunanje odvisnosti.
* Slabo: ni dostopen od drugod, zahteva lastno avtentikacijo in ni primeren za javno demo.
