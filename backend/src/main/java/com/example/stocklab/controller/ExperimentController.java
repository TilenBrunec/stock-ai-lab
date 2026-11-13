package com.example.stocklab.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** Začasno: pravi endpoint pride v fazi 2 (US-03). Rabi ga pogoj 401 iz US-01. */
@RestController
@RequestMapping("/api/experiments")
public class ExperimentController {

    @PostMapping
    public ResponseEntity<Void> create() {
        return ResponseEntity.status(HttpStatus.NOT_IMPLEMENTED).build();
    }
}