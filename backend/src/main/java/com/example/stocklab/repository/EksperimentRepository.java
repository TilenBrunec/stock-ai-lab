package com.example.stocklab.repository;

import com.example.stocklab.model.Eksperiment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface EksperimentRepository extends JpaRepository<Eksperiment, Long> {
    List<Eksperiment> findByUporabnikIdOrderByUstvarjenDesc(UUID uporabnikId);
    Optional<Eksperiment> findByIdAndUporabnikId(Long id, UUID uporabnikId);
}
