package com.weather.exception;

public class SpotNotFoundException extends RuntimeException {

    public SpotNotFoundException(Long id) {
        super("Photo spot not found: id=" + id);
    }
}
