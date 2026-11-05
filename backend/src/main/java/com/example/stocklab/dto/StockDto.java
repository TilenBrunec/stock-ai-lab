package com.example.stocklab.dto;

import com.example.stocklab.model.Stock;

public record StockDto(Long id, String ticker, String ime, String tip) {

    public static StockDto from(Stock s) {
        return new StockDto(s.getId(), s.getTicker(), s.getIme(), s.getTip().name());
    }
}