"""
Prenese ~15 let dnevnih prilagojenih zapiralnih cen (adjusted close)
za vse instrumente iz data/tickers.csv in jih shrani v data/raw/prices.csv.

Uporaba (iz root mape repozitorija):
    pip install -r scripts/requirements.txt
    python scripts/fetch_prices.py
    python scripts/fetch_prices.py --start 2011-01-01

Izhod:
    data/raw/prices.csv          ticker,datum,cena (dolgi format)
    data/raw/missing_tickers.txt tickerji, za katere ni podatkov
"""

import argparse
import csv
import logging
import sys
import time
from datetime import date
from pathlib import Path

import pandas as pd
import yfinance as yf

ROOT = Path(__file__).resolve().parent.parent
TICKERS_CSV = ROOT / "data" / "tickers.csv"
RAW_DIR = ROOT / "data" / "raw"
OUT_CSV = RAW_DIR / "prices.csv"
MISSING_TXT = RAW_DIR / "missing_tickers.txt"

BATCH_SIZE = 25      # koliko tickerjev naenkrat (manj = manj možnosti za rate limit)
PAUSE_SEC = 2        # premor med batchi

logging.basicConfig(level=logging.INFO, format="%(asctime)s %(levelname)s %(message)s")
log = logging.getLogger("fetch_prices")


def read_tickers() -> list[str]:
    with TICKERS_CSV.open(encoding="utf-8") as f:
        return [row["ticker"].strip() for row in csv.DictReader(f) if row["ticker"].strip()]


def fetch_batch(tickers: list[str], start: str, end: str) -> pd.DataFrame:
    """Vrne DataFrame v dolgem formatu: ticker, datum, cena."""
    # auto_adjust=True -> stolpec Close je že prilagojen za splite in dividende
    df = yf.download(
        tickers,
        start=start,
        end=end,
        interval="1d",
        auto_adjust=True,
        group_by="ticker",
        progress=False,
        threads=True,
    )
    if df.empty:
        return pd.DataFrame(columns=["ticker", "datum", "cena"])

    frames = []
    for t in tickers:
        try:
            close = df[t]["Close"] if isinstance(df.columns, pd.MultiIndex) else df["Close"]
        except KeyError:
            continue
        close = close.dropna()
        if close.empty:
            continue
        frames.append(pd.DataFrame({
            "ticker": t,
            "datum": close.index.date,
            "cena": close.values.round(4),
        }))
    return pd.concat(frames, ignore_index=True) if frames else pd.DataFrame(columns=["ticker", "datum", "cena"])


def main() -> int:
    parser = argparse.ArgumentParser(description="Prenos zgodovinskih cen")
    parser.add_argument("--start", default=f"{date.today().year - 15}-01-01")
    parser.add_argument("--end", default=date.today().isoformat())
    args = parser.parse_args()

    tickers = read_tickers()
    log.info("Tickerjev: %d, obdobje %s - %s", len(tickers), args.start, args.end)
    RAW_DIR.mkdir(parents=True, exist_ok=True)

    results = []
    for i in range(0, len(tickers), BATCH_SIZE):
        batch = tickers[i:i + BATCH_SIZE]
        log.info("Batch %d/%d: %s ...", i // BATCH_SIZE + 1,
                 (len(tickers) + BATCH_SIZE - 1) // BATCH_SIZE, ", ".join(batch[:3]))
        try:
            results.append(fetch_batch(batch, args.start, args.end))
        except Exception as e:  # napaka pri batchu ne ustavi celega prenosa
            log.warning("Batch ni uspel (%s), nadaljujem", e)
        time.sleep(PAUSE_SEC)

    prices = pd.concat(results, ignore_index=True) if results else pd.DataFrame(columns=["ticker", "datum", "cena"])
    prices.sort_values(["ticker", "datum"]).to_csv(OUT_CSV, index=False)

    found = set(prices["ticker"].unique())
    missing = [t for t in tickers if t not in found]
    MISSING_TXT.write_text("\n".join(missing) + ("\n" if missing else ""), encoding="utf-8")

    log.info("Shranjeno %d vrstic za %d tickerjev v %s", len(prices), len(found), OUT_CSV.relative_to(ROOT))
    if missing:
        log.warning("Brez podatkov (%d): %s", len(missing), ", ".join(missing))
    return 0


if __name__ == "__main__":
    sys.exit(main())
