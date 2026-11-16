package com.example.stocklab.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/** Telo zahtevka POST /api/experiments. Validacija je v ExperimentValidator. */
public record CreateExperimentRequest(
        List<Long> instrumentIds,
        LocalDate odlocitveniDatum,
        Integer letaZgodovine,
        LocalDate konecVrednotenja,
        BigDecimal kapital,
        List<Integer> modelIds) {}
