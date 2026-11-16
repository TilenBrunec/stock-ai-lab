package com.example.stocklab.model;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

@Entity
@Table(name = "eksperiment")
public class Eksperiment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "uporabnik_id")
    private Uporabnik uporabnik;

    @Column(name = "odlocitveni_datum", nullable = false)
    private LocalDate odlocitveniDatum;

    @Column(name = "leta_zgodovine", nullable = false)
    private int letaZgodovine;

    @Column(name = "konec_vrednotenja", nullable = false)
    private LocalDate konecVrednotenja;

    @Column(nullable = false, precision = 14, scale = 2)
    private BigDecimal kapital;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Status status = Status.CAKA;

    @Column(nullable = false)
    private Instant ustvarjen = Instant.now();

    @ManyToMany
    @JoinTable(
            name = "eksperiment_instrument",
            joinColumns = @JoinColumn(name = "eksperiment_id"),
            inverseJoinColumns = @JoinColumn(name = "stock_id"))
    private Set<Stock> instrumenti = new LinkedHashSet<>();

    @OneToMany(mappedBy = "eksperiment", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("id")
    private List<Zagon> zagoni = new ArrayList<>();

    protected Eksperiment() {}

    public Eksperiment(Uporabnik uporabnik, LocalDate odlocitveniDatum, int letaZgodovine,
                       LocalDate konecVrednotenja, BigDecimal kapital, Set<Stock> instrumenti) {
        this.uporabnik = uporabnik;
        this.odlocitveniDatum = odlocitveniDatum;
        this.letaZgodovine = letaZgodovine;
        this.konecVrednotenja = konecVrednotenja;
        this.kapital = kapital;
        this.instrumenti = new LinkedHashSet<>(instrumenti);
    }

    public Zagon dodajZagon(LlmModel model) {
        Zagon z = new Zagon(this, model);
        zagoni.add(z);
        return z;
    }

    public Long getId() { return id; }
    public Uporabnik getUporabnik() { return uporabnik; }
    public LocalDate getOdlocitveniDatum() { return odlocitveniDatum; }
    public int getLetaZgodovine() { return letaZgodovine; }
    public LocalDate getKonecVrednotenja() { return konecVrednotenja; }
    public BigDecimal getKapital() { return kapital; }
    public Status getStatus() { return status; }
    public Instant getUstvarjen() { return ustvarjen; }
    public Set<Stock> getInstrumenti() { return instrumenti; }
    public List<Zagon> getZagoni() { return zagoni; }

    public void setStatus(Status status) { this.status = status; }
}
