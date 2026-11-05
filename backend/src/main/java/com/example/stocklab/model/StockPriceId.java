package com.example.stocklab.model;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import java.io.Serializable;
import java.time.LocalDate;
import java.util.Objects;

@Embeddable
public class StockPriceId implements Serializable {

    @Column(name = "stock_id")
    private Long stockId;

    @Column(name = "datum")
    private LocalDate datum;

    protected StockPriceId() {}

    public Long getStockId() { return stockId; }
    public LocalDate getDatum() { return datum; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof StockPriceId other)) return false;
        return Objects.equals(stockId, other.stockId) && Objects.equals(datum, other.datum);
    }

    @Override
    public int hashCode() { return Objects.hash(stockId, datum); }
}