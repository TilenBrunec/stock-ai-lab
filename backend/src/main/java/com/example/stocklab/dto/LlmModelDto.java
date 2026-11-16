package com.example.stocklab.dto;

import com.example.stocklab.model.LlmModel;

public record LlmModelDto(Integer id, String ime, boolean placljiv) {

    public static LlmModelDto from(LlmModel m) {
        return new LlmModelDto(m.getId(), m.getIme(), m.isPlacljiv());
    }
}
