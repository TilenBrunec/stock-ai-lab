CREATE TABLE stock (
                       id           BIGSERIAL PRIMARY KEY,
                       ticker       VARCHAR(16)  NOT NULL UNIQUE,
                       ime          VARCHAR(200) NOT NULL,
                       tip          VARCHAR(10)  NOT NULL CHECK (tip IN ('DELNICA', 'SKLAD')),
                       anon_oznaka  VARCHAR(8)   UNIQUE
);

CREATE TABLE stock_price (
                             stock_id  BIGINT        NOT NULL REFERENCES stock(id) ON DELETE CASCADE,
                             datum     DATE          NOT NULL,
                             cena      NUMERIC(14,4) NOT NULL CHECK (cena > 0),
                             PRIMARY KEY (stock_id, datum)
);
ALTER TABLE stock        ENABLE ROW LEVEL SECURITY;
ALTER TABLE stock_price  ENABLE ROW LEVEL SECURITY;