package com.example.stocklab.dto;

import com.example.stocklab.model.Eksperiment;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

/** Vrstica v seznamu "moji eksperimenti". */
public record ExperimentSummaryDto(
        Long id,
        LocalDate odlocitveniDatum,
        LocalDate konecVrednotenja,
        BigDecimal kapital,
        String status,
        int steviloInstrumentov,
        List<String> modeli,
        Instant ustvarjen) {

    public static ExperimentSummaryDto from(Eksperiment e) {
        return new ExperimentSummaryDto(
                e.getId(),
                e.getOdlocitveniDatum(),
                e.getKonecVrednotenja(),
                e.getKapital(),
                e.getStatus().name(),
                e.getInstrumenti().size(),
                e.getZagoni().stream().map(z -> z.getModel().getIme()).toList(),
                e.getUstvarjen());
    }
}
