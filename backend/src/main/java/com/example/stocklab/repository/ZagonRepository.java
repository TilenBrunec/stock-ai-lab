package com.example.stocklab.repository;

import com.example.stocklab.model.Zagon;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ZagonRepository extends JpaRepository<Zagon, Long> {
    List<Zagon> findByEksperimentIdOrderById(Long eksperimentId);
}
