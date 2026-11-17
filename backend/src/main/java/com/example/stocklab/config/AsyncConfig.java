package com.example.stocklab.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;

/** Omogoči @Async: eksperiment se izvaja v ozadju, HTTP zahtevek pa takoj vrne 202 (#64). */
@Configuration
@EnableAsync
public class AsyncConfig {}
