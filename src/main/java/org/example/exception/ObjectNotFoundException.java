package org.example.exception;


public class ObjectNotFoundException extends RuntimeException {
    public ObjectNotFoundException(final String msg) {
        super(msg);
    }
}
