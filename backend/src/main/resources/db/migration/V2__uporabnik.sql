CREATE TABLE uporabnik (
                           id         UUID          PRIMARY KEY,          -- = Supabase user id (claim "sub")
                           email      VARCHAR(320)  NOT NULL,
                           ime        VARCHAR(200),
                           slika_url  VARCHAR(1000),
                           vloga      VARCHAR(20)   NOT NULL DEFAULT 'RAZISKOVALEC'
                               CHECK (vloga IN ('RAZISKOVALEC', 'ADMINISTRATOR')),
                           ustvarjen  TIMESTAMPTZ   NOT NULL DEFAULT now()
);

ALTER TABLE uporabnik ENABLE ROW LEVEL SECURITY;