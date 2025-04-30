package org.example.exception;

public class ObjectExistedException extends RuntimeException {

    public ObjectExistedException(final String mes) {
        super(mes);
    }
}
