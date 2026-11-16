-- #46: šifriran OpenRouter ključ uporabnika (base64 IV + ciphertext) in zadnji 4 znaki za prikaz
ALTER TABLE uporabnik
    ADD COLUMN openrouter_kljuc_enc     VARCHAR(1000),
    ADD COLUMN openrouter_kljuc_zadnji4 VARCHAR(4);
