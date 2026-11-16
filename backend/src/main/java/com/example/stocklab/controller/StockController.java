package com.example.stocklab.controller;

import com.example.stocklab.dto.DateRangeDto;
import com.example.stocklab.dto.PriceDto;
import com.example.stocklab.dto.StockDto;
import com.example.stocklab.service.StockService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

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

    @GetMapping("/range")
    public DateRangeDto range() {
        return stockService.range();
    }

    @GetMapping("/random")
    public List<StockDto> random(
            @RequestParam int n,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate od) {
        return stockService.random(n, od);
    }
}
