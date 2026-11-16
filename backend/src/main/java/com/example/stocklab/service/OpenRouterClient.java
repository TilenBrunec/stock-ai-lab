package com.example.stocklab.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.server.ResponseStatusException;

import java.time.Duration;

/** Klici na OpenRouter API. Ključ uporabnika se nikoli ne izpiše v log. */
@Component
public class OpenRouterClient {

    private final RestClient rest;

    public OpenRouterClient(@Value("${openrouter.base-url:https://openrouter.ai/api/v1}") String baseUrl) {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(Duration.ofSeconds(10));
        factory.setReadTimeout(Duration.ofSeconds(120));   // klici modelov znajo trajati

        this.rest = RestClient.builder()
                .baseUrl(baseUrl)
                .requestFactory(factory)
                .build();
    }

    /**
     * Preveri ključ s klicem GET /key.
     * @return true, če je ključ veljaven; false, če ga OpenRouter zavrne (401/403)
     * @throws ResponseStatusException 502, če OpenRouter ni dosegljiv
     */
    public boolean isKeyValid(String apiKey) {
        try {
            rest.get()
                    .uri("/key")
                    .header("Authorization", "Bearer " + apiKey)
                    .retrieve()
                    .toBodilessEntity();
            return true;
        } catch (HttpClientErrorException e) {
            int status = e.getStatusCode().value();
            if (status == 401 || status == 403) {
                return false;
            }
            throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "OpenRouter je vrnil napako " + status);
        } catch (RestClientException e) {
            throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "OpenRouter ni dosegljiv");
        }
    }
}
