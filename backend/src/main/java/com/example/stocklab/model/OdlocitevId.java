package com.example.stocklab.model;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

import java.io.Serializable;
import java.util.Objects;

@Embeddable
public class OdlocitevId implements Serializable {

    @Column(name = "zagon_id")
    private Long zagonId;

    @Column(name = "stock_id")
    private Long stockId;

    protected OdlocitevId() {}

    public Long getZagonId() { return zagonId; }
    public Long getStockId() { return stockId; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof OdlocitevId other)) return false;
        return Objects.equals(zagonId, other.zagonId) && Objects.equals(stockId, other.stockId);
    }

    @Override
    public int hashCode() { return Objects.hash(zagonId, stockId); }
}
