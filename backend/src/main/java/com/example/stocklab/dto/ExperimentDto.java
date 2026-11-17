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

    /** Zagon enega modela; metrike so null, dokler zagon ni končan. */
    public record ZagonDto(
            Long id,
            Integer modelId,
            String model,
            boolean placljiv,
            String status,
            String napaka,
            BigDecimal koncnaVrednost,
            BigDecimal zasluzek,
            BigDecimal donos,
            BigDecimal cagr,
            BigDecimal delezPravilnih) {

        static ZagonDto from(Zagon z) {
            return new ZagonDto(z.getId(), z.getModel().getId(), z.getModel().getIme(),
                    z.getModel().isPlacljiv(), z.getStatus().name(), z.getNapaka(),
                    z.getKoncnaVrednost(), z.getZasluzek(), z.getDonos(), z.getCagr(), z.getDelezPravilnih());
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
