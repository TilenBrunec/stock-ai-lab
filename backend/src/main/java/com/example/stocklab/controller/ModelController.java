package com.example.stocklab.controller;

import com.example.stocklab.dto.LlmModelDto;
import com.example.stocklab.service.ModelService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/models")
public class ModelController {

    private final ModelService modelService;

    public ModelController(ModelService modelService) {
        this.modelService = modelService;
    }

    @GetMapping
    public List<LlmModelDto> list() {
        return modelService.aktivni();
    }
}
