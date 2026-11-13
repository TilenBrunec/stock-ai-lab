package com.example.stocklab.model;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "uporabnik")
public class Uporabnik {

    @Id
    private UUID id;

    @Column(nullable = false, length = 320)
    private String email;

    @Column(length = 200)
    private String ime;

    @Column(name = "slika_url", length = 1000)
    private String slikaUrl;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Vloga vloga = Vloga.RAZISKOVALEC;

    @Column(nullable = false)
    private Instant ustvarjen = Instant.now();

    protected Uporabnik() {}

    public Uporabnik(UUID id, String email, String ime, String slikaUrl) {
        this.id = id;
        this.email = email;
        this.ime = ime;
        this.slikaUrl = slikaUrl;
    }

    public UUID getId() { return id; }
    public String getEmail() { return email; }
    public String getIme() { return ime; }
    public String getSlikaUrl() { return slikaUrl; }
    public Vloga getVloga() { return vloga; }
    public Instant getUstvarjen() { return ustvarjen; }

    public void posodobiProfil(String email, String ime, String slikaUrl) {
        this.email = email;
        this.ime = ime;
        this.slikaUrl = slikaUrl;
    }
}