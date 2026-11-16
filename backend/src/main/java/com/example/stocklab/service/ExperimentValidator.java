package com.example.stocklab.service;

import com.example.stocklab.dto.CreateExperimentRequest;
import com.example.stocklab.dto.DateRangeDto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

/**
 * Pravila za nov eksperiment (US-03, US-04). Čista funkcija brez baze, zato je enostavna za testiranje.
 * Vrne napake po poljih; prazen map pomeni veljaven zahtevek.
 */
public final class ExperimentValidator {

    public static final int MAX_INSTRUMENTOV = 20;
    public static final int MIN_LET_ZGODOVINE = 1;
    public static final int MAX_LET_ZGODOVINE = 5;
    public static final BigDecimal MAX_KAPITAL = new BigDecimal("1000000000");

    /** Podatki o obstoječem instrumentu: ticker in prvi datum s ceno (null, če cen ni). */
    public record InstrumentInfo(String ticker, LocalDate prviDatum) {}

    private ExperimentValidator() {}

    public static Map<String, String> validate(CreateExperimentRequest r,
                                               DateRangeDto razpon,
                                               Map<Long, InstrumentInfo> instrumenti,
                                               Set<Integer> aktivniModeli,
                                               boolean imaKljuc) {
        Map<String, String> napake = new LinkedHashMap<>();

        if (!imaKljuc) {
            napake.put("kljuc", "Najprej v nastavitvah shrani OpenRouter ključ");
        }

        // --- instrumenti ---
        Set<Long> ids = new LinkedHashSet<>(r.instrumentIds() == null ? List.of() : r.instrumentIds());
        ids.remove(null);
        if (ids.isEmpty()) {
            napake.put("instrumentIds", "Izberi vsaj en instrument");
        } else if (ids.size() > MAX_INSTRUMENTOV) {
            napake.put("instrumentIds", "Izbereš lahko največ " + MAX_INSTRUMENTOV + " instrumentov");
        } else {
            List<Long> neznani = ids.stream().filter(id -> !instrumenti.containsKey(id)).toList();
            if (!neznani.isEmpty()) {
                napake.put("instrumentIds", "Neznani instrumenti: " + neznani);
            }
        }

        // --- leta zgodovine ---
        Integer leta = r.letaZgodovine();
        boolean letaOk = leta != null && leta >= MIN_LET_ZGODOVINE && leta <= MAX_LET_ZGODOVINE;
        if (!letaOk) {
            napake.put("letaZgodovine",
                    "Leta zgodovine morajo biti med " + MIN_LET_ZGODOVINE + " in " + MAX_LET_ZGODOVINE);
        }

        // --- kapital ---
        if (r.kapital() == null || r.kapital().signum() <= 0) {
            napake.put("kapital", "Kapital mora biti večji od 0 €");
        } else if (r.kapital().compareTo(MAX_KAPITAL) > 0) {
            napake.put("kapital", "Kapital je prevelik");
        }

        // --- odločitveni datum ---
        LocalDate odl = r.odlocitveniDatum();
        LocalDate zadnjiOdl = razpon.konec().minusYears(1);
        if (odl == null) {
            napake.put("odlocitveniDatum", "Vnesi odločitveni datum");
        } else if (letaOk && odl.minusYears(leta).isBefore(razpon.zacetek())) {
            napake.put("odlocitveniDatum", "Pri " + leta + " letih zgodovine je najzgodnejši odločitveni datum "
                    + razpon.zacetek().plusYears(leta));
        } else if (odl.isAfter(zadnjiOdl)) {
            napake.put("odlocitveniDatum", "Najpoznejši odločitveni datum je " + zadnjiOdl
                    + " (za vrednotenje mora ostati vsaj 1 leto podatkov)");
        }

        // vsak instrument mora imeti cene za celotno obdobje zgodovine
        if (odl != null && letaOk && !napake.containsKey("instrumentIds")) {
            LocalDate zacetekZgodovine = odl.minusYears(leta);
            List<String> premalo = ids.stream()
                    .map(instrumenti::get)
                    .filter(Objects::nonNull)
                    .filter(i -> i.prviDatum() == null || i.prviDatum().isAfter(zacetekZgodovine))
                    .map(InstrumentInfo::ticker)
                    .toList();
            if (!premalo.isEmpty()) {
                napake.put("instrumentIds", "Premalo zgodovine pred odločitvenim datumom (od "
                        + zacetekZgodovine + "): " + String.join(", ", premalo));
            }
        }

        // --- konec vrednotenja ---
        LocalDate konec = r.konecVrednotenja();
        if (konec == null) {
            napake.put("konecVrednotenja", "Vnesi konec vrednotenja");
        } else if (odl != null && konec.isBefore(odl.plusYears(1))) {
            napake.put("konecVrednotenja", "Konec vrednotenja mora biti vsaj 1 leto po odločitvenem datumu");
        } else if (konec.isAfter(razpon.konec())) {
            napake.put("konecVrednotenja", "Najpoznejši konec vrednotenja je " + razpon.konec());
        }

        // --- modeli ---
        Set<Integer> modeli = new LinkedHashSet<>(r.modelIds() == null ? List.of() : r.modelIds());
        modeli.remove(null);
        if (modeli.isEmpty()) {
            napake.put("modelIds", "Izberi vsaj en model");
        } else if (!aktivniModeli.containsAll(modeli)) {
            napake.put("modelIds", "Izbran je neznan ali neaktiven model");
        }

        return napake;
    }
}
