package org.mateusz.ioc.exceptions.setter_injection;

public class NoArgumentsDependencyMethodException extends DependencyMethodException {
    public NoArgumentsDependencyMethodException(Class<?> aClass, String methodName) {
        super("DependencyMethod setter must have at least one argument", aClass, methodName);
    }
}
