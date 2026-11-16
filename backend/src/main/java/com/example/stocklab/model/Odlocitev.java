package com.example.stocklab.model;

import jakarta.persistence.*;

import java.math.BigDecimal;

/** Odločitev modela za en instrument (KUPI / NE_KUPI) z utemeljitvijo. */
@Entity
@Table(name = "odlocitev")
public class Odlocitev {

    @EmbeddedId
    private OdlocitevId id = new OdlocitevId();

    @MapsId("zagonId")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "zagon_id")
    private Zagon zagon;

    @MapsId("stockId")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "stock_id")
    private Stock stock;

    @Enumerated(EnumType.STRING)
    @Column(name = "odlocitev", nullable = false, length = 10)
    private OdlocitevTip odlocitev;

    @Column(nullable = false, length = 4000)
    private String utemeljitev;

    @Column(name = "donos_instrumenta", precision = 10, scale = 2)
    private BigDecimal donosInstrumenta;

    private Boolean pravilna;

    protected Odlocitev() {}

    Odlocitev(Zagon zagon, Stock stock, OdlocitevTip odlocitev, String utemeljitev) {
        this.zagon = zagon;
        this.stock = stock;
        this.odlocitev = odlocitev;
        this.utemeljitev = utemeljitev.length() > 4000 ? utemeljitev.substring(0, 4000) : utemeljitev;
    }

    public OdlocitevId getId() { return id; }
    public Zagon getZagon() { return zagon; }
    public Stock getStock() { return stock; }
    public OdlocitevTip getOdlocitev() { return odlocitev; }
    public String getUtemeljitev() { return utemeljitev; }
    public BigDecimal getDonosInstrumenta() { return donosInstrumenta; }
    public Boolean getPravilna() { return pravilna; }

    public void nastaviRezultat(BigDecimal donosInstrumenta, boolean pravilna) {
        this.donosInstrumenta = donosInstrumenta;
        this.pravilna = pravilna;
    }
}
