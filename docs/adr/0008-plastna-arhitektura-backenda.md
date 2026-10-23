---
status: "sprejeto"
date: 2026-10-23
---

# Plastna arhitektura backenda (controller, service, repository, model, dto, config)

## Kontekst in problem

Backend združuje REST API, poslovno logiko eksperimentov (anonimizacija, klici agentov, izračun donosa) in dostop do baze. Brez jasne strukture bi se poslovna pravila pomešala s HTTP in SQL kodo, kar otežuje testiranje in spremembe.

## Dejavniki odločitve

* Poslovna logika (zlasti US-10 in US-11) mora biti testabilna brez HTTP in baze.
* API ne sme razkrivati internih entitet (npr. preslikave anonimnih ID-jev).
* Struktura mora biti znana in običajna za Spring Boot.

## Obravnavane alternative

* Plastna arhitektura po tehničnih vlogah (controller / service / repository / model / dto / config)
* Paketi po funkcionalnostih (experiment, stock, agent, user)
* Heksagonalna arhitektura (porti in adapterji)

## Odločitev

Izbrana alternativa: "Plastna arhitektura po tehničnih vlogah", ker je standardna za Spring Boot, jasno loči HTTP od poslovne logike in baze ter ne zahteva dodatnih abstrakcij za projekt te velikosti.

### Posledice

* Dobro: servise je mogoče testirati z enotnimi testi, brez zagona strežnika.
* Dobro: DTO-ji ločijo API od entitet, zato sprememba baze ne spremeni nujno API-ja.
* Slabo: ob rasti projekta se funkcionalnosti razpršijo čez več paketov in jih je težje najti.
* Slabo: več razredov (entiteta + DTO + preslikave) za vsako preprosto operacijo.

## Prednosti in slabosti alternativ

### Paketi po funkcionalnostih

* Dobro: vse za eno funkcionalnost na enem mestu.
* Slabo: manj običajno za ekipo in tečaj, brez jasne ločitve tehničnih odgovornosti.

### Heksagonalna arhitektura

* Dobro: poslovna logika je neodvisna od ogrodja in baze.
* Slabo: za en projekt in enega razvijalca prinaša preveč abstrakcij.
