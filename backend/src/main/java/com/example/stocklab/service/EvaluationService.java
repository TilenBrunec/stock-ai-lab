package com.example.stocklab.service;

import com.example.stocklab.model.Eksperiment;
import com.example.stocklab.model.Odlocitev;
import com.example.stocklab.model.OdlocitevTip;
import com.example.stocklab.model.StockPrice;
import com.example.stocklab.model.Zagon;
import com.example.stocklab.repository.StockPriceRepository;
import com.example.stocklab.repository.ZagonRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/** #65: vrednotenje odločitev zagona z realnimi cenami po odločitvenem datumu. */
@Service
public class EvaluationService {

    private final ZagonRepository zagonRepository;
    private final StockPriceRepository stockPriceRepository;

    public EvaluationService(ZagonRepository zagonRepository, StockPriceRepository stockPriceRepository) {
        this.zagonRepository = zagonRepository;
        this.stockPriceRepository = stockPriceRepository;
    }

    @Transactional
    public void ovrednoti(Long zagonId) {
        Zagon zagon = zagonRepository.findById(zagonId)
                .orElseThrow(() -> new IllegalStateException("Zagon " + zagonId + " ne obstaja"));
        Eksperiment e = zagon.getEksperiment();
        List<Odlocitev> odlocitve = zagon.getOdlocitve();

        List<Vrednotenje.Postavka> postavke = new ArrayList<>();
        for (Odlocitev o : odlocitve) {
            Long stockId = o.getStock().getId();
            postavke.add(new Vrednotenje.Postavka(
                    o.getOdlocitev() == OdlocitevTip.KUPI,
                    cenaNaDan(stockId, e.getOdlocitveniDatum()),
                    cenaNaDan(stockId, e.getKonecVrednotenja())));
        }

        Vrednotenje.Rezultat r = Vrednotenje.izracunaj(
                e.getKapital(), e.getOdlocitveniDatum(), e.getKonecVrednotenja(), postavke);

        for (int i = 0; i < odlocitve.size(); i++) {
            Vrednotenje.PostavkaRezultat pr = r.postavke().get(i);
            odlocitve.get(i).nastaviRezultat(pr.donos(), pr.pravilna());
        }
        zagon.nastaviRezultat(r.koncnaVrednost(), r.zasluzek(), r.donos(), r.cagr(), r.delezPravilnih());
    }

    /** Zadnja znana cena na dan ali pred njim (vikendi, prazniki). */
    private BigDecimal cenaNaDan(Long stockId, LocalDate datum) {
        return stockPriceRepository
                .findFirstByIdStockIdAndIdDatumLessThanEqualOrderByIdDatumDesc(stockId, datum)
                .map(StockPrice::getCena)
                .orElseThrow(() -> new IllegalStateException("Ni cene za instrument " + stockId + " do " + datum));
    }
}
