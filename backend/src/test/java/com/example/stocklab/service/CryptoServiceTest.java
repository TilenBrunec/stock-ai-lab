package com.example.stocklab.service;

import org.junit.jupiter.api.Test;

import java.util.Base64;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class CryptoServiceTest {

    static final String KLJUC = Base64.getEncoder().encodeToString(new byte[32]);   // 32 ničel, samo za test
    final CryptoService crypto = new CryptoService(KLJUC);

    @Test
    void encryptDecrypt_vrneIzvirnik() {
        String original = "sk-or-v1-testni-kljuc-1234";
        assertThat(crypto.decrypt(crypto.encrypt(original))).isEqualTo(original);
    }

    @Test
    void dvakratnoSifriranje_daRazlicenRezultat() {
        String a = crypto.encrypt("isti niz");
        String b = crypto.encrypt("isti niz");
        assertThat(a).isNotEqualTo(b);           // nov naključen IV vsakič
    }

    @Test
    void sifriranoNeVsebujeIzvirnika() {
        assertThat(crypto.encrypt("sk-or-v1-skrivnost")).doesNotContain("skrivnost");
    }

    @Test
    void drugKljuc_neMoreDesifrirati() {
        byte[] drugi = new byte[32];
        drugi[0] = 1;
        CryptoService drugCrypto = new CryptoService(Base64.getEncoder().encodeToString(drugi));

        String sifrirano = crypto.encrypt("skrivnost");
        assertThatThrownBy(() -> drugCrypto.decrypt(sifrirano)).isInstanceOf(IllegalStateException.class);
    }

    @Test
    void manjkajociKljuc_vrzeNapako() {
        assertThatThrownBy(() -> new CryptoService("")).isInstanceOf(IllegalStateException.class);
    }

    @Test
    void napacnaDolzinaKljuca_vrzeNapako() {
        String kratek = Base64.getEncoder().encodeToString(new byte[16]);
        assertThatThrownBy(() -> new CryptoService(kratek)).isInstanceOf(IllegalStateException.class);
    }
}
