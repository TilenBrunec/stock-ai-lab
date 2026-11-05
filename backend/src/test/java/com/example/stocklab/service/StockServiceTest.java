package com.example.stocklab.service;

import com.example.stocklab.repository.StockPriceRepository;
import com.example.stocklab.repository.StockRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class StockServiceTest {

    StockRepository stockRepository;
    StockPriceRepository stockPriceRepository;
    StockService service;

    @BeforeEach
    void setUp() {
        stockRepository = mock(StockRepository.class);
        stockPriceRepository = mock(StockPriceRepository.class);
        service = new StockService(stockRepository, stockPriceRepository);
    }

    @Test
    void findPrices_neobstojecInstrument_vrze404() {
        when(stockRepository.existsById(999L)).thenReturn(false);

        assertThatThrownBy(() -> service.findPrices(999L, null, null))
                .isInstanceOf(ResponseStatusException.class)
                .satisfies(e -> assertThat(((ResponseStatusException) e).getStatusCode())
                        .isEqualTo(HttpStatus.NOT_FOUND));
    }

    @Test
    void findPrices_fromPoTo_vrze400() {
        when(stockRepository.existsById(1L)).thenReturn(true);

        assertThatThrownBy(() -> service.findPrices(1L, LocalDate.of(2025, 1, 1), LocalDate.of(2024, 1, 1)))
                .isInstanceOf(ResponseStatusException.class)
                .satisfies(e -> assertThat(((ResponseStatusException) e).getStatusCode())
                        .isEqualTo(HttpStatus.BAD_REQUEST));
    }

    @Test
    void findPrices_brezDatumov_uporabiCeloObdobje() {
        when(stockRepository.existsById(1L)).thenReturn(true);
        when(stockPriceRepository.findByIdStockIdAndIdDatumBetweenOrderByIdDatum(anyLong(), any(), any()))
                .thenReturn(List.of());

        service.findPrices(1L, null, null);

        verify(stockPriceRepository).findByIdStockIdAndIdDatumBetweenOrderByIdDatum(
                1L, LocalDate.of(1900, 1, 1), LocalDate.now());
    }

    @Test
    void findAll_praznoIskanje_vrneVse() {
        when(stockRepository.findAllByOrderByTicker()).thenReturn(List.of());

        service.findAll("   ");

        verify(stockRepository).findAllByOrderByTicker();
        verify(stockRepository, never())
                .findByImeContainingIgnoreCaseOrTickerContainingIgnoreCaseOrderByTicker(any(), any());
    }
}