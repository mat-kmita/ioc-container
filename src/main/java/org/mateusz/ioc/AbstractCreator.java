package org.mateusz.ioc;

import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
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

    protected List<Constructor<To>> findMaxArgsConstructors() throws Exception {
        int max = Arrays.stream(toClass.getConstructors()).
                mapToInt(Constructor::getParameterCount)
                .max().orElseThrow(() -> new Exception("No constructors!"));

        return this.getAllConstructors().stream()
                .filter(c -> c.getParameterCount() == max)
                .collect(Collectors.toList());
    }

    protected Optional<Constructor<To>> findAnnotatedConstructor() throws Exception {
        List<Constructor<To>> annotatedConstructors = this.getAllConstructors().stream()
                .filter(c -> c.isAnnotationPresent(DependencyConstructor.class))
                .collect(Collectors.toList());

        if (annotatedConstructors.size() > 1) {
            throw new Exception("Only one @DependencyConstructor is allowed!");
        }

        if(annotatedConstructors.size() < 1) return Optional.empty();
        return Optional.ofNullable(annotatedConstructors.get(0));
    }

    private boolean hasNoCycle(List<Class<?>> dependent, List<Class<?>> parameters) {
        return parameters.stream().noneMatch(dependent::contains);
    }

    private boolean areAllParametersInContainer(List<Class<?>> parametersClasses) {
        for (Class<?> aClass : parametersClasses) {
            try {
                this.container.getCreator(aClass);
            } catch (Exception e) {
                return false;
            }
        }
        return true;
    }

    protected Optional<Constructor<To>> findBestMatchConstructor(List<Class<?>> dependent) {
        List<Constructor<To>> constructorList = null;
        try {
            constructorList = findMaxArgsConstructors();

            Constructor<To> constructor = constructorList.stream()
                    .filter(c -> hasNoCycle(dependent, Arrays.asList(c.getParameterTypes()))
                            && areAllParametersInContainer(Arrays.asList(c.getParameterTypes())))
                    .findFirst().orElse(null);
            return Optional.ofNullable(constructor);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return Optional.empty();
    }

    protected To createObject(List<Class<?>> dependent) {
        Constructor<To> constructor = null;
        try {
            constructor = findAnnotatedConstructor()
                    .orElse(findBestMatchConstructor(dependent)
                            .orElseThrow(() -> new Error("No valid constructor found for " + toClass))
                    );
        } catch (Exception e) {
            e.printStackTrace();
        }

        List<Class<?>> updatedDependent = new ArrayList<>(dependent);
        updatedDependent.add(this.toClass);

        try {
            return constructor
                    .newInstance(
                            Arrays.stream(constructor.getParameterTypes())
                                    .map(aClass -> {
                                        return ((AbstractCreator<?>)this.container.getCreator(aClass)).createObject(updatedDependent);
                                    })
                                    .toArray()
                    );
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }
}
