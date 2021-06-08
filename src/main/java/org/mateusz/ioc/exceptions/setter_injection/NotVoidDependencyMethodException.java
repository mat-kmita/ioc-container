package org.mateusz.ioc.exceptions.setter_injection;

public class NotVoidDependencyMethodException extends DependencyMethodException {
    public NotVoidDependencyMethodException(Class<?> aClass, String methodName) {
        super("DependencyMethod setter must have void return type", aClass, methodName);
    }
}
