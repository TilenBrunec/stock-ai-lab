---
status: "sprejeto"
date: 2026-10-23
---

# Omejitev podatkov na ~200 delnic, ~15 let in samo dnevno vrednost

## Kontekst in problem

Prvotna ideja je bila hraniti približno 50 let podatkov za znane delnice in indeksne sklade. Obseg podatkov vpliva na velikost baze (brezplačna omejitev), čas nalaganja grafov, velikost vhoda za LLM (stroški) in dostopnost vira podatkov.

## Dejavniki odločitve

* Brezplačna baza (omejitev 500 MB, ADR-0003).
* Vhodni podatki za LLM naj bodo dovolj majhni, da stroški ne presežejo proračuna.
* Podatki morajo biti na voljo brezplačno in enotno za vse delnice.
* Pri MVP je pomembnejši delujoč potek (eksperiment → rezultat) kot obseg podatkov.

## Obravnavane alternative

* ~200 delnic, ~15 let, samo ime in dnevna vrednost
* ~200 delnic, ~50 let
* Več atributov (odpiralna, najvišja, najnižja cena, volumen)

## Odločitev

Izbrana alternativa: "~200 delnic, ~15 let, samo ime in dnevna vrednost", ker daje okoli 750.000 vrstic, kar je znatno pod omejitvijo baze, vhod za LLM pa ostane obvladljiv, hkrati pa obdobje 15 let še vedno vključuje različne tržne razmere.

### Posledice

* Dobro: majhna baza in hiter prikaz grafov (okoli 3.800 točk na delnico).
* Dobro: manjši vhod za LLM pomeni nižje stroške na zagon eksperimenta.
* Slabo: eksperimenti ne morejo simulirati odločitev starejših od približno 15 let.
* Slabo: z enim samim atributom (dnevna vrednost) ni mogoče izračunati volatilnosti iz dnevnih razponov ali analizirati volumna.
* Slabo: razširitev na več let ali atributov bo zahtevala novo odločitev o hrambi (nov ADR).

## Prednosti in slabosti alternativ

### 50 let podatkov

* Dobro: daljši zgodovinski eksperimenti.
* Slabo: večja baza, težje pridobivanje podatkov za vseh 200 simbolov, višji stroški LLM.

### Več atributov

* Dobro: bogatejši vhod za agenta.
* Slabo: večkratna velikost baze in vhoda, za MVP nepotrebno.
