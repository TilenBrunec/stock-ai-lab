package com.example.stocklab.service;

import com.example.stocklab.dto.LlmModelDto;
import com.example.stocklab.repository.LlmModelRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
public class ModelService {

    private final LlmModelRepository repository;

    public ModelService(LlmModelRepository repository) {
        this.repository = repository;
    }

    /** Aktivni modeli: najprej brezplačni, nato po imenu. */
    public List<LlmModelDto> aktivni() {
        return repository.findByAktivenTrueOrderByPlacljivAscImeAsc().stream()
                .map(LlmModelDto::from)
                .toList();
    }
}
