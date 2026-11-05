package com.example.stocklab.dto;

import com.example.stocklab.model.StockPrice;
import java.math.BigDecimal;
import java.time.LocalDate;

public record PriceDto(LocalDate datum, BigDecimal cena) {

    public static PriceDto from(StockPrice p) {
        return new PriceDto(p.getId().getDatum(), p.getCena());
    }
}