package com.example.stocklab.service;

import java.util.Map;

/** Napake validacije po poljih; ApiExceptionHandler jih vrne kot 400 {"message", "napake": {polje: sporočilo}}. */
public class NeveljavniPodatkiException extends RuntimeException {

    private final Map<String, String> napake;

    public NeveljavniPodatkiException(Map<String, String> napake) {
        super("Podatki niso veljavni");
        this.napake = Map.copyOf(napake);
    }

    public Map<String, String> getNapake() { return napake; }
}
