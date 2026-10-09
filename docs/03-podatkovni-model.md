# 03 – Podatkovni model

## Entitete in glavna polja

### 1. `Agent` (AI Agent)
Shranjuje podatke o uporabljenih modelih umetne inteligence.
- `id` (UUID, PK): Unikatni identifikator agenta.
- `ime` (String): Poimenovanje ali oznaka modela (npr. `GPT-4o`, `Claude 3.5 Sonnet`).

### 2. `STOCK` (Financial STOCK)
Shranjuje osnovne podatke o posamezni delnici ali indeksnem skladu.
- `id` (UUID, PK): Unikatni identifikator.
- `ticker` (String): Prava borzna oznaka (npr. `AAPL`, `NVDA`).
- `ime` (String): Polno ime podjetja oz. sklada.
- `anonimno_ime` (String): Anonimizirana oznaka za AI agenta (npr. `S042`).
- `sektor` (String): Panoga podjetja.

### 3. `STOCKCena` (STOCK Daily Price)
Shranjuje zgodovinske dnevne cene STOCKov za generiranje posnetkov in vrednotenje.
- `id` (UUID, PK): Unikatni identifikator zapisa.
- `STOCK_id` (UUID, FK): Povezava na pripadajoči STOCK.
- `datum` (Date): Datum trgovalnega dne.
- `zapiralna_cena` (Decimal): Tečaj STOCKa na dani datum.

### 4. `Napoved` (Agent Prediction / Result)
Shranjuje odločitev agenta za določen datum, njegovo utemeljitev ter izračunano uspešnost.
- `id` (UUID, PK): Unikatni identifikator napovedi.
- `agent_id` (UUID, FK): Povezava na agenta, ki je ustvaril napoved.
- `odlocitveni_datum` (Date): Datum, do katerega je agent videl podatke.
- `konec_vrednotenja` (Date): Datum, do katerega se meri uspešnost.
- `utemeljitev_tekst` (Text): Podrobna tekstovna obrazložitev odločitve.
- `razporeditev_json` (JSON): Struktura izbranih anonimnih STOCKov in uteži (`{"S042": 0.40, "S012": 0.60}`).
- `koncni_donos_cagr` (Decimal): Izračunana letna stopnja rasti.
- `max_padec` (Decimal): Izračunan največji padec vrednosti portfelja.
- `nihajnost` (Decimal): Izračunana volatilnost.

---

## Povezave med entitetami

- **Agent – Napoved (1 : N):** En agent lahko ustvari več napovedi na različne datume ali v različnih eksperimentih.
- **STOCK – STOCKCena (1 : N):** Posamezen STOCK ima več dnevnih cen skozi čas.
- **Napoved – STOCKCena (1 : N):** Uspešnost napovedi se ovrednoti z uporabo večih dnevnih cen izbranih STOCKov po odločitvenem datumu.
- **Napoved – STOCK (N : M):** Posamezna napoved vsebuje več izbranih STOCKov (preko JSON strukture uteži), posamezen STOCK pa je vključen v več napovedih.

---

## Osnutek podatkovnega modela (Mermaid diagram)

```mermaid
erDiagram
    AGENT ||--o{ NAPOVED : ustvari
    STOCK ||--o{ STOCK_CENA : ima_zgodovino
    NAPOVED }|--o{ STOCK_CENA : vrednoti_s_cenami
    NAPOVED }|--|{ STOCK : vključuje

    AGENT {
        uuid id PK
        string ime
    }

    NAPOVED {
        uuid id PK
        uuid agent_id FK
        date odlocitveni_datum
        date konec_vrednotenja
        text utemeljitev_tekst
        json razporeditev_json
        decimal koncni_donos_cagr
        decimal max_padec
        decimal nihajnost
    }

    STOCK {
        uuid id PK
        string ticker
        string ime
        string anonimno_ime
        string sektor
    }

    STOCK_CENA {
        uuid id PK
        uuid instrument_id FK
        date datum
        decimal zapiralna_cena
    }