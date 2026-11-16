package com.example.stocklab.service;

import com.example.stocklab.dto.CreateExperimentRequest;
import com.example.stocklab.dto.DateRangeDto;
import com.example.stocklab.service.ExperimentValidator.InstrumentInfo;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.LongStream;

import static org.assertj.core.api.Assertions.assertThat;

class ExperimentValidatorTest {

    static final DateRangeDto RAZPON = new DateRangeDto(LocalDate.of(2011, 1, 3), LocalDate.of(2026, 11, 5));
    static final Map<Long, InstrumentInfo> INSTRUMENTI = Map.of(
            1L, new InstrumentInfo("AAPL", LocalDate.of(2011, 1, 3)),
            2L, new InstrumentInfo("MSFT", LocalDate.of(2011, 1, 3)),
            3L, new InstrumentInfo("UBER", LocalDate.of(2019, 5, 10)));
    static final Set<Integer> MODELI = Set.of(1, 2);

    static CreateExperimentRequest veljaven() {
        return new CreateExperimentRequest(
                List.of(1L, 2L),
                LocalDate.of(2018, 1, 2),
                3,
                LocalDate.of(2023, 1, 2),
                new BigDecimal("10000"),
                List.of(1));
    }

    static Map<String, String> validate(CreateExperimentRequest r) {
        return ExperimentValidator.validate(r, RAZPON, INSTRUMENTI, MODELI, true);
    }

    @Test
    void veljavenZahtevek_nimaNapak() {
        assertThat(validate(veljaven())).isEmpty();
    }

    @Test
    void brezKljuca() {
        assertThat(ExperimentValidator.validate(veljaven(), RAZPON, INSTRUMENTI, MODELI, false))
                .containsKey("kljuc");
    }

    @Test
    void brezInstrumentov() {
        var r = new CreateExperimentRequest(List.of(), LocalDate.of(2018, 1, 2), 3,
                LocalDate.of(2023, 1, 2), BigDecimal.TEN, List.of(1));
        assertThat(validate(r)).containsKey("instrumentIds");
    }

    @Test
    void prevecInstrumentov() {
        List<Long> ids = LongStream.rangeClosed(1, 21).boxed().toList();
        var r = new CreateExperimentRequest(ids, LocalDate.of(2018, 1, 2), 3,
                LocalDate.of(2023, 1, 2), BigDecimal.TEN, List.of(1));
        assertThat(validate(r).get("instrumentIds")).contains("največ 20");
    }

    @Test
    void neznanInstrument() {
        var r = new CreateExperimentRequest(List.of(1L, 999L), LocalDate.of(2018, 1, 2), 3,
                LocalDate.of(2023, 1, 2), BigDecimal.TEN, List.of(1));
        assertThat(validate(r).get("instrumentIds")).contains("999");
    }

    @Test
    void instrumentBrezDovoljZgodovine() {
        var r = new CreateExperimentRequest(List.of(1L, 3L), LocalDate.of(2020, 1, 2), 3,
                LocalDate.of(2023, 1, 2), BigDecimal.TEN, List.of(1));
        assertThat(validate(r).get("instrumentIds")).contains("UBER").doesNotContain("AAPL");
    }

    @Test
    void letaZgodovineIzvenObmocja() {
        var r0 = new CreateExperimentRequest(List.of(1L), LocalDate.of(2018, 1, 2), 0,
                LocalDate.of(2023, 1, 2), BigDecimal.TEN, List.of(1));
        var r6 = new CreateExperimentRequest(List.of(1L), LocalDate.of(2018, 1, 2), 6,
                LocalDate.of(2023, 1, 2), BigDecimal.TEN, List.of(1));
        assertThat(validate(r0)).containsKey("letaZgodovine");
        assertThat(validate(r6)).containsKey("letaZgodovine");
    }

    @Test
    void kapitalNicAliNegativen() {
        var r = new CreateExperimentRequest(List.of(1L), LocalDate.of(2018, 1, 2), 3,
                LocalDate.of(2023, 1, 2), BigDecimal.ZERO, List.of(1));
        assertThat(validate(r)).containsKey("kapital");
    }

    @Test
    void odlocitveniDatumPrezgodaj() {
        // 3 leta zgodovine pred 2012-06-01 bi segala pred začetek podatkov (2011-01-03)
        var r = new CreateExperimentRequest(List.of(1L), LocalDate.of(2012, 6, 1), 3,
                LocalDate.of(2015, 1, 2), BigDecimal.TEN, List.of(1));
        assertThat(validate(r).get("odlocitveniDatum")).contains("2014-01-03");
    }

    @Test
    void odlocitveniDatumPrepozno() {
        var r = new CreateExperimentRequest(List.of(1L), LocalDate.of(2026, 6, 1), 3,
                LocalDate.of(2026, 11, 5), BigDecimal.TEN, List.of(1));
        assertThat(validate(r)).containsKey("odlocitveniDatum");
    }

    @Test
    void konecManjKotLetoPoOdlocitvi() {
        var r = new CreateExperimentRequest(List.of(1L), LocalDate.of(2018, 1, 2), 3,
                LocalDate.of(2018, 12, 31), BigDecimal.TEN, List.of(1));
        assertThat(validate(r)).containsKey("konecVrednotenja");
    }

    @Test
    void konecPoZadnjihPodatkih() {
        var r = new CreateExperimentRequest(List.of(1L), LocalDate.of(2018, 1, 2), 3,
                LocalDate.of(2027, 1, 1), BigDecimal.TEN, List.of(1));
        assertThat(validate(r).get("konecVrednotenja")).contains("2026-11-05");
    }

    @Test
    void brezModela() {
        var r = new CreateExperimentRequest(List.of(1L), LocalDate.of(2018, 1, 2), 3,
                LocalDate.of(2023, 1, 2), BigDecimal.TEN, List.of());
        assertThat(validate(r)).containsKey("modelIds");
    }

    @Test
    void neaktivenModel() {
        var r = new CreateExperimentRequest(List.of(1L), LocalDate.of(2018, 1, 2), 3,
                LocalDate.of(2023, 1, 2), BigDecimal.TEN, List.of(1, 7));
        assertThat(validate(r)).containsKey("modelIds");
    }

    @Test
    void manjkajociPodatki_neVrzejoIzjeme() {
        var r = new CreateExperimentRequest(null, null, null, null, null, null);
        assertThat(validate(r)).containsKeys(
                "instrumentIds", "letaZgodovine", "kapital", "odlocitveniDatum", "konecVrednotenja", "modelIds");
    }
}
