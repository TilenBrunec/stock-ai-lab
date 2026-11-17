package com.example.stocklab.service;

/** Napaka pri klicu modela prek OpenRouterja. "ponovljiva" pove, ali ima smisel poskusiti znova. */
public class LlmKlicException extends RuntimeException {

    private final boolean ponovljiva;

    public LlmKlicException(String message, boolean ponovljiva) {
        super(message);
        this.ponovljiva = ponovljiva;
    }

    public boolean isPonovljiva() { return ponovljiva; }
}
