package org.mateusz.ioc.exceptions.setter_injection;

public class DependencyMethodException extends Exception {
    public DependencyMethodException(String message, Class<?> aClass, String methodName) {
        super("DependencyMethod exception occurred in method " + methodName + " of class " + aClass + ". " + message);
    }
}
