---
status: "sprejeto"
date: 2026-10-23
---

# Prijava z Google računom (Supabase Auth) in preverjanje JWT v Spring Bootu

## Kontekst in problem

Aplikacija ima tri vloge (obiskovalec, raziskovalec, administrator). Ustvarjanje eksperimentov porabi plačljive LLM kredite, zato mora biti zaščiteno s prijavo. Odločiti se je treba, kdo upravlja identiteto in kako backend preverja, kdo kliče API.

## Dejavniki odločitve

* Ne želimo hraniti gesel in sami skrbeti za ponastavitev gesla, potrjevanje e-pošte ipd.
* Ustvarjanje eksperimentov stane (OpenRouter krediti), zato morajo biti anonimni obiskovalci omejeni na pregled podatkov.
* Rešitev mora biti brezplačna in čim manj kode.

## Obravnavane alternative

* Supabase Auth z Google prijavo, JWT preverja Spring Boot
* Lastna prijava z e-pošto in geslom (Spring Security)
* Brez prijave (vsi lahko zaganjajo eksperimente)
* Samostojen identity provider (npr. Keycloak)

## Odločitev

Izbrana alternativa: "Supabase Auth z Google prijavo, JWT preverja Spring Boot", ker identiteto prepusti preverjenemu ponudniku (nobenih gesel v naši bazi), je brezplačna že znotraj Supabase, backend pa mora samo preveriti podpis žetona in prebrati vlogo.

### Posledice

* Dobro: ni hranjenja gesel, ni ponastavitve gesla in ni potrjevanja e-pošte.
* Dobro: neprijavljen obiskovalec ima brez dodatne kode samo bralni dostop do podatkov o delnicah.
* Slabo: uporabnik brez Google računa se ne more prijaviti.
* Slabo: vloga (raziskovalec / administrator) ni del Google identitete, zato jo moramo hraniti sami v tabeli uporabnikov.

## Prednosti in slabosti alternativ

### Lastna prijava z geslom

* Dobro: neodvisno od Googla.
* Slabo: odgovornost za varno hrambo gesel, ponastavitev, potrjevanje e-pošte.

### Brez prijave

* Dobro: najenostavneje.
* Slabo: kdorkoli bi lahko porabljal plačljive LLM kredite; eksperimenti niso povezani z lastnikom.

### Keycloak

* Dobro: popoln nadzor nad vlogami in prijavo.
* Slabo: dodaten strežnik za vzdrževanje in gostovanje, preveč za obseg projekta.

## Dodatne informacije

Vloge in avtorizacija po vlogah: `docs/05-uporabniske-zgodbe.md`. Posodobi US-01 in US-02 v skladu s to odločitvijo (Google prijava, ne e-pošta in geslo).
