package com.example.stocklab.service;

import com.example.stocklab.dto.StockDto;
import com.example.stocklab.repository.StockRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
@Transactional(readOnly = true)
public class StockService {

    private final StockRepository stockRepository;

    public StockService(StockRepository stockRepository) {
        this.stockRepository = stockRepository;
    }

    public List<StockDto> findAll(String q) {
        var stocks = (q == null || q.isBlank())
                ? stockRepository.findAllByOrderByTicker()
                : stockRepository.findByImeContainingIgnoreCaseOrTickerContainingIgnoreCaseOrderByTicker(q.trim(), q.trim());
        return stocks.stream().map(StockDto::from).toList();
    }
}