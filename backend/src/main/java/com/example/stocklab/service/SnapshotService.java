package com.example.stocklab.service;

import com.example.stocklab.model.Eksperiment;
import com.example.stocklab.model.Stock;
import com.example.stocklab.repository.EksperimentRepository;
import com.example.stocklab.repository.StockPriceRepository;
import com.example.stocklab.service.Snapshot.Instrument;
import com.example.stocklab.service.Snapshot.Tocka;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.time.temporal.IsoFields;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** #60: zaprt in anonimiziran posnetek podatkov do odločitvenega datuma. */
@Service
public class SnapshotService {

    /** Ena dnevna cena (vhod za pripravo posnetka). */
    public record CenaDne(LocalDate datum, BigDecimal cena) {}

    private final EksperimentRepository eksperimentRepository;
    private final StockPriceRepository stockPriceRepository;

    public SnapshotService(EksperimentRepository eksperimentRepository, StockPriceRepository stockPriceRepository) {
        this.eksperimentRepository = eksperimentRepository;
        this.stockPriceRepository = stockPriceRepository;
    }

    @Transactional(readOnly = true)
    public Snapshot ustvari(Long eksperimentId) {
        Eksperiment e = eksperimentRepository.findById(eksperimentId)
                .orElseThrow(() -> new IllegalStateException("Eksperiment " + eksperimentId + " ne obstaja"));
        LocalDate odl = e.getOdlocitveniDatum();
        LocalDate od = odl.minusYears(e.getLetaZgodovine());

        List<Instrument> instrumenti = new ArrayList<>();
        for (Stock s : e.getInstrumenti()) {
            if (s.getAnonOznaka() == null) {
                throw new IllegalStateException("Instrument " + s.getId() + " nima anonimne oznake");
            }
            List<CenaDne> cene = stockPriceRepository
                    .findByIdStockIdAndIdDatumBetweenOrderByIdDatum(s.getId(), od, odl).stream()
                    .map(p -> new CenaDne(p.getId().getDatum(), p.getCena()))
                    .toList();
            List<Tocka> tocke = pripravi(cene, od, odl);
            if (tocke.size() < 2) {
                throw new IllegalStateException("Premalo podatkov za " + s.getAnonOznaka());
            }
            instrumenti.add(statistika(s.getId(), s.getAnonOznaka(), tocke));
        }
        instrumenti.sort(Comparator.comparing(Instrument::oznaka));

        long meseci = ChronoUnit.MONTHS.between(odl, e.getKonecVrednotenja());
        return new Snapshot(e.getLetaZgodovine(), meseci, instrumenti);
    }

    /**
     * Cene samo v [od, odl], zadnja cena v vsakem ISO tednu, normalizirano na 100 na prvi teden.
     * Statična in brez baze, zato jo test lahko preveri neposredno.
     */
    static List<Tocka> pripravi(List<CenaDne> cene, LocalDate od, LocalDate odl) {
        Map<Integer, CenaDne> poTednih = new LinkedHashMap<>();
        cene.stream()
                .filter(c -> c.datum() != null && c.cena() != null && c.cena().signum() > 0)
                .filter(c -> !c.datum().isBefore(od) && !c.datum().isAfter(odl))   // nič iz prihodnosti
                .sorted(Comparator.comparing(CenaDne::datum))
                .forEach(c -> poTednih.put(
                        c.datum().get(IsoFields.WEEK_BASED_YEAR) * 100 + c.datum().get(IsoFields.WEEK_OF_WEEK_BASED_YEAR),
                        c));                                                         // zadnja v tednu prepiše prejšnje

        List<Tocka> tocke = new ArrayList<>();
        double prva = 0;
        for (CenaDne c : poTednih.values()) {
            double v = c.cena().doubleValue();
            if (tocke.isEmpty()) prva = v;
            tocke.add(new Tocka(c.datum(), zaokrozi(v / prva * 100, 2)));
        }
        return tocke;
    }

    static Instrument statistika(Long stockId, String oznaka, List<Tocka> tocke) {
        double prva = tocke.get(0).vrednost();
        double zadnja = tocke.get(tocke.size() - 1).vrednost();
        double donos = (zadnja / prva - 1) * 100;

        // letna nihajnost iz tedenskih logaritemskih donosov
        List<Double> r = new ArrayList<>();
        for (int i = 1; i < tocke.size(); i++) {
            r.add(Math.log(tocke.get(i).vrednost() / tocke.get(i - 1).vrednost()));
        }
        double povp = r.stream().mapToDouble(Double::doubleValue).average().orElse(0);
        double var = r.stream().mapToDouble(x -> (x - povp) * (x - povp)).sum() / Math.max(1, r.size() - 1);
        double nihajnost = Math.sqrt(var) * Math.sqrt(52) * 100;

        // največji padec od vrha
        double vrh = prva;
        double padec = 0;
        for (Tocka t : tocke) {
            vrh = Math.max(vrh, t.vrednost());
            padec = Math.min(padec, (t.vrednost() / vrh - 1) * 100);
        }

        return new Instrument(stockId, oznaka, tocke, zaokrozi(donos, 1), zaokrozi(nihajnost, 1), zaokrozi(padec, 1));
    }

    private static double zaokrozi(double v, int decimalke) {
        double f = Math.pow(10, decimalke);
        return Math.round(v * f) / f;
    }
}
