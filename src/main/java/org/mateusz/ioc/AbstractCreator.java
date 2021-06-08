package org.mateusz.ioc;

import org.mateusz.ioc.exceptions.*;
import org.mateusz.ioc.exceptions.setter_injection.NoArgumentsDependencyMethodException;
import org.mateusz.ioc.exceptions.setter_injection.NotVoidDependencyMethodException;

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

    protected List<Method> findDependencyMethodSetters()
            throws
            NotVoidDependencyMethodException,
            NoArgumentsDependencyMethodException {

        List<Method> setters = Arrays.stream(this.toClass.getMethods())
                .filter(method -> method.isAnnotationPresent(DependencyMethod.class))
                .collect(Collectors.toList());

        for(Method setter : setters) {
            if(!setter.getReturnType().equals(Void.TYPE)) {
                throw new NotVoidDependencyMethodException(toClass, setter.getName());
            } else if(setter.getParameterCount() < 1) {
                throw new NoArgumentsDependencyMethodException(toClass, setter.getName());
            }
        }

        return setters;
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

    // Auxiliary method to declutter createObject()
    private Object createObjectFromContainer(Class<?> aClass, List<Class<?>> updatedDependent)
            throws
            NotInContainerException {

        Optional<ICreator<?>> optionalICreator = this.container.getCreator(aClass);
        ICreator<?> creator = optionalICreator.orElseThrow(() -> new NotInContainerException(aClass));
        return ((AbstractCreator<?>) creator).createObject(updatedDependent);
    }

    // Auxiliary method to declutter createObject()
    // Resolves dependencies specified by @DependencyMethod setter's parameters
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

        // We need a constructor to create an object
        // If there is any @DependencyConstructor present then it will be used
        // Otherwise use best matching one
        Constructor<To> constructor = null;
        try {
            constructor = findAnnotatedConstructor().orElse(findBestMatchConstructor(dependent));
        } catch (DependencyConstructorException | NoConstructorInBeanException e) {
            e.printStackTrace();
            throw new ObjectCreationException(toClass);
        }

        // Add this class to dependencies list
        List<Class<?>> updatedDependent = new ArrayList<>(dependent);
        updatedDependent.add(this.toClass);

        try {
            // Use the chosen constructor to create a new instance of To type
            // Resolve other dependencies first
             final To instance = constructor
                    .newInstance(Arrays.stream(constructor.getParameterTypes())
                                    .map(aClass -> createObjectFromContainer(aClass, updatedDependent))
                                    .toArray());

             // Now resolve dependencies specified by @DependencyMethod setters
            for(Method setter : findDependencyMethodSetters()) {
                List<Object> parameters = constructParametersForSetter(setter, updatedDependent);
                setter.invoke(instance, parameters.toArray());
            }

            return instance;
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException |
                NoArgumentsDependencyMethodException | NotVoidDependencyMethodException e) {
            e.printStackTrace();
            throw new ObjectCreationException(toClass);
        }
    }
}
