package org.mateusz.ioc;

import org.mateusz.ioc.exceptions.*;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.lang.reflect.Method;
import java.util.*;
import java.util.stream.Collectors;

public abstract class AbstractCreator<To> implements ICreator<To> {

    protected Class<To> toClass;
    protected Container container;

    public AbstractCreator(Class<To> classObject, Container container) {
        this.toClass = classObject;
        this.container = container;
    }

    private List<Constructor<To>> getAllConstructors() {
        return Arrays.stream(toClass.getConstructors())
                .map(c -> {
                    try {
                        return toClass.getConstructor(c.getParameterTypes());
                    } catch (NoSuchMethodException e) {
                        e.printStackTrace();
                    }
                    return null;
                })
                .collect(Collectors.toList());
    }

    protected List<Constructor<To>> findMaxArgsConstructors() {
        int max = Arrays.stream(toClass.getConstructors()).
                mapToInt(Constructor::getParameterCount)
                .max().orElse(0);

        return this.getAllConstructors().stream()
                .filter(c -> c.getParameterCount() == max)
                .collect(Collectors.toList());
    }

    protected Optional<Constructor<To>> findAnnotatedConstructor() throws DependencyConstructorException {

        List<Constructor<To>> annotatedConstructors = this.getAllConstructors().stream()
                .filter(c -> c.isAnnotationPresent(DependencyConstructor.class))
                .collect(Collectors.toList());

        if (annotatedConstructors.size() > 1) {
            throw new DependencyConstructorException("Only one @DependencyConstructor is allowed!");
        }

        if (annotatedConstructors.size() < 1) return Optional.empty();
        return Optional.ofNullable(annotatedConstructors.get(0));
    }

    protected List<Method> findDependencyMethodSetters() {
        return Arrays.stream(this.toClass.getMethods())
                .filter(method -> method.isAnnotationPresent(DependencyMethod.class))
                .filter(method -> method.getReturnType().equals(Void.TYPE))
                .filter(method -> method.getParameterCount() > 0)
                .collect(Collectors.toList());
    }

    private boolean hasNoCycle(List<Class<?>> dependent, List<Class<?>> parameters) {
        return parameters.stream().noneMatch(dependent::contains);
    }

    protected Constructor<To> findBestMatchConstructor(List<Class<?>> dependent)
            throws
            DependencyConstructorException,
            NoConstructorInBeanException,
            DependenciesCycleException {

        List<Constructor<To>> constructorList;
        constructorList = findMaxArgsConstructors();

        if (constructorList.size() > 1) {
            throw new DependencyConstructorException("More than one constructor is suitable for creating an object of type " + this.toClass);
        }

        if (constructorList.size() < 1) {
            throw new NoConstructorInBeanException(this.toClass);
        }

        return constructorList.get(0);
    }

    private Object createObjectFromContainer(Class<?> aClass, List<Class<?>> updatedDependent)
            throws
            NotInContainerException {

        Optional<ICreator<?>> optionalICreator = this.container.getCreator(aClass);
        ICreator<?> creator = optionalICreator.orElseThrow(() -> new NotInContainerException(aClass));
        return ((AbstractCreator<?>) creator).createObject(updatedDependent);
    }

    private List<Object> constructParametersForSetter(Method setter, List<Class<?>> dependencies) {
        List<Object> list = new ArrayList<>();
        for (Class<?> aClass : setter.getParameterTypes()) {
            Object objectFromContainer = this.createObjectFromContainer(aClass, dependencies);
            list.add(objectFromContainer);
        }
        return list;
    }

    protected To createObject(List<Class<?>> dependent)  {
        if(!hasNoCycle(dependent, Collections.singletonList(toClass))) {
            throw new DependenciesCycleException(toClass);
        }

        Constructor<To> constructor = null;
        try {
            constructor = findAnnotatedConstructor().orElse(findBestMatchConstructor(dependent));
        } catch (DependencyConstructorException | NoConstructorInBeanException e) {
            e.printStackTrace();
            throw new ObjectCreationException(toClass);
        }

        List<Class<?>> updatedDependent = new ArrayList<>(dependent);
        updatedDependent.add(this.toClass);

        try {
             final To instance = constructor
                    .newInstance(
                            Arrays.stream(constructor.getParameterTypes())
                                    .map(aClass -> createObjectFromContainer(aClass, updatedDependent))
                                    .toArray());


            for(Method setter : findDependencyMethodSetters()) {
                List<Object> parameters = constructParametersForSetter(setter, updatedDependent);
                setter.invoke(instance, parameters.toArray());
            }

            return instance;
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException e) {
            e.printStackTrace();
            throw new ObjectCreationException(toClass);
        }
    }
}
