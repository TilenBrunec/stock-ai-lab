package com.example.stocklab.dto;

import java.time.LocalDate;

/** Najzgodnejši in najpoznejši datum cen v bazi. */
public record DateRangeDto(LocalDate zacetek, LocalDate konec) {}
