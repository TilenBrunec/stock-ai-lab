"""
Očisti surove cene iz data/raw/prices.csv in jih zapiše v data/clean/prices.csv.

Koraki:
  1. odstrani vrstice s prazno ali neštevilsko ceno
  2. odstrani cene <= 0
  3. odstrani neveljavne datume in datume v prihodnosti
  4. odstrani podvojene (ticker, datum) - obdrži prvo
  5. poročilo o manjkajočih trgovalnih dneh po tickerju

Za "trgovalni koledar" vzame vse datume, ki jih ima SPY (ali najpogostejše
datume, če SPY ni). Manjkajoči dan = dan v koledarju med prvim in zadnjim
datumom tickerja, za katerega ticker nima cene.

Uporaba (iz root mape):
    python scripts/clean_data.py

Izhod:
    data/clean/prices.csv   ticker,datum,cena
    data/clean/report.csv   ticker,prvi_datum,zadnji_datum,vrstic,manjkajocih_dni
"""

import logging
import sys
from datetime import date
from pathlib import Path

import pandas as pd

ROOT = Path(__file__).resolve().parent.parent
RAW_CSV = ROOT / "data" / "raw" / "prices.csv"
CLEAN_DIR = ROOT / "data" / "clean"
CLEAN_CSV = CLEAN_DIR / "prices.csv"
REPORT_CSV = CLEAN_DIR / "report.csv"
CALENDAR_TICKER = "SPY"

logging.basicConfig(level=logging.INFO, format="%(asctime)s %(levelname)s %(message)s")
log = logging.getLogger("clean_data")


def trading_calendar(df: pd.DataFrame) -> pd.DatetimeIndex:
    if CALENDAR_TICKER in set(df["ticker"]):
        days = df.loc[df["ticker"] == CALENDAR_TICKER, "datum"]
    else:
        # datumi, ki jih ima vsaj polovica tickerjev
        counts = df.groupby("datum")["ticker"].nunique()
        days = counts[counts >= df["ticker"].nunique() / 2].index.to_series()
    return pd.DatetimeIndex(sorted(days.unique()))


def missing_report(df: pd.DataFrame, calendar: pd.DatetimeIndex) -> pd.DataFrame:
    rows = []
    for ticker, g in df.groupby("ticker"):
        first, last = g["datum"].min(), g["datum"].max()
        expected = calendar[(calendar >= first) & (calendar <= last)]
        missing = len(expected.difference(pd.DatetimeIndex(g["datum"])))
        rows.append({
            "ticker": ticker,
            "prvi_datum": first.date(),
            "zadnji_datum": last.date(),
            "vrstic": len(g),
            "manjkajocih_dni": missing,
        })
    return pd.DataFrame(rows).sort_values("manjkajocih_dni", ascending=False)


def main() -> int:
    if not RAW_CSV.exists():
        log.error("Ni datoteke %s - najprej zaženi fetch_prices.py", RAW_CSV.relative_to(ROOT))
        return 1

    df = pd.read_csv(RAW_CSV, dtype={"ticker": str, "datum": str, "cena": str})
    start = len(df)
    log.info("Prebranih vrstic: %d", start)

    stats = {}

    df["cena"] = pd.to_numeric(df["cena"], errors="coerce")
    before = len(df)
    df = df.dropna(subset=["ticker", "cena"])
    stats["prazna ali neštevilska cena"] = before - len(df)

    before = len(df)
    df = df[df["cena"] > 0]
    stats["cena <= 0"] = before - len(df)

    df["datum"] = pd.to_datetime(df["datum"], errors="coerce")
    before = len(df)
    df = df.dropna(subset=["datum"])
    df = df[df["datum"] <= pd.Timestamp(date.today())]
    stats["neveljaven ali prihodnji datum"] = before - len(df)

    before = len(df)
    df = df.drop_duplicates(subset=["ticker", "datum"], keep="first")
    stats["podvojen (ticker, datum)"] = before - len(df)

    for reason, n in stats.items():
        log.info("Odstranjeno - %s: %d", reason, n)

    df = df.sort_values(["ticker", "datum"])
    CLEAN_DIR.mkdir(parents=True, exist_ok=True)
    out = df.assign(datum=df["datum"].dt.date, cena=df["cena"].round(4))
    out.to_csv(CLEAN_CSV, index=False)

    report = missing_report(df, trading_calendar(df))
    report.to_csv(REPORT_CSV, index=False)

    log.info("Očiščeno: %d vrstic (%d odstranjenih) za %d tickerjev -> %s",
             len(df), start - len(df), df["ticker"].nunique(), CLEAN_CSV.relative_to(ROOT))
    worst = report[report["manjkajocih_dni"] > 0].head(10)
    if worst.empty:
        log.info("Manjkajočih trgovalnih dni ni.")
    else:
        log.info("Največ manjkajočih dni:\n%s", worst.to_string(index=False))
    log.info("Celotno poročilo: %s", REPORT_CSV.relative_to(ROOT))
    return 0


if __name__ == "__main__":
    sys.exit(main())
