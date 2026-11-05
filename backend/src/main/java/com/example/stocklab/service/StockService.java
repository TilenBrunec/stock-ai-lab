package com.example.stocklab.service;

import com.example.stocklab.dto.PriceDto;
import com.example.stocklab.dto.StockDto;
import com.example.stocklab.repository.StockPriceRepository;
import com.example.stocklab.repository.StockRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;

import java.util.List;

@Service
@Transactional(readOnly = true)
public class StockService {

    private final StockRepository stockRepository;
    private final StockPriceRepository stockPriceRepository;

    public StockService(StockRepository stockRepository, StockPriceRepository stockPriceRepository) {
        this.stockRepository = stockRepository;
        this.stockPriceRepository = stockPriceRepository;
    }

    public List<StockDto> findAll(String q) {
        var stocks = (q == null || q.isBlank())
                ? stockRepository.findAllByOrderByTicker()
                : stockRepository.findByImeContainingIgnoreCaseOrTickerContainingIgnoreCaseOrderByTicker(q.trim(), q.trim());
        return stocks.stream().map(StockDto::from).toList();
    }

    public List<PriceDto> findPrices(Long stockId, LocalDate from, LocalDate to) {
        if (!stockRepository.existsById(stockId)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Instrument " + stockId + " ne obstaja");
        }
        LocalDate start = (from != null) ? from : LocalDate.of(1900, 1, 1);
        LocalDate end = (to != null) ? to : LocalDate.now();
        if (start.isAfter(end)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "'from' mora biti pred 'to'");
        }
        return stockPriceRepository
                .findByIdStockIdAndIdDatumBetweenOrderByIdDatum(stockId, start, end)
                .stream().map(PriceDto::from).toList();
    }
}