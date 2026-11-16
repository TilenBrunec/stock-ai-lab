package com.example.stocklab.repository;

import com.example.stocklab.model.StockPrice;
import com.example.stocklab.model.StockPriceId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.Collection;
import java.util.List;

public interface StockPriceRepository extends JpaRepository<StockPrice, StockPriceId> {
    List<StockPrice> findByIdStockIdAndIdDatumBetweenOrderByIdDatum(Long stockId, LocalDate from, LocalDate to);

    @Query("SELECT min(p.id.datum) FROM StockPrice p")
    LocalDate findMinDatum();

    @Query("SELECT max(p.id.datum) FROM StockPrice p")
    LocalDate findMaxDatum();

    /** Prvi datum s ceno za vsak podan instrument: vrstice [stockId (Long), datum (LocalDate)]. */
    @Query("SELECT p.id.stockId, min(p.id.datum) FROM StockPrice p WHERE p.id.stockId IN :ids GROUP BY p.id.stockId")
    List<Object[]> findPrviDatumi(@Param("ids") Collection<Long> ids);
}
