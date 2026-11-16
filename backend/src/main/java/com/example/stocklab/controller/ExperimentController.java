package com.example.stocklab.controller;

import com.example.stocklab.dto.CreateExperimentRequest;
import com.example.stocklab.dto.ExperimentDto;
import com.example.stocklab.dto.ExperimentSummaryDto;
import com.example.stocklab.service.ExperimentService;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/experiments")
public class ExperimentController {

    private final ExperimentService experimentService;

    public ExperimentController(ExperimentService experimentService) {
        this.experimentService = experimentService;
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
}
