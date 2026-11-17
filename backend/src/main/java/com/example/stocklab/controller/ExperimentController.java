package com.example.stocklab.controller;

import com.example.stocklab.dto.CreateExperimentRequest;
import com.example.stocklab.dto.ExperimentDto;
import com.example.stocklab.dto.ExperimentSummaryDto;
import com.example.stocklab.service.ExperimentRunner;
import com.example.stocklab.service.ExperimentService;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/experiments")
public class ExperimentController {

    private final ExperimentService experimentService;
    private final ExperimentRunner experimentRunner;

    public ExperimentController(ExperimentService experimentService, ExperimentRunner experimentRunner) {
        this.experimentService = experimentService;
        this.experimentRunner = experimentRunner;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ExperimentDto create(@AuthenticationPrincipal Jwt jwt, @RequestBody CreateExperimentRequest request) {
        return experimentService.create(jwt, request);
    }

    @GetMapping
    public List<ExperimentSummaryDto> moji(@AuthenticationPrincipal Jwt jwt) {
        return experimentService.moji(jwt);
    }

    @GetMapping("/{id}")
    public ExperimentDto najdi(@AuthenticationPrincipal Jwt jwt, @PathVariable Long id) {
        return experimentService.najdi(jwt, id);
    }

    /** #64: zagon v ozadju. Vrne 202 takoj; frontend nato osvežuje GET /{id}. */
    @PostMapping("/{id}/run")
    @ResponseStatus(HttpStatus.ACCEPTED)
    public ExperimentDto run(@AuthenticationPrincipal Jwt jwt, @PathVariable Long id) {
        ExperimentDto dto = experimentService.zacni(jwt, id);          // transakcija se tu potrdi
        experimentRunner.izvedi(id, UUID.fromString(jwt.getSubject())); // @Async - ne čaka
        return dto;
    }
}
