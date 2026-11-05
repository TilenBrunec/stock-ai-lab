package com.example.stocklab.controller;

import com.example.stocklab.dto.StockDto;
import com.example.stocklab.service.StockService;
import org.springframework.web.bind.annotation.*;
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
}