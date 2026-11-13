package com.example.stocklab.dto;

import com.example.stocklab.model.Uporabnik;
import java.util.UUID;

public record MeDto(UUID id, String email, String ime, String slikaUrl, String vloga) {

    public static MeDto from(Uporabnik u) {
        return new MeDto(u.getId(), u.getEmail(), u.getIme(), u.getSlikaUrl(), u.getVloga().name());
    }
}