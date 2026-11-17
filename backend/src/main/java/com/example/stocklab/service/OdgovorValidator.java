package com.example.stocklab.service;

import com.example.stocklab.model.OdlocitevTip;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

/**
 * #63: preveri odgovor modela. Vsak instrument iz posnetka mora imeti natanko eno odločitev
 * KUPI/NE_KUPI in neprazno utemeljitev, neznanih oznak ne sme biti.
 */
public final class OdgovorValidator {

    /** Veljavna odločitev za en instrument. */
    public record Sklep(OdlocitevTip tip, String utemeljitev) {}

    private OdgovorValidator() {}

    public static Map<String, Sklep> validiraj(OdgovorModela odgovor, Set<String> oznake) {
        if (odgovor == null || odgovor.odlocitve() == null || odgovor.odlocitve().isEmpty()) {
            throw new NeveljavenOdgovorException("Odgovor nima seznama \"odlocitve\"");
        }

        Map<String, Sklep> sklepi = new LinkedHashMap<>();
        List<String> neznane = new ArrayList<>();

        for (OdgovorModela.Postavka p : odgovor.odlocitve()) {
            if (p == null || p.oznaka() == null) {
                throw new NeveljavenOdgovorException("Odločitev brez oznake");
            }
            String oznaka = p.oznaka().trim().toUpperCase(Locale.ROOT);
            if (!oznake.contains(oznaka)) {
                neznane.add(oznaka);
                continue;
            }
            if (sklepi.containsKey(oznaka)) {
                throw new NeveljavenOdgovorException("Instrument " + oznaka + " ima več odločitev");
            }
            OdlocitevTip tip = tip(p.odlocitev());
            if (tip == null) {
                throw new NeveljavenOdgovorException("Neveljavna odločitev za " + oznaka + ": " + p.odlocitev());
            }
            if (p.utemeljitev() == null || p.utemeljitev().isBlank()) {
                throw new NeveljavenOdgovorException("Manjka utemeljitev za " + oznaka);
            }
            sklepi.put(oznaka, new Sklep(tip, p.utemeljitev().trim()));
        }

        if (!neznane.isEmpty()) {
            throw new NeveljavenOdgovorException("Neznane oznake v odgovoru: " + neznane);
        }
        List<String> manjkajo = oznake.stream().filter(o -> !sklepi.containsKey(o)).toList();
        if (!manjkajo.isEmpty()) {
            throw new NeveljavenOdgovorException("Manjkajo odločitve za: " + manjkajo);
        }
        return sklepi;
    }

    /** Sprejme tudi pogoste različice zapisa ("ne kupi", "BUY", "NOT_BUY" ...). */
    static OdlocitevTip tip(String vrednost) {
        if (vrednost == null) return null;
        String v = vrednost.trim().toUpperCase(Locale.ROOT).replace(' ', '_').replace('-', '_');
        return switch (v) {
            case "KUPI", "BUY" -> OdlocitevTip.KUPI;
            case "NE_KUPI", "NEKUPI", "NOT_BUY", "DONT_BUY", "DO_NOT_BUY", "NO_BUY", "SKIP" -> OdlocitevTip.NE_KUPI;
            default -> null;
        };
    }
}
