package org.mateusz.ioc.exceptions;

public class NotInContainerException extends RuntimeException {
    public NotInContainerException(Class<?> aClass) {
        super("Class " + aClass + " not found inside IoC container");
    }
}
