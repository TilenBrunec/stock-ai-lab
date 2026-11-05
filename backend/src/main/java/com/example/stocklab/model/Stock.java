package com.example.stocklab.model;

import jakarta.persistence.*;

@Entity
@Table(name = "stock")
public class Stock {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 16)
    private String ticker;

    @Column(nullable = false, length = 200)
    private String ime;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private InstrumentType tip;

    @Column(name = "anon_oznaka", unique = true, length = 8)
    private String anonOznaka;

    protected Stock() {}

    public Long getId() { return id; }
    public String getTicker() { return ticker; }
    public String getIme() { return ime; }
    public InstrumentType getTip() { return tip; }
    public String getAnonOznaka() { return anonOznaka; }
}