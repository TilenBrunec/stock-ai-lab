package com.example.stocklab.controller;

import com.example.stocklab.dto.MeDto;
import com.example.stocklab.service.UporabnikService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
}