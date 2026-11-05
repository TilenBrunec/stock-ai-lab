package com.example.stocklab.model;

import jakarta.persistence.*;
import java.math.BigDecimal;

@Entity
@Table(name = "stock_price")
public class StockPrice {

    @EmbeddedId
    private StockPriceId id;

    @MapsId("stockId")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "stock_id")
    private Stock stock;

    @Column(nullable = false, precision = 14, scale = 4)
    private BigDecimal cena;

    protected StockPrice() {}

    public StockPriceId getId() { return id; }
    public Stock getStock() { return stock; }
    public BigDecimal getCena() { return cena; }
}