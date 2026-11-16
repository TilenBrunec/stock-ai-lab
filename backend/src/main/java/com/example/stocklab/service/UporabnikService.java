package com.example.stocklab.service;

import com.example.stocklab.dto.MeDto;
import com.example.stocklab.model.Uporabnik;
import com.example.stocklab.repository.UporabnikRepository;
import org.springframework.http.HttpStatus;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.Map;
import java.util.UUID;

@Service
public class UporabnikService {

    private final UporabnikRepository repository;
    private final CryptoService cryptoService;
    private final OpenRouterClient openRouterClient;

    public UporabnikService(UporabnikRepository repository,
                            CryptoService cryptoService,
                            OpenRouterClient openRouterClient) {
        this.repository = repository;
        this.cryptoService = cryptoService;
        this.openRouterClient = openRouterClient;
    }

    /** Vrne prijavljenega uporabnika; ob prvem klicu ga ustvari z vlogo RAZISKOVALEC. */
    @Transactional
    public MeDto getOrCreate(Jwt jwt) {
        return MeDto.from(getOrCreateEntity(jwt));
    }

    /** Preveri ključ prek OpenRouterja, ga šifrira in shrani. */
    @Transactional
    public MeDto shraniKljuc(Jwt jwt, String kljuc) {
        String k = kljuc == null ? "" : kljuc.trim();
        if (k.length() < 10) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Ključ je prazen ali prekratek");
        }
        if (!openRouterClient.isKeyValid(k)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "OpenRouter ključa ni sprejel");
        }

        Uporabnik u = getOrCreateEntity(jwt);
        u.nastaviKljuc(cryptoService.encrypt(k), k.substring(k.length() - 4));
        return MeDto.from(u);
    }

    @Transactional
    public MeDto izbrisiKljuc(Jwt jwt) {
        Uporabnik u = getOrCreateEntity(jwt);
        u.izbrisiKljuc();
        return MeDto.from(u);
    }

    /** Dešifriran ključ za klice modelov (faza 2, US-06). Samo za notranjo uporabo, nikoli v odgovor. */
    @Transactional(readOnly = true)
    public String desifriranKljuc(UUID uporabnikId) {
        Uporabnik u = repository.findById(uporabnikId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Uporabnik ne obstaja"));
        if (!u.imaKljuc()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Uporabnik nima shranjenega OpenRouter ključa");
        }
        return cryptoService.decrypt(u.getOpenrouterKljucEnc());
    }

    private Uporabnik getOrCreateEntity(Jwt jwt) {
        UUID id = UUID.fromString(jwt.getSubject());
        String email = jwt.getClaimAsString("email");
        Map<String, Object> meta = jwt.getClaimAsMap("user_metadata");
        String ime = meta == null ? null : (String) meta.getOrDefault("full_name", meta.get("name"));
        String slika = meta == null ? null : (String) meta.getOrDefault("avatar_url", meta.get("picture"));

        Uporabnik u = repository.findById(id)
                .orElseGet(() -> repository.save(new Uporabnik(id, email, ime, slika)));
        u.posodobiProfil(email, ime, slika);   // ime ali slika se v Googlu lahko spremenita
        return u;
    }
}
