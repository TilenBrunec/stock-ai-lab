package com.example.stocklab.controller;

import com.example.stocklab.service.NeveljavniPodatkiException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Map;

@RestControllerAdvice
public class ApiExceptionHandler {

    /** 400 z napakami po poljih: {"message": "...", "napake": {"kapital": "..."}} */
    @ExceptionHandler(NeveljavniPodatkiException.class)
    public ResponseEntity<Map<String, Object>> neveljavniPodatki(NeveljavniPodatkiException e) {
        return ResponseEntity.badRequest().body(Map.of(
                "message", e.getMessage(),
                "napake", e.getNapake()));
    }
}
