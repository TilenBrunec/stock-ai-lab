package com.example.stocklab.repository;

import com.example.stocklab.model.Stock;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface StockRepository extends JpaRepository<Stock, Long> {
    List<Stock> findByImeContainingIgnoreCaseOrTickerContainingIgnoreCaseOrderByTicker(String ime, String ticker);
    List<Stock> findAllByOrderByTicker();
}