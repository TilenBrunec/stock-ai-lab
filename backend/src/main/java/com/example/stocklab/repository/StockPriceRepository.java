package com.example.stocklab.repository;

import com.example.stocklab.model.StockPrice;
import com.example.stocklab.model.StockPriceId;
import org.springframework.data.jpa.repository.JpaRepository;
import java.time.LocalDate;
import java.util.List;

public interface StockPriceRepository extends JpaRepository<StockPrice, StockPriceId> {
    List<StockPrice> findByIdStockIdAndIdDatumBetweenOrderByIdDatum(Long stockId, LocalDate from, LocalDate to);
}