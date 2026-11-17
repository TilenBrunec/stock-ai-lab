package com.example.stocklab.service;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.server.ResponseStatusException;

import java.time.Duration;
import java.util.List;

/** Klici na OpenRouter API. Ključ uporabnika se nikoli ne izpiše v log. */
@Component
public class OpenRouterClient {

    // --- telo zahtevka in odgovora za /chat/completions ---
    record Message(String role, String content) {}

    record ChatRequest(String model, List<Message> messages, double temperature) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    record ChatResponse(List<Choice> choices, ErrorBody error) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    record Choice(Message message) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    record ErrorBody(String message, Integer code) {}

    private final RestClient rest;

    public OpenRouterClient(@Value("${openrouter.base-url:https://openrouter.ai/api/v1}") String baseUrl) {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(Duration.ofSeconds(10));
        factory.setReadTimeout(Duration.ofSeconds(180));   // klici modelov znajo trajati

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

    /**
     * #62: klic modela (POST /chat/completions). Vrne besedilo odgovora.
     * @throws LlmKlicException z opisom napake in podatkom, ali je smiselno poskusiti znova
     */
    public String chat(String apiKey, String model, String sistemsko, String uporabnisko) {
        ChatRequest body = new ChatRequest(model,
                List.of(new Message("system", sistemsko), new Message("user", uporabnisko)),
                0.2);
        try {
            ChatResponse odgovor = rest.post()
                    .uri("/chat/completions")
                    .header("Authorization", "Bearer " + apiKey)
                    .header("X-Title", "StockLab")
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(body)
                    .retrieve()
                    .body(ChatResponse.class);

            if (odgovor == null) {
                throw new LlmKlicException("Prazen odgovor OpenRouterja", true);
            }
            if (odgovor.error() != null) {
                throw new LlmKlicException("OpenRouter: " + odgovor.error().message(), true);
            }
            if (odgovor.choices() == null || odgovor.choices().isEmpty()
                    || odgovor.choices().get(0).message() == null
                    || odgovor.choices().get(0).message().content() == null) {
                throw new LlmKlicException("Model ni vrnil besedila", true);
            }
            return odgovor.choices().get(0).message().content();

        } catch (HttpStatusCodeException e) {
            int status = e.getStatusCode().value();
            throw switch (status) {
                case 401 -> new LlmKlicException("OpenRouter ključ ni veljaven", false);
                case 402 -> new LlmKlicException("Na OpenRouter računu ni dovolj kreditov", false);
                case 404 -> new LlmKlicException("Model " + model + " na OpenRouterju ne obstaja", false);
                case 429 -> new LlmKlicException("Omejitev zahtevkov pri OpenRouterju (rate limit)", true);
                default -> new LlmKlicException("OpenRouter je vrnil napako " + status, status >= 500);
            };
        } catch (RestClientException e) {
            throw new LlmKlicException("OpenRouter ni dosegljiv", true);
        }
    }
}
