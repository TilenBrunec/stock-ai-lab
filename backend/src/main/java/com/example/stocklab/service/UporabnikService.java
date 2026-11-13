package com.example.stocklab.service;

import com.example.stocklab.dto.MeDto;
import com.example.stocklab.model.Uporabnik;
import com.example.stocklab.repository.UporabnikRepository;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;
import java.util.UUID;

@Service
public class UporabnikService {

    private final UporabnikRepository repository;

    public UporabnikService(UporabnikRepository repository) {
        this.repository = repository;
    }

    /** Vrne prijavljenega uporabnika; ob prvem klicu ga ustvari z vlogo RAZISKOVALEC. */
    @Transactional
    public MeDto getOrCreate(Jwt jwt) {
        UUID id = UUID.fromString(jwt.getSubject());
        String email = jwt.getClaimAsString("email");
        Map<String, Object> meta = jwt.getClaimAsMap("user_metadata");
        String ime = meta == null ? null : (String) meta.getOrDefault("full_name", meta.get("name"));
        String slika = meta == null ? null : (String) meta.getOrDefault("avatar_url", meta.get("picture"));

        Uporabnik u = repository.findById(id)
                .orElseGet(() -> repository.save(new Uporabnik(id, email, ime, slika)));
        u.posodobiProfil(email, ime, slika);   // ime ali slika se v Googlu lahko spremenita
        return MeDto.from(u);
    }
}