package com.example.stocklab.dto;

import com.example.stocklab.model.Uporabnik;
import java.util.UUID;

/** Podatki o prijavljenem uporabniku. Celotnega OpenRouter ključa NE vsebuje. */
public record MeDto(
        UUID id,
        String email,
        String ime,
        String slikaUrl,
        String vloga,
        boolean imaKljuc,
        String kljucZadnji4) {

    public static MeDto from(Uporabnik u) {
        return new MeDto(
                u.getId(),
                u.getEmail(),
                u.getIme(),
                u.getSlikaUrl(),
                u.getVloga().name(),
                u.imaKljuc(),
                u.getOpenrouterKljucZadnji4());
    }
}
