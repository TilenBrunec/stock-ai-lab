package com.example.stocklab.dto;

import com.example.stocklab.model.Eksperiment;
import com.example.stocklab.model.Zagon;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;

/** Podrobnosti eksperimenta za avtorja. */
public record ExperimentDto(
        Long id,
        LocalDate odlocitveniDatum,
        int letaZgodovine,
        LocalDate konecVrednotenja,
        BigDecimal kapital,
        String status,
        Instant ustvarjen,
        List<StockDto> instrumenti,
        List<ZagonDto> zagoni) {

    public record ZagonDto(Long id, Integer modelId, String model, boolean placljiv, String status, String napaka) {

        static ZagonDto from(Zagon z) {
            return new ZagonDto(z.getId(), z.getModel().getId(), z.getModel().getIme(),
                    z.getModel().isPlacljiv(), z.getStatus().name(), z.getNapaka());
        }
    }

    public static ExperimentDto from(Eksperiment e) {
        return new ExperimentDto(
                e.getId(),
                e.getOdlocitveniDatum(),
                e.getLetaZgodovine(),
                e.getKonecVrednotenja(),
                e.getKapital(),
                e.getStatus().name(),
                e.getUstvarjen(),
                e.getInstrumenti().stream()
                        .map(StockDto::from)
                        .sorted(Comparator.comparing(StockDto::ticker))
                        .toList(),
                e.getZagoni().stream().map(ZagonDto::from).toList());
    }
}
