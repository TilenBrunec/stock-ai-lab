package com.example.stocklab.controller;

import com.example.stocklab.config.SecurityConfig;
import com.example.stocklab.dto.ExperimentDto;
import com.example.stocklab.service.ExperimentService;
import com.example.stocklab.service.NeveljavniPodatkiException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ExperimentController.class)
@Import(SecurityConfig.class)
class ExperimentControllerTest {

    static final String TELO = """
            {"instrumentIds":[1,2],"odlocitveniDatum":"2018-01-02","letaZgodovine":3,
             "konecVrednotenja":"2023-01-02","kapital":10000,"modelIds":[1]}
            """;

    @Autowired MockMvc mvc;

    @MockitoBean ExperimentService experimentService;
    @MockitoBean JwtDecoder jwtDecoder;

    static ExperimentDto dto() {
        return new ExperimentDto(5L, LocalDate.of(2018, 1, 2), 3, LocalDate.of(2023, 1, 2),
                new BigDecimal("10000.00"), "CAKA", Instant.now(), List.of(),
                List.of(new ExperimentDto.ZagonDto(10L, 1, "Gemma", false, "CAKA", null)));
    }

    @Test
    void create_brezZetona_vrne401() throws Exception {
        mvc.perform(post("/api/experiments").contentType(MediaType.APPLICATION_JSON).content(TELO))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void create_veljaven_vrne201InStatusCaka() throws Exception {
        when(experimentService.create(any(), any())).thenReturn(dto());

        mvc.perform(post("/api/experiments").with(jwt())
                        .contentType(MediaType.APPLICATION_JSON).content(TELO))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(5))
                .andExpect(jsonPath("$.status").value("CAKA"))
                .andExpect(jsonPath("$.zagoni[0].status").value("CAKA"));
    }

    @Test
    void create_neveljaven_vrne400ZNapakamiPoPoljih() throws Exception {
        when(experimentService.create(any(), any()))
                .thenThrow(new NeveljavniPodatkiException(Map.of("kapital", "Kapital mora biti večji od 0 €")));

        mvc.perform(post("/api/experiments").with(jwt())
                        .contentType(MediaType.APPLICATION_JSON).content(TELO))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.napake.kapital").exists());
    }

    @Test
    void najdi_tuj_vrne404() throws Exception {
        when(experimentService.najdi(any(), eq(99L)))
                .thenThrow(new ResponseStatusException(HttpStatus.NOT_FOUND));

        mvc.perform(get("/api/experiments/99").with(jwt()))
                .andExpect(status().isNotFound());
    }

    @Test
    void moji_brezZetona_vrne401() throws Exception {
        mvc.perform(get("/api/experiments")).andExpect(status().isUnauthorized());
    }
}
