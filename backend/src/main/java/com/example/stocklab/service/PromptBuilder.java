package com.example.stocklab.service;

import java.util.Locale;
import java.util.stream.Collectors;

/**
 * #62: navodila in podatki za model. V prompt gredo samo anonimne oznake in normalizirane
 * vrednosti, brez tickerjev, imen in koledarskih datumov.
 */
public final class PromptBuilder {

    private PromptBuilder() {}

    public static String sistemsko() {
        return """
                You are an investment analyst taking part in a historical backtesting experiment.
                You receive anonymized weekly price histories of stocks and index funds up to a decision date.
                Company names, tickers and calendar dates are hidden on purpose. Do not try to identify the
                instruments and do not use knowledge of real-world events.

                For EACH instrument decide whether to buy it now and hold it until the end of the evaluation period:
                "KUPI" (buy) or "NE_KUPI" (do not buy).
                The capital is split equally among all instruments marked KUPI. If you mark none, the capital stays in cash.
                Base every decision only on the provided data (trend, volatility, drawdowns, recent momentum).

                Respond with ONLY a JSON object and no other text, in exactly this format:
                {"odlocitve":[{"oznaka":"S001","odlocitev":"KUPI","utemeljitev":"..."}]}
                Include every instrument exactly once. Write each "utemeljitev" in Slovenian, 1 to 3 sentences.
                """;
    }

    public static String uporabnisko(Snapshot s) {
        StringBuilder sb = new StringBuilder();
        sb.append("Zgodovina: ").append(s.letaZgodovine()).append(" let tedenskih vrednosti do odločitve ")
          .append("(indeks 100 = prvi teden, zadnja vrednost = teden odločitve).\n");
        sb.append("Obdobje vrednotenja po odločitvi: ").append(s.mesecevVrednotenja()).append(" mesecev.\n");
        sb.append("Število instrumentov: ").append(s.instrumenti().size()).append("\n\n");

        for (Snapshot.Instrument i : s.instrumenti()) {
            sb.append(i.oznaka())
              .append(" | donos ").append(fmt(i.donos())).append("%")
              .append(" | letna nihajnost ").append(fmt(i.letnaNihajnost())).append("%")
              .append(" | največji padec ").append(fmt(i.najvecjiPadec())).append("%\n");
            sb.append("  ")
              .append(i.tocke().stream().map(t -> fmt(t.vrednost())).collect(Collectors.joining(" ")))
              .append("\n");
        }
        return sb.toString();
    }

    private static String fmt(double v) {
        return String.format(Locale.ROOT, "%.1f", v);
    }
}
