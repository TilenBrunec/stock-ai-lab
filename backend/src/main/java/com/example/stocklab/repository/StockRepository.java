package com.example.stocklab.repository;

import com.example.stocklab.model.Stock;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

public interface StockRepository extends JpaRepository<Stock, Long> {
    List<Stock> findByImeContainingIgnoreCaseOrTickerContainingIgnoreCaseOrderByTicker(String ime, String ticker);
    List<Stock> findAllByOrderByTicker();

    @Query(value = "SELECT * FROM stock ORDER BY random() LIMIT :n", nativeQuery = true)
    List<Stock> findRandom(@Param("n") int n);

    /** Naključni instrumenti, ki imajo cene vsaj od podanega datuma naprej. */
    @Query(value = """
            SELECT s.* FROM stock s
            WHERE (SELECT min(p.datum) FROM stock_price p WHERE p.stock_id = s.id) <= :od
            ORDER BY random()
            LIMIT :n
            """, nativeQuery = true)
    List<Stock> findRandomWithDataSince(@Param("od") LocalDate od, @Param("n") int n);
}
