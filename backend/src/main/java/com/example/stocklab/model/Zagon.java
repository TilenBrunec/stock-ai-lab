package com.example.stocklab.model;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/** Izvedba eksperimenta z enim modelom. */
@Entity
@Table(name = "zagon")
public class Zagon {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "eksperiment_id")
    private Eksperiment eksperiment;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "model_id")
    private LlmModel model;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Status status = Status.CAKA;

    @Column(length = 1000)
    private String napaka;

    @Column(name = "koncna_vrednost", precision = 14, scale = 2)
    private BigDecimal koncnaVrednost;

    @Column(precision = 14, scale = 2)
    private BigDecimal zasluzek;

    @Column(precision = 10, scale = 2)
    private BigDecimal donos;

    @Column(precision = 10, scale = 2)
    private BigDecimal cagr;

    @Column(name = "delez_pravilnih", precision = 5, scale = 2)
    private BigDecimal delezPravilnih;

    @OneToMany(mappedBy = "zagon", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Odlocitev> odlocitve = new ArrayList<>();

    protected Zagon() {}

    Zagon(Eksperiment eksperiment, LlmModel model) {
        this.eksperiment = eksperiment;
        this.model = model;
    }

    public Long getId() { return id; }
    public Eksperiment getEksperiment() { return eksperiment; }
    public LlmModel getModel() { return model; }
    public Status getStatus() { return status; }
    public String getNapaka() { return napaka; }
    public BigDecimal getKoncnaVrednost() { return koncnaVrednost; }
    public BigDecimal getZasluzek() { return zasluzek; }
    public BigDecimal getDonos() { return donos; }
    public BigDecimal getCagr() { return cagr; }
    public BigDecimal getDelezPravilnih() { return delezPravilnih; }
    public List<Odlocitev> getOdlocitve() { return odlocitve; }

    public void setStatus(Status status) { this.status = status; }

    public void neuspeh(String razlog) {
        this.status = Status.NEUSPESNO;
        this.napaka = razlog == null ? null : razlog.substring(0, Math.min(razlog.length(), 1000));
    }

    public void nastaviRezultat(BigDecimal koncnaVrednost, BigDecimal zasluzek, BigDecimal donos,
                                BigDecimal cagr, BigDecimal delezPravilnih) {
        this.koncnaVrednost = koncnaVrednost;
        this.zasluzek = zasluzek;
        this.donos = donos;
        this.cagr = cagr;
        this.delezPravilnih = delezPravilnih;
    }

    public Odlocitev dodajOdlocitev(Stock stock, OdlocitevTip tip, String utemeljitev) {
        Odlocitev o = new Odlocitev(this, stock, tip, utemeljitev);
        odlocitve.add(o);
        return o;
    }
}
