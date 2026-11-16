package com.example.stocklab.service;

import com.example.stocklab.dto.CreateExperimentRequest;
import com.example.stocklab.dto.ExperimentDto;
import com.example.stocklab.dto.ExperimentSummaryDto;
import com.example.stocklab.model.Eksperiment;
import com.example.stocklab.model.LlmModel;
import com.example.stocklab.model.Stock;
import com.example.stocklab.model.Uporabnik;
import com.example.stocklab.repository.EksperimentRepository;
import com.example.stocklab.repository.LlmModelRepository;
import com.example.stocklab.repository.StockPriceRepository;
import com.example.stocklab.repository.StockRepository;
import com.example.stocklab.repository.UporabnikRepository;
import com.example.stocklab.service.ExperimentValidator.InstrumentInfo;
import org.springframework.http.HttpStatus;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class ExperimentService {

    private final EksperimentRepository eksperimentRepository;
    private final UporabnikRepository uporabnikRepository;
    private final StockRepository stockRepository;
    private final StockPriceRepository stockPriceRepository;
    private final LlmModelRepository llmModelRepository;
    private final StockService stockService;

    public ExperimentService(EksperimentRepository eksperimentRepository,
                             UporabnikRepository uporabnikRepository,
                             StockRepository stockRepository,
                             StockPriceRepository stockPriceRepository,
                             LlmModelRepository llmModelRepository,
                             StockService stockService) {
        this.eksperimentRepository = eksperimentRepository;
        this.uporabnikRepository = uporabnikRepository;
        this.stockRepository = stockRepository;
        this.stockPriceRepository = stockPriceRepository;
        this.llmModelRepository = llmModelRepository;
        this.stockService = stockService;
    }

    /** #56: ustvari eksperiment s statusom CAKA in po en zagon za vsak izbran model. */
    @Transactional
    public ExperimentDto create(Jwt jwt, CreateExperimentRequest r) {
        Uporabnik uporabnik = prijavljen(jwt);

        // instrumenti, ki obstajajo, in njihov prvi datum s ceno
        Set<Long> ids = new LinkedHashSet<>(r.instrumentIds() == null ? List.of() : r.instrumentIds());
        ids.remove(null);
        List<Stock> stocks = ids.isEmpty() || ids.size() > ExperimentValidator.MAX_INSTRUMENTOV
                ? List.of()
                : stockRepository.findAllById(ids);
        Map<Long, LocalDate> prviDatumi = new HashMap<>();
        if (!stocks.isEmpty()) {
            for (Object[] row : stockPriceRepository.findPrviDatumi(ids)) {
                prviDatumi.put((Long) row[0], (LocalDate) row[1]);
            }
        }
        Map<Long, InstrumentInfo> info = stocks.stream().collect(Collectors.toMap(
                Stock::getId, s -> new InstrumentInfo(s.getTicker(), prviDatumi.get(s.getId()))));

        // aktivni modeli med izbranimi
        Set<Integer> modelIds = new LinkedHashSet<>(r.modelIds() == null ? List.of() : r.modelIds());
        modelIds.remove(null);
        List<LlmModel> modeli = llmModelRepository.findAllById(modelIds).stream()
                .filter(LlmModel::isAktiven)
                .toList();
        Set<Integer> aktivni = modeli.stream().map(LlmModel::getId).collect(Collectors.toSet());

        Map<String, String> napake = ExperimentValidator.validate(
                r, stockService.range(), info, aktivni, uporabnik.imaKljuc());
        if (!napake.isEmpty()) {
            throw new NeveljavniPodatkiException(napake);
        }

        Eksperiment e = new Eksperiment(uporabnik, r.odlocitveniDatum(), r.letaZgodovine(),
                r.konecVrednotenja(), r.kapital(), new LinkedHashSet<>(stocks));
        modeli.forEach(e::dodajZagon);
        return ExperimentDto.from(eksperimentRepository.save(e));
    }

    @Transactional(readOnly = true)
    public List<ExperimentSummaryDto> moji(Jwt jwt) {
        return eksperimentRepository.findByUporabnikIdOrderByUstvarjenDesc(uporabnikId(jwt)).stream()
                .map(ExperimentSummaryDto::from)
                .toList();
    }

    /** Eksperiment prijavljenega uporabnika; tuj ali neobstoječ vrne 404. */
    @Transactional(readOnly = true)
    public ExperimentDto najdi(Jwt jwt, Long id) {
        return ExperimentDto.from(eksperimentRepository.findByIdAndUporabnikId(id, uporabnikId(jwt))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Eksperiment ne obstaja")));
    }

    private Uporabnik prijavljen(Jwt jwt) {
        return uporabnikRepository.findById(uporabnikId(jwt))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED,
                        "Uporabnik ni registriran - osveži stran"));
    }

    private static UUID uporabnikId(Jwt jwt) {
        return UUID.fromString(jwt.getSubject());
    }
}
