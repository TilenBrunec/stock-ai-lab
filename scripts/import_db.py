"""
Uvozi instrumente in očiščene cene v Supabase (Postgres).

  data/tickers.csv        -> tabela stock        (ticker, ime, tip)
  data/clean/prices.csv   -> tabela stock_price  (stock_id, datum, cena)

Idempotentno: ponoven zagon ne ustvari dvojnikov (ON CONFLICT DO NOTHING).
Instrumenti brez cen (npr. delistani) se preskočijo.

Povezava se prebere iz okoljske spremenljivke DATABASE_URL, npr.:
    postgresql://postgres.<ref>:<geslo>@aws-0-eu-central-1.pooler.supabase.com:5432/postgres?sslmode=require

Uporaba (iz root mape):
    v .env (root, ni v gitu) zapiši: DATABASE_URL=postgresql://...
    python scripts/import_db.py
"""

import csv
import logging
import os
import sys
import time
from pathlib import Path

import psycopg

ROOT = Path(__file__).resolve().parent.parent
TICKERS_CSV = ROOT / "data" / "tickers.csv"
PRICES_CSV = ROOT / "data" / "clean" / "prices.csv"

logging.basicConfig(level=logging.INFO, format="%(asctime)s %(levelname)s %(message)s")
log = logging.getLogger("import_db")


def load_env_file() -> None:
    """Prebere .env iz root mape (KLJUC=vrednost), če obstaja. Že nastavljene spremenljivke imajo prednost."""
    env = ROOT / ".env"
    if not env.exists():
        return
    for line in env.read_text(encoding="utf-8").splitlines():
        line = line.strip()
        if not line or line.startswith("#") or "=" not in line:
            continue
        key, value = line.split("=", 1)
        os.environ.setdefault(key.strip(), value.strip().strip('"').strip("'"))


def read_instruments() -> list[tuple[str, str, str]]:
    with TICKERS_CSV.open(encoding="utf-8") as f:
        return [(r["ticker"].strip(), r["ime"].strip(), r["tip"].strip()) for r in csv.DictReader(f)]


def tickers_with_prices() -> set[str]:
    with PRICES_CSV.open(encoding="utf-8") as f:
        return {row["ticker"] for row in csv.DictReader(f)}


def main() -> int:
    load_env_file()
    url = os.environ.get("DATABASE_URL")
    if not url:
        log.error("Nastavi DATABASE_URL v .env (root mapa) ali kot okoljsko spremenljivko")
        return 1
    if url.startswith("jdbc:"):
        log.error("DATABASE_URL ne sme imeti 'jdbc:' na začetku (to je format za Javo)")
        return 1
    if not PRICES_CSV.exists():
        log.error("Ni datoteke %s - najprej zaženi clean_data.py", PRICES_CSV.relative_to(ROOT))
        return 1

    t0 = time.time()
    instruments = read_instruments()
    have_prices = tickers_with_prices()
    to_insert = [i for i in instruments if i[0] in have_prices]
    skipped = [i[0] for i in instruments if i[0] not in have_prices]
    if skipped:
        log.info("Preskočeni (brez cen): %s", ", ".join(skipped))

    with psycopg.connect(url) as conn:
        with conn.cursor() as cur:
            # 1) instrumenti
            cur.executemany(
                "INSERT INTO stock (ticker, ime, tip) VALUES (%s, %s, %s) "
                "ON CONFLICT (ticker) DO NOTHING",
                to_insert,
            )
            cur.execute("SELECT count(*) FROM stock")
            log.info("Instrumentov v bazi: %d", cur.fetchone()[0])

            # 1b) anonimne oznake (#7): samo instrumenti brez oznake, v naključnem vrstnem redu,
            #     številčenje nadaljuje od največje obstoječe -> obstoječe oznake se ne spremenijo
            cur.execute("""
                WITH n AS (
                    SELECT coalesce(max(substring(anon_oznaka FROM 2)::int), 0) AS m
                    FROM stock WHERE anon_oznaka IS NOT NULL
                ), t AS (
                    SELECT id, row_number() OVER (ORDER BY random()) AS rn
                    FROM stock WHERE anon_oznaka IS NULL
                )
                UPDATE stock s
                SET anon_oznaka = 'S' || lpad((n.m + t.rn)::text, 3, '0')
                FROM t, n
                WHERE s.id = t.id
            """)
            log.info("Novih anonimnih oznak: %d", cur.rowcount)

            # 2) cene: COPY v začasno tabelo, nato en INSERT z ON CONFLICT
            cur.execute(
                "CREATE TEMP TABLE tmp_price (ticker VARCHAR(16), datum DATE, cena NUMERIC(14,4)) "
                "ON COMMIT DROP"
            )
            log.info("Nalagam cene v začasno tabelo ...")
            with PRICES_CSV.open(encoding="utf-8") as f, \
                    cur.copy("COPY tmp_price (ticker, datum, cena) FROM STDIN WITH (FORMAT csv, HEADER true)") as copy:
                while chunk := f.read(1 << 20):
                    copy.write(chunk)

            cur.execute("SELECT count(*) FROM tmp_price")
            log.info("Prebranih cen: %d", cur.fetchone()[0])

            cur.execute(
                "INSERT INTO stock_price (stock_id, datum, cena) "
                "SELECT s.id, t.datum, t.cena FROM tmp_price t JOIN stock s ON s.ticker = t.ticker "
                "ON CONFLICT (stock_id, datum) DO NOTHING"
            )
            inserted = cur.rowcount
            log.info("Novih cen vstavljenih: %d", inserted)

            cur.execute("SELECT count(*) FROM stock_price")
            total = cur.fetchone()[0]
            cur.execute("SELECT pg_size_pretty(pg_database_size(current_database()))")
            size = cur.fetchone()[0]
        conn.commit()

    log.info("Cen v bazi skupaj: %d | velikost baze: %s | čas: %.1f s", total, size, time.time() - t0)
    return 0


if __name__ == "__main__":
    sys.exit(main())
