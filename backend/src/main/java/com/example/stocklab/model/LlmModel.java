package com.example.stocklab.model;

import jakarta.persistence.*;

@Entity
@Table(name = "llm_model")
public class LlmModel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "openrouter_id", nullable = false, unique = true, length = 200)
    private String openrouterId;

    @Column(nullable = false, length = 100)
    private String ime;

    @Column(nullable = false)
    private boolean placljiv;

    @Column(nullable = false)
    private boolean aktiven = true;

    protected LlmModel() {}

    public LlmModel(String openrouterId, String ime, boolean placljiv) {
        this.openrouterId = openrouterId;
        this.ime = ime;
        this.placljiv = placljiv;
    }

    public Integer getId() { return id; }
    public String getOpenrouterId() { return openrouterId; }
    public String getIme() { return ime; }
    public boolean isPlacljiv() { return placljiv; }
    public boolean isAktiven() { return aktiven; }
}
