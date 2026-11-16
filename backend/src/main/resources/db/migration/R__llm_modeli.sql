-- Seznam modelov (Flyway "repeatable" migracija: ob vsaki spremembi te datoteke se ponovno izvede).
-- Za nov model ali spremembo dodaj/uredi vrstico in ponovno zaženi backend.
-- Pravi ID-ji: https://openrouter.ai/models (stran modela -> gumb za kopiranje ID-ja)
--
-- Brezplačni modeli (:free) se na OpenRouterju pogosto menjajo. Plačljiva modela sta
-- dodana kot neaktivna: preveri ID na openrouter.ai, popravi ga tukaj in nastavi aktiven na TRUE.

INSERT INTO llm_model (openrouter_id, ime, placljiv, aktiven) VALUES
    ('google/gemma-4-31b-it:free',             'Gemma 4 31B (brezplačen)',       FALSE, TRUE),
    ('nvidia/nemotron-3-ultra-550b-a55b:free', 'Nemotron 3 Ultra (brezplačen)', FALSE, TRUE),
    ('anthropic/claude-haiku-5.5',             'Claude Haiku',                   TRUE,  FALSE),
    ('openai/gpt-5-mini',                      'GPT mini',                       TRUE,  FALSE)
ON CONFLICT (openrouter_id) DO UPDATE
    SET ime      = EXCLUDED.ime,
        placljiv = EXCLUDED.placljiv,
        aktiven  = EXCLUDED.aktiven;
