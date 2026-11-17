package com.example.stocklab.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

/**
 * #65: izračun uspešnosti odločitev. Čista funkcija brez baze (testi v VrednotenjeTest).
 * Kapital se enakomerno razdeli med instrumente z odločitvijo KUPI; brez nakupa ostane nespremenjen.
 */
public final class Vrednotenje {

    /** Vhod: odločitev in ceni na odločitveni dan ter na koncu vrednotenja (zadnji znani ceni). */
    public record Postavka(boolean kupi, BigDecimal cenaZacetek, BigDecimal cenaKonec) {}

    public record PostavkaRezultat(BigDecimal donos, boolean pravilna) {}

    public record Rezultat(
            BigDecimal koncnaVrednost,
            BigDecimal zasluzek,
            BigDecimal donos,            // %
            BigDecimal cagr,             // %
            BigDecimal delezPravilnih,   // %
            List<PostavkaRezultat> postavke) {}

    private Vrednotenje() {}

    public static Rezultat izracunaj(BigDecimal kapital, LocalDate od, LocalDate doDatuma, List<Postavka> postavke) {
        double k = kapital.doubleValue();
        long steviloNakupov = postavke.stream().filter(Postavka::kupi).count();
        double naInstrument = steviloNakupov == 0 ? 0 : k / steviloNakupov;

        double koncna = steviloNakupov == 0 ? k : 0;
        int pravilne = 0;
        List<PostavkaRezultat> rezultati = new ArrayList<>();

        for (Postavka p : postavke) {
            double rast = p.cenaKonec().doubleValue() / p.cenaZacetek().doubleValue();
            double donos = (rast - 1) * 100;
            boolean pravilna = p.kupi() ? donos > 0 : donos <= 0;
            if (pravilna) pravilne++;
            if (p.kupi()) koncna += naInstrument * rast;
            rezultati.add(new PostavkaRezultat(bd(donos), pravilna));
        }

        double donosSkupaj = (koncna / k - 1) * 100;
        double leta = ChronoUnit.DAYS.between(od, doDatuma) / 365.25;
        double cagr = leta > 0 ? (Math.pow(koncna / k, 1 / leta) - 1) * 100 : donosSkupaj;
        double delez = postavke.isEmpty() ? 0 : pravilne * 100.0 / postavke.size();

        return new Rezultat(bd(koncna), bd(koncna - k), bd(donosSkupaj), bd(cagr), bd(delez), rezultati);
    }

    private static BigDecimal bd(double v) {
        return BigDecimal.valueOf(v).setScale(2, RoundingMode.HALF_UP);
    }
}
