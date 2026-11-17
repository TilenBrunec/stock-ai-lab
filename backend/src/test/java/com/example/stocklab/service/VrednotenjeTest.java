package com.example.stocklab.service;

import com.example.stocklab.service.Vrednotenje.Postavka;
import com.example.stocklab.service.Vrednotenje.Rezultat;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;

/** #66: izračun se ujema z ročnim izračunom. */
class VrednotenjeTest {

    static final BigDecimal KAPITAL = new BigDecimal("1000");
    static final LocalDate OD = LocalDate.of(2020, 1, 1);
    static final LocalDate DO = LocalDate.of(2022, 1, 1);   // 2 leti

    static BigDecimal bd(String v) { return new BigDecimal(v); }

    @Test
    void enInstrument_rocniIzracun() {
        // 1000 € v instrument 100 -> 150: končna 1500, zaslužek 500, donos 50 %
        Rezultat r = Vrednotenje.izracunaj(KAPITAL, OD, DO,
                List.of(new Postavka(true, bd("100"), bd("150"))));

        assertThat(r.koncnaVrednost()).isEqualByComparingTo("1500.00");
        assertThat(r.zasluzek()).isEqualByComparingTo("500.00");
        assertThat(r.donos()).isEqualByComparingTo("50.00");
        // CAGR: 1.5^(1/2) - 1 = 22.47 % (2 leti = 731 dni / 365.25)
        assertThat(r.cagr().doubleValue()).isCloseTo(22.47, within(0.05));
        assertThat(r.delezPravilnih()).isEqualByComparingTo("100.00");
    }

    @Test
    void kapitalEnakomernoRazdeljenMedNakupe() {
        // 500 € v A (100 -> 200) = 1000, 500 € v B (100 -> 50) = 250, skupaj 1250
        Rezultat r = Vrednotenje.izracunaj(KAPITAL, OD, DO, List.of(
                new Postavka(true, bd("100"), bd("200")),
                new Postavka(true, bd("100"), bd("50"))));

        assertThat(r.koncnaVrednost()).isEqualByComparingTo("1250.00");
        assertThat(r.donos()).isEqualByComparingTo("25.00");
        assertThat(r.delezPravilnih()).isEqualByComparingTo("50.00");   // B je bil napačen nakup
    }

    @Test
    void brezNakupa_kapitalOstane() {
        Rezultat r = Vrednotenje.izracunaj(KAPITAL, OD, DO, List.of(
                new Postavka(false, bd("100"), bd("300"))));

        assertThat(r.koncnaVrednost()).isEqualByComparingTo("1000.00");
        assertThat(r.zasluzek()).isEqualByComparingTo("0.00");
        assertThat(r.cagr()).isEqualByComparingTo("0.00");
        assertThat(r.postavke().get(0).pravilna()).isFalse();          // zamudil je rast 200 %
    }

    @Test
    void neKupiPriPadcu_jePravilna() {
        Rezultat r = Vrednotenje.izracunaj(KAPITAL, OD, DO, List.of(
                new Postavka(true, bd("100"), bd("110")),
                new Postavka(false, bd("100"), bd("80"))));

        assertThat(r.postavke().get(0).donos()).isEqualByComparingTo("10.00");
        assertThat(r.postavke().get(1).donos()).isEqualByComparingTo("-20.00");
        assertThat(r.postavke()).allSatisfy(p -> assertThat(p.pravilna()).isTrue());
        assertThat(r.koncnaVrednost()).isEqualByComparingTo("1100.00");   // ves kapital v edini nakup
    }

    @Test
    void izguba_negativenZasluzek() {
        Rezultat r = Vrednotenje.izracunaj(KAPITAL, OD, DO, List.of(
                new Postavka(true, bd("100"), bd("60"))));

        assertThat(r.zasluzek()).isEqualByComparingTo("-400.00");
        assertThat(r.donos()).isEqualByComparingTo("-40.00");
        assertThat(r.cagr().signum()).isNegative();
    }
}
