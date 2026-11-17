package com.example.stocklab.service;

/**
 * Napaka pri klicu modela prek OpenRouterja. "ponovljiva" pove, ali ima smisel poskusiti znova,
 * "pocakajMs" pa, koliko naj se počaka pred ponovitvijo (0 = privzeto).
 */
public class LlmKlicException extends RuntimeException {

    private final boolean ponovljiva;
    private final long pocakajMs;

    public LlmKlicException(String message, boolean ponovljiva) {
        this(message, ponovljiva, 0);
    }

    public LlmKlicException(String message, boolean ponovljiva, long pocakajMs) {
        super(message);
        this.ponovljiva = ponovljiva;
        this.pocakajMs = pocakajMs;
    }

    public boolean isPonovljiva() { return ponovljiva; }

    public long getPocakajMs() { return pocakajMs; }
}