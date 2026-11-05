package com.example.stocklab.controller;

import com.example.stocklab.dto.StockDto;
import com.example.stocklab.service.StockService;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import com.example.stocklab.dto.PriceDto;
import org.springframework.format.annotation.DateTimeFormat;
import java.time.LocalDate;

@RestController
@RequestMapping("/api/stocks")
public class StockController {

    private final StockService stockService;

    public StockController(StockService stockService) {
        this.stockService = stockService;
    }

    @GetMapping
    public List<StockDto> list(@RequestParam(required = false) String q) {
        return stockService.findAll(q);
    }
    @GetMapping("/{id}/prices")
    public List<PriceDto> prices(
            @PathVariable Long id,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {
        return stockService.findPrices(id, from, to);
    }
}