package org.mateusz.ioc.exceptions;

public class DependenciesCycleException extends RuntimeException {
    public DependenciesCycleException(Class<?> aClass) {
        super("Dependencies cycle detected in " + aClass);
    }

    public DependenciesCycleException(Class<?> aClass, String methodName) {
        super("Dependencies cycle detected in " + aClass + " in setter " + methodName);
    }
}
