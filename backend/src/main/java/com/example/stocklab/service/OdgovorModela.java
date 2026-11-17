package com.example.stocklab.service;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;

/** JSON, ki ga vrne model: {"odlocitve":[{"oznaka":"S001","odlocitev":"KUPI","utemeljitev":"..."}]} */
@JsonIgnoreProperties(ignoreUnknown = true)
public record OdgovorModela(List<Postavka> odlocitve) {

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Postavka(String oznaka, String odlocitev, String utemeljitev) {}
}
