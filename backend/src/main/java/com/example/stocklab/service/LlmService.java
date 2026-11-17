package com.example.stocklab.service;

import com.example.stocklab.service.OdgovorValidator.Sklep;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import tools.jackson.databind.json.JsonMapper;

import java.util.Map;

/** #62 + #63: pošlje posnetek modelu, preveri odgovor in ob napaki poskusi največ 2-krat znova. */
@Service
public class LlmService {

    static final int MAX_PONOVITEV = 2;
    private static final Logger log = LoggerFactory.getLogger(LlmService.class);

    private final OpenRouterClient client;
    private final JsonMapper json = JsonMapper.builder().build();

    public LlmService(OpenRouterClient client) {
        this.client = client;
    }

    public Map<String, Sklep> odloci(String apiKey, String model, Snapshot snapshot) {
        String sistemsko = PromptBuilder.sistemsko();
        String uporabnisko = PromptBuilder.uporabnisko(snapshot);
        String zadnjaNapaka = "neznana napaka";

        for (int poskus = 1; poskus <= MAX_PONOVITEV + 1; poskus++) {
            try {
                String besedilo = client.chat(apiKey, model, sistemsko, uporabnisko);
                return OdgovorValidator.validiraj(preberi(besedilo), snapshot.oznake());
            } catch (NeveljavenOdgovorException | LlmKlicException e) {
                zadnjaNapaka = e.getMessage();
                log.warn("Model {}: poskus {} ni uspel: {}", model, poskus, e.getMessage());
                if (e instanceof LlmKlicException k && !k.isPonovljiva()) {
                    break;                       // npr. neveljaven ključ ali ni kreditov - ponovitev ne pomaga
                }
                if (poskus <= MAX_PONOVITEV) {
                    // rate limit: počakaj, kolikor zahteva OpenRouter (Retry-After), sicer 10 s, 20 s
                    long privzeto = poskus * 10_000L;
                    long zahtevano = e instanceof LlmKlicException lk ? lk.getPocakajMs() : 0;
                    pocakaj(Math.max(privzeto, zahtevano));
                }
            }
        }
        throw new LlmKlicException(zadnjaNapaka, false);
    }

    /** Izlušči JSON iz odgovora (modeli ga pogosto zavijejo v ```json ... ```). */
    OdgovorModela preberi(String besedilo) {
        if (besedilo == null) {
            throw new NeveljavenOdgovorException("Prazen odgovor");
        }
        int zacetek = besedilo.indexOf('{');
        int konec = besedilo.lastIndexOf('}');
        if (zacetek < 0 || konec <= zacetek) {
            throw new NeveljavenOdgovorException("Odgovor ne vsebuje JSON objekta");
        }
        try {
            return json.readValue(besedilo.substring(zacetek, konec + 1), OdgovorModela.class);
        } catch (RuntimeException e) {
            throw new NeveljavenOdgovorException("Odgovor ni veljaven JSON");
        }
    }

    private static void pocakaj(long ms) {
        try {
            Thread.sleep(ms);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}