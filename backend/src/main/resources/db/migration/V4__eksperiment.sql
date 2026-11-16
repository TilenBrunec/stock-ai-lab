-- #52: modeli, eksperimenti, zagoni (en na model) in odločitve po instrumentih

CREATE TABLE llm_model (
    id             SERIAL        PRIMARY KEY,
    openrouter_id  VARCHAR(200)  NOT NULL UNIQUE,     -- npr. google/gemma-4-31b-it:free
    ime            VARCHAR(100)  NOT NULL,
    placljiv       BOOLEAN       NOT NULL,
    aktiven        BOOLEAN       NOT NULL DEFAULT TRUE
);

CREATE TABLE eksperiment (
    id                 BIGSERIAL      PRIMARY KEY,
    uporabnik_id       UUID           NOT NULL REFERENCES uporabnik(id),
    odlocitveni_datum  DATE           NOT NULL,
    leta_zgodovine     INTEGER        NOT NULL CHECK (leta_zgodovine BETWEEN 1 AND 5),
    konec_vrednotenja  DATE           NOT NULL,
    kapital            NUMERIC(14,2)  NOT NULL CHECK (kapital > 0),
    status             VARCHAR(20)    NOT NULL DEFAULT 'CAKA'
                       CHECK (status IN ('CAKA', 'V_TEKU', 'KONCANO', 'NEUSPESNO')),
    ustvarjen          TIMESTAMPTZ    NOT NULL DEFAULT now(),
    CHECK (konec_vrednotenja > odlocitveni_datum)
);
CREATE INDEX idx_eksperiment_uporabnik ON eksperiment (uporabnik_id);

CREATE TABLE eksperiment_instrument (
    eksperiment_id  BIGINT  NOT NULL REFERENCES eksperiment(id) ON DELETE CASCADE,
    stock_id        BIGINT  NOT NULL REFERENCES stock(id),
    PRIMARY KEY (eksperiment_id, stock_id)
);

CREATE TABLE zagon (
    id               BIGSERIAL      PRIMARY KEY,
    eksperiment_id   BIGINT         NOT NULL REFERENCES eksperiment(id) ON DELETE CASCADE,
    model_id         INTEGER        NOT NULL REFERENCES llm_model(id),
    status           VARCHAR(20)    NOT NULL DEFAULT 'CAKA'
                     CHECK (status IN ('CAKA', 'V_TEKU', 'KONCANO', 'NEUSPESNO')),
    napaka           VARCHAR(1000),
    koncna_vrednost  NUMERIC(14,2),
    zasluzek         NUMERIC(14,2),
    donos            NUMERIC(10,2),     -- v %
    cagr             NUMERIC(10,2),     -- v %
    delez_pravilnih  NUMERIC(5,2),      -- v %
    UNIQUE (eksperiment_id, model_id)
);

CREATE TABLE odlocitev (
    zagon_id           BIGINT          NOT NULL REFERENCES zagon(id) ON DELETE CASCADE,
    stock_id           BIGINT          NOT NULL REFERENCES stock(id),
    odlocitev          VARCHAR(10)     NOT NULL CHECK (odlocitev IN ('KUPI', 'NE_KUPI')),
    utemeljitev        VARCHAR(4000)   NOT NULL,
    donos_instrumenta  NUMERIC(10,2),  -- v %, izračuna se pri vrednotenju
    pravilna           BOOLEAN,
    PRIMARY KEY (zagon_id, stock_id)
);

ALTER TABLE llm_model              ENABLE ROW LEVEL SECURITY;
ALTER TABLE eksperiment            ENABLE ROW LEVEL SECURITY;
ALTER TABLE eksperiment_instrument ENABLE ROW LEVEL SECURITY;
ALTER TABLE zagon                  ENABLE ROW LEVEL SECURITY;
ALTER TABLE odlocitev              ENABLE ROW LEVEL SECURITY;
