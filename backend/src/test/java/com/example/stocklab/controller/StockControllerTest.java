package com.example.stocklab.controller;

import com.example.stocklab.dto.PriceDto;
import com.example.stocklab.dto.StockDto;
import com.example.stocklab.service.StockService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(StockController.class)
class StockControllerTest {

    @Autowired
    MockMvc mvc;

    @MockitoBean
    StockService stockService;

    @Test
    void list_vrneVseInstrumente() throws Exception {
        when(stockService.findAll(null)).thenReturn(List.of(
                new StockDto(1L, "AAPL", "Apple", "DELNICA"),
                new StockDto(2L, "SPY", "SPDR S&P 500 ETF", "SKLAD")));

        mvc.perform(get("/api/stocks"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].ticker").value("AAPL"))
                .andExpect(jsonPath("$[1].tip").value("SKLAD"))
                .andExpect(jsonPath("$[0].anonOznaka").doesNotExist());
    }

    @Test
    void list_posredujeIskalniNiz() throws Exception {
        when(stockService.findAll("apple")).thenReturn(List.of(
                new StockDto(1L, "AAPL", "Apple", "DELNICA")));

        mvc.perform(get("/api/stocks").param("q", "apple"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1));

        verify(stockService).findAll("apple");
    }

    @Test
    void prices_vrneCeneZaObdobje() throws Exception {
        when(stockService.findPrices(1L, LocalDate.of(2024, 1, 1), LocalDate.of(2024, 1, 31)))
                .thenReturn(List.of(
                        new PriceDto(LocalDate.of(2024, 1, 2), new BigDecimal("185.6400")),
                        new PriceDto(LocalDate.of(2024, 1, 3), new BigDecimal("184.2500"))));

        mvc.perform(get("/api/stocks/1/prices").param("from", "2024-01-01").param("to", "2024-01-31"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].datum").value("2024-01-02"))
                .andExpect(jsonPath("$[0].cena").value(185.64));
    }

    @Test
    void prices_brezParametrov_posredujeNull() throws Exception {
        when(stockService.findPrices(1L, null, null)).thenReturn(List.of());

        mvc.perform(get("/api/stocks/1/prices"))
                .andExpect(status().isOk());

        verify(stockService).findPrices(1L, null, null);
    }

    @Test
    void prices_neobstojecInstrument_vrne404() throws Exception {
        when(stockService.findPrices(eq(999L), any(), any()))
                .thenThrow(new ResponseStatusException(HttpStatus.NOT_FOUND));

        mvc.perform(get("/api/stocks/999/prices"))
                .andExpect(status().isNotFound());
    }

    @Test
    void prices_napacenDatum_vrne400() throws Exception {
        mvc.perform(get("/api/stocks/1/prices").param("from", "banana"))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(stockService);
    }
}