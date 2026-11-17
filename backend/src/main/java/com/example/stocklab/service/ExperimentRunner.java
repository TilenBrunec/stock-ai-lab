package com.example.stocklab.service;

import com.example.stocklab.service.OdgovorValidator.Sklep;
import com.example.stocklab.service.ZagonService.ZagonInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.UUID;

/**
 * #64: izvedba eksperimenta v ozadju. Za vsak model: posnetek -> klic -> validacija -> vrednotenje.
 * Napaka enega modela ne ustavi ostalih.
 */
@Component
public class ExperimentRunner {

    private static final Logger log = LoggerFactory.getLogger(ExperimentRunner.class);

    private final SnapshotService snapshotService;
    private final LlmService llmService;
    private final ZagonService zagonService;
    private final EvaluationService evaluationService;
    private final UporabnikService uporabnikService;

    public ExperimentRunner(SnapshotService snapshotService,
                            LlmService llmService,
                            ZagonService zagonService,
                            EvaluationService evaluationService,
                            UporabnikService uporabnikService) {
        this.snapshotService = snapshotService;
        this.llmService = llmService;
        this.zagonService = zagonService;
        this.evaluationService = evaluationService;
        this.uporabnikService = uporabnikService;
    }

    @Async
    public void izvedi(Long eksperimentId, UUID uporabnikId) {
        String razlogPrekinitve = null;
        try {
            String kljuc = uporabnikService.desifriranKljuc(uporabnikId);
            Snapshot snapshot = snapshotService.ustvari(eksperimentId);

            for (ZagonInfo z : zagonService.zaIzvedbo(eksperimentId)) {
                zagonService.zacni(z.id());
                try {
                    Map<String, Sklep> sklepi = llmService.odloci(kljuc, z.openrouterId(), snapshot);
                    zagonService.shraniOdlocitve(z.id(), sklepi, snapshot.stockIdPoOznaki());
                    evaluationService.ovrednoti(z.id());
                    zagonService.koncaj(z.id());
                    log.info("Eksperiment {}: model {} končan", eksperimentId, z.model());
                } catch (Exception e) {
                    log.warn("Eksperiment {}: model {} ni uspel: {}", eksperimentId, z.model(), e.getMessage());
                    zagonService.neuspeh(z.id(), e.getMessage());
                }
            }
        } catch (Exception e) {
            log.error("Eksperiment {} ni uspel", eksperimentId, e);
            razlogPrekinitve = e.getMessage();
        } finally {
            zagonService.zakljuciEksperiment(eksperimentId, razlogPrekinitve);
        }
    }
}
