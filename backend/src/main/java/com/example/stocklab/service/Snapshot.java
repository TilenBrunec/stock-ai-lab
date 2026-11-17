package com.example.stocklab.service;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Zaprt posnetek podatkov za model (#60): samo cene do odločitvenega datuma, samo anonimne oznake.
 * Datumi in stockId so tu za interno uporabo in teste; v prompt gredo samo oznake in vrednosti.
 */
public record Snapshot(int letaZgodovine, long mesecevVrednotenja, List<Instrument> instrumenti) {

    /** Tedenska točka: datum (zadnji trgovalni dan v tednu) in vrednost, normalizirana na 100. */
    public record Tocka(LocalDate datum, double vrednost) {}

    public record Instrument(
            Long stockId,
            String oznaka,
            List<Tocka> tocke,
            double donos,            // % v obdobju zgodovine
            double letnaNihajnost,   // %
            double najvecjiPadec) {} // %, negativno število

    public Set<String> oznake() {
        Set<String> s = new LinkedHashSet<>();
        instrumenti.forEach(i -> s.add(i.oznaka()));
        return s;
    }

    public Map<String, Long> stockIdPoOznaki() {
        Map<String, Long> m = new LinkedHashMap<>();
        instrumenti.forEach(i -> m.put(i.oznaka(), i.stockId()));
        return m;
    }

    /** Najpoznejši datum v posnetku (test: ne sme biti po odločitvenem datumu). */
    public LocalDate zadnjiDatum() {
        return instrumenti.stream()
                .flatMap(i -> i.tocke().stream())
                .map(Tocka::datum)
                .max(Comparator.naturalOrder())
                .orElse(null);
    }
}
