package com.example.stocklab.controller;

import com.example.stocklab.dto.KljucRequest;
import com.example.stocklab.dto.MeDto;
import com.example.stocklab.service.UporabnikService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/me")
public class MeController {

    private final UporabnikService uporabnikService;

    public MeController(UporabnikService uporabnikService) {
        this.uporabnikService = uporabnikService;
    }

    @GetMapping
    public MeDto me(@AuthenticationPrincipal Jwt jwt) {
        return uporabnikService.getOrCreate(jwt);
    }

    @PutMapping("/openrouter-key")
    public MeDto shraniKljuc(@AuthenticationPrincipal Jwt jwt, @RequestBody KljucRequest request) {
        return uporabnikService.shraniKljuc(jwt, request.kljuc());
    }

    @DeleteMapping("/openrouter-key")
    public MeDto izbrisiKljuc(@AuthenticationPrincipal Jwt jwt) {
        return uporabnikService.izbrisiKljuc(jwt);
    }
}
