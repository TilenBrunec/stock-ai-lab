---
status: "sprejeto"
date: 2026-10-23
---

# OpenRouter kot enoten dostop do LLM modelov z lastnim API ključem (BYOK)

## Kontekst in problem

Aplikacija primerja več LLM agentov (npr. Claude, GPT in brezplačne modele). Vsak ponudnik ima lasten API, avtentikacijo in cene, kar bi pomenilo več integracij. Ker eksperimente poganjajo uporabniki sami, javno gostovanje aplikacije z enim skupnim API ključem predstavlja finančno tveganje (nepredvideni stroški ali hitro izčrpani krediti).

## Dejavniki odločitve

* Uporabnik mora imeti možnost testiranja poljubnih agentov in modelov brez finančnega bremena za lastnika aplikacije (model BYOK - Bring Your Own Key).
* Integracija mora biti enotna: dodajanje novega agenta ne sme zahtevati sprememb v kodi backend-a.
* Vmesnik mora omogočati dostop tako do najmočnejših komercialnih modelov kot tudi do brezplačnih variant.

## Obravnavane alternative

* OpenRouter z vnosom uporabniškega API ključa (BYOK)
* Skupni OpenRouter API ključ v lasti aplikacije
* Neposredni API-ji ponudnikov (Anthropic, OpenAI, …)
* Lokalni modeli (npr. Ollama)

## Odločitev

Izbrana alternativa: "OpenRouter z vnosom uporabniškega API ključa", ker en sam API vmesnik omogoča dostop do stotina modelov, uporabnik pa v aplikacijo (v nastavitvah ali ob zagonu eksperimenta) vnese svoj OpenRouter API ključ. Backend ta ključ uporabi le kot posrednik za avtentikacijo zahtevkov na OpenRouter.

### Posledice

* Dobro: Oskrba s krediti in stroški sta v celoti na strani uporabnika – aplikacija nima tveganja nepredvidenih stroškov ali zlorabe API ključa.
* Dobro: Enostavna arhitektura na backendu (en API odjemalec), nov agent pomeni le izbiro nove ID oznake modela (npr. `openai/gpt-4o` ali `anthropic/claude-3.5-sonnet`).
* Dobro: Uporabnik lahko prosto eksperimentira tudi z povsem brezplačnimi modeli, ki jih ponuja OpenRouter.
* Slabo: Manjša uporabniška izkušnja (friction) za nove uporabnike, saj si morajo ustvariti OpenRouter račun in pridobiti lasten API ključ pred začetkom uporabe.
* Slabo: Občutljivost shranjevanja – ključ uporabnika se mora varno prenašati (npr. v glavi zahtevka ali šifriran v sejno shrambo) in se ne sme trajno shranjevati v nešifrirani obliki na strežniku.

## Prednosti in slabosti alternativ

### Skupni OpenRouter API ključ aplikacije

* Dobro: Boljša uporabniška izkušnja, saj uporabniku ni treba vnašati ključev.
* Slabo: Visoko finančno tveganje za avtorja aplikacije; nevarnost zlorab ali hitrega doseganja omejitev kreditov pri večjem številu eksperimentov.

### Neposredni API-ji ponudnikov

* Dobro: Neposredna povezava brez posrednika.
* Slabo: Uporabnik bi moral vnašati več različnih API ključev (OpenAI, Anthropic, Google) posebej za vsakega agenta; zahteva več različnih integracij v backendu.

### Lokalni modeli (Ollama)

* Dobro: Popolnoma brez stroškov in brez potrebe po spletnih API ključih.
* Slabo: Zahteva močno lokalno strojno opremo ter ne omogoča objektivne primerjave z vodilnimi komercialnimi LLM modeli v oblaku.

## Dodatne informacije

Varnost in prenos uporabniških ključev: ADR-0004 (Avtentikacija) in ADR-0008 (Struktura klicanja zunanjih storitev).