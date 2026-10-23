---
status: "sprejeto"
date: 2026-10-23
---

# Java Spring Boot kot backend

## Kontekst in problem

Backend mora izpostaviti REST API za podatke o delnicah, upravljati eksperimente, klicati LLM-je in preverjati identiteto uporabnikov. Eksperiment je daljše, večkoraken delo (priprava podatkov, klici več agentov, izračun donosa), zato potrebujemo zrel ekosistem za pregledno poslovno logiko, validacijo in dostop do baze.

## Dejavniki odločitve

* Dobro poznavanje Jave in Spring ekosistema (hitrejši razvoj, manj tveganja).
* Podpora za validacijo, varnost (preverjanje JWT) in dostop do Postgresa.
* Omogočanje dobrega strukturiranja in testabilenosti.
*  močno tipizirana koda (DTO-ji, entitete) zmanjša napake med backendom in bazo

## Obravnavane alternative

* Java Spring Boot
* Node.js (Express / NestJS)
* Python (FastAPI)

## Odločitev

Izbrana alternativa: "Java Spring Boot", ker najbolje združuje obstoječe znanje, vgrajeno podporo za varnost, validacijo in dostop do baze ter jasno plastno strukturo kode.

### Posledice

* Dobro: Spring Security omogoča preverjanje JWT-jev brez lastne kriptografske kode.
* Dobro: močno tipizirana koda (DTO-ji, entitete) zmanjša napake med backendom in bazo.
* Slabo: več začetnega konfiguracijskega dela in več kode kot pri Node.js ali FastAPI.
* Slabo: večja poraba pomnilnika pri morebitnem brezplačnem gostovanju.

## Prednosti in slabosti alternativ

### Node.js (Express / NestJS)

* Dobro: ista jezikovna družina kot React frontend, manj kode.
* Slabo: manj strogo tipiziranja (brez TypeScripta), več odločitev o strukturi prepuščenih razvijalcu.

### Python (FastAPI)

* Dobro: najlažja integracija z LLM-ji in podatkovno analitiko.
* Slabo: LLM klici gredo prek HTTP API-ja (OpenRouter, glej ADR-0006), zato ta prednost odpade.

## Dodatne informacije

Struktura kode: ADR-0008.
