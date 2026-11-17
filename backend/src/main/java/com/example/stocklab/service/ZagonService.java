package com.example.stocklab.service;

import com.example.stocklab.model.Eksperiment;
import com.example.stocklab.model.Status;
import com.example.stocklab.model.Zagon;
import com.example.stocklab.repository.EksperimentRepository;
import com.example.stocklab.repository.StockRepository;
import com.example.stocklab.repository.ZagonRepository;
import com.example.stocklab.service.OdgovorValidator.Sklep;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

/** Kratke transakcije za spreminjanje stanja zagonov med izvedbo v ozadju (#64). */
@Service
public class ZagonService {

    /** Kar runner potrebuje o zagonu, brez lazy entitet izven transakcije. */
    public record ZagonInfo(Long id, String openrouterId, String model) {}

    private final ZagonRepository zagonRepository;
    private final EksperimentRepository eksperimentRepository;
    private final StockRepository stockRepository;

    public ZagonService(ZagonRepository zagonRepository,
                        EksperimentRepository eksperimentRepository,
                        StockRepository stockRepository) {
        this.zagonRepository = zagonRepository;
        this.eksperimentRepository = eksperimentRepository;
        this.stockRepository = stockRepository;
    }

    @Transactional(readOnly = true)
    public List<ZagonInfo> zaIzvedbo(Long eksperimentId) {
        return zagonRepository.findByEksperimentIdOrderById(eksperimentId).stream()
                .filter(z -> z.getStatus() == Status.CAKA)
                .map(z -> new ZagonInfo(z.getId(), z.getModel().getOpenrouterId(), z.getModel().getIme()))
                .toList();
    }

    @Transactional
    public void zacni(Long zagonId) {
        zagon(zagonId).setStatus(Status.V_TEKU);
    }

    @Transactional
    public void shraniOdlocitve(Long zagonId, Map<String, Sklep> sklepi, Map<String, Long> stockIdPoOznaki) {
        Zagon z = zagon(zagonId);
        sklepi.forEach((oznaka, sklep) -> z.dodajOdlocitev(
                stockRepository.getReferenceById(stockIdPoOznaki.get(oznaka)),
                sklep.tip(),
                sklep.utemeljitev()));
    }

    @Transactional
    public void koncaj(Long zagonId) {
        zagon(zagonId).setStatus(Status.KONCANO);
    }

    @Transactional
    public void neuspeh(Long zagonId, String razlog) {
        zagon(zagonId).neuspeh(razlog);
    }

    /**
     * Na koncu izvedbe: nedokončane zagone označi kot neuspešne, eksperiment pa kot KONCANO,
     * če je uspel vsaj en model, sicer NEUSPESNO.
     */
    @Transactional
    public void zakljuciEksperiment(Long eksperimentId, String razlogPrekinitve) {
        Eksperiment e = eksperimentRepository.findById(eksperimentId).orElseThrow();
        for (Zagon z : e.getZagoni()) {
            if (z.getStatus() == Status.CAKA || z.getStatus() == Status.V_TEKU) {
                z.neuspeh(razlogPrekinitve != null ? razlogPrekinitve : "Izvedba je bila prekinjena");
            }
        }
        boolean uspel = e.getZagoni().stream().anyMatch(z -> z.getStatus() == Status.KONCANO);
        e.setStatus(uspel ? Status.KONCANO : Status.NEUSPESNO);
    }

    private Zagon zagon(Long id) {
        return zagonRepository.findById(id)
                .orElseThrow(() -> new IllegalStateException("Zagon " + id + " ne obstaja"));
    }
}
