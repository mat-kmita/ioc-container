package org.mateusz.ioc.exceptions;

public class NoConstructorInBeanException extends Exception {
    public NoConstructorInBeanException(Class<?> aClass) {
        super("No constructor found in " + aClass);
    }
}
