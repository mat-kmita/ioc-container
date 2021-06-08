package org.mateusz.ioc.exceptions;

public class ObjectCreationException extends RuntimeException {
    public ObjectCreationException(Class<?> aClass) {
        super("Couldn't create object of type " + aClass);
    }
}
