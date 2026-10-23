---
status: "sprejeto"
date: 2026-10-23
---

# Anonimni ID-ji delnic in strogo časovno omejeni podatki za LLM

## Kontekst in problem

Bistvo aplikacije je preveriti, kako bi LLM agent odločal ob izbranem datumu v preteklosti, ne da bi poznal prihodnost. Veliki jezikovni modeli pa si lahko zapomnijo, kako se je določena delnica gibala po letu, ki ga simuliramo, zato bi rezultati odražali predznanje (look-ahead bias) in ne sposobnosti odločanja.

## Dejavniki odločitve

* Rezultat eksperimenta mora biti znanstveno smiseln in primerljiv med agenti.
* Delnice niso samo zmagovalke (nabor ne sme biti pristranski).
* Pravila mora uveljavljati strežnik, ne uporabnik ali prompt.

## Obravnavane alternative

* Anonimni ID-ji delnic + podatki samo do datuma odločitve
* Prava imena delnic, podatki samo do datuma odločitve
* Prava imena delnic, agentu v promptu naročimo, naj ignorira prihodnost

## Odločitev

Izbrana alternativa: "Anonimni ID-ji delnic + podatki samo do datuma odločitve", ker edina odpravi možnost, da agent delnico prepozna po imenu in uporabi znanje o njeni poznejši uspešnosti.

### Posledice

* Dobro: primerjava agentov je poštena, izid ni odvisen od tega, kaj je model "slišal" o posameznih podjetjih.
* Dobro: filtriranje po datumu in zamenjava imen se izvaja na backendu, zato ju je mogoče avtomatsko testirati (noben vhodni zapis ni poznejši od datuma odločitve).
* Slabo: agent ne more uporabiti kvalitativnega znanja (panoga, novice), odloča le po številkah.
* Slabo: potrebujemo preslikavo ID → ime in jo moramo ohraniti stabilno med uvozi podatkov.
* Slabo: anonimizacija ni popolna; ob dovolj značilni krivulji bi model lahko še vedno prepoznal znano delnico.

## Prednosti in slabosti alternativ

### Prava imena, samo časovna omejitev

* Dobro: agent lahko uporabi kontekst o podjetju.
* Slabo: odpira se look-ahead bias prek zapomnjenega znanja.

### Prava imena + navodilo v promptu

* Dobro: najenostavneje.
* Slabo: navodilo modela ne preprečuje uporabe zapomnjenega znanja in ga ni mogoče preveriti.
