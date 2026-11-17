package com.example.stocklab.service;

import com.example.stocklab.model.OdlocitevTip;
import com.example.stocklab.service.OdgovorModela.Postavka;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class OdgovorValidatorTest {

    static final Set<String> OZNAKE = Set.of("S001", "S002");

    static OdgovorModela odgovor(Postavka... p) {
        return new OdgovorModela(List.of(p));
    }

    @Test
    void veljavenOdgovor() {
        var sklepi = OdgovorValidator.validiraj(odgovor(
                new Postavka("S001", "KUPI", "Stabilna rast."),
                new Postavka("S002", "NE_KUPI", "Velik padec.")), OZNAKE);

        assertThat(sklepi).hasSize(2);
        assertThat(sklepi.get("S001").tip()).isEqualTo(OdlocitevTip.KUPI);
        assertThat(sklepi.get("S002").tip()).isEqualTo(OdlocitevTip.NE_KUPI);
    }

    @Test
    void sprejmeRazlicneZapise() {
        var sklepi = OdgovorValidator.validiraj(odgovor(
                new Postavka(" s001 ", "buy", "a"),
                new Postavka("S002", "ne kupi", "b")), OZNAKE);

        assertThat(sklepi.get("S001").tip()).isEqualTo(OdlocitevTip.KUPI);
        assertThat(sklepi.get("S002").tip()).isEqualTo(OdlocitevTip.NE_KUPI);
    }

    @Test
    void manjkajociInstrument_zavrnjen() {
        assertThatThrownBy(() -> OdgovorValidator.validiraj(odgovor(
                new Postavka("S001", "KUPI", "a")), OZNAKE))
                .isInstanceOf(NeveljavenOdgovorException.class)
                .hasMessageContaining("S002");
    }

    @Test
    void neznanaOznaka_zavrnjena() {
        assertThatThrownBy(() -> OdgovorValidator.validiraj(odgovor(
                new Postavka("S001", "KUPI", "a"),
                new Postavka("S002", "KUPI", "b"),
                new Postavka("S999", "KUPI", "c")), OZNAKE))
                .isInstanceOf(NeveljavenOdgovorException.class)
                .hasMessageContaining("S999");
    }

    @Test
    void dvojnaOdlocitev_zavrnjena() {
        assertThatThrownBy(() -> OdgovorValidator.validiraj(odgovor(
                new Postavka("S001", "KUPI", "a"),
                new Postavka("S001", "NE_KUPI", "b"),
                new Postavka("S002", "KUPI", "c")), OZNAKE))
                .isInstanceOf(NeveljavenOdgovorException.class);
    }

    @Test
    void neveljavnaOdlocitev_zavrnjena() {
        assertThatThrownBy(() -> OdgovorValidator.validiraj(odgovor(
                new Postavka("S001", "MOGOCE", "a"),
                new Postavka("S002", "KUPI", "b")), OZNAKE))
                .isInstanceOf(NeveljavenOdgovorException.class);
    }

    @Test
    void praznaUtemeljitev_zavrnjena() {
        assertThatThrownBy(() -> OdgovorValidator.validiraj(odgovor(
                new Postavka("S001", "KUPI", "  "),
                new Postavka("S002", "KUPI", "b")), OZNAKE))
                .isInstanceOf(NeveljavenOdgovorException.class);
    }

    @Test
    void prazenOdgovor_zavrnjen() {
        assertThatThrownBy(() -> OdgovorValidator.validiraj(new OdgovorModela(null), OZNAKE))
                .isInstanceOf(NeveljavenOdgovorException.class);
    }
}
