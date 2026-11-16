package com.example.stocklab.repository;

import com.example.stocklab.model.LlmModel;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface LlmModelRepository extends JpaRepository<LlmModel, Integer> {
    List<LlmModel> findByAktivenTrueOrderByPlacljivAscImeAsc();
}
