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

    protected List<Constructor<To>> findMaxArgsConstructors() {
        int max = Arrays.stream(toClass.getConstructors()).
                mapToInt(Constructor::getParameterCount)
                .max().orElse(0);

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

        if (annotatedConstructors.size() < 1) return Optional.empty();
        return Optional.ofNullable(annotatedConstructors.get(0));
    }

    private boolean hasNoCycle(List<Class<?>> dependent, List<Class<?>> parameters) {
        return parameters.stream().noneMatch(dependent::contains);
    }

    protected Constructor<To> findBestMatchConstructor(List<Class<?>> dependent) throws Exception {
        List<Constructor<To>> constructorList;
        constructorList = findMaxArgsConstructors();

        if (constructorList.size() > 1) {
            throw new Exception("More than one constructor is suitable for creating an object of type " + this.toClass);
        }

        if (constructorList.size() < 1) {
            throw new Exception("No constructor found in " + this.toClass);
        }

        Constructor<To> constructor = constructorList.get(0);

        if (!hasNoCycle(dependent, Arrays.asList(constructor.getParameterTypes()))) {
            throw new Exception("Dependencies cycle detected in " + this.toClass);
        }

        return constructor;
    }

    private Object createObjectFromContainer(Class<?> aClass, List<Class<?>> updatedDependent) throws Exception {
        Optional<ICreator<?>> optionalICreator = this.container.getCreator(aClass);
        ICreator<?> creator = optionalICreator.orElseThrow(() -> new Exception(aClass + " not found in the given container!"));
        return ((AbstractCreator<?>) creator).createObject(updatedDependent);
    }

    protected To createObject(List<Class<?>> dependent) throws Exception {
        Constructor<To> constructor = null;
        try {
            constructor = findAnnotatedConstructor().orElse(findBestMatchConstructor(dependent));
        } catch (Exception e) {
            e.printStackTrace();
            throw new Exception("Couldn't create an object of type " + this.toClass);
        }

        List<Class<?>> updatedDependent = new ArrayList<>(dependent);
        updatedDependent.add(this.toClass);

        try {
            return constructor
                    .newInstance(
                            Arrays.stream(constructor.getParameterTypes())
                                    .map(aClass -> {
                                        try {
                                            return createObjectFromContainer(aClass, updatedDependent);
                                        } catch (Exception e) {
                                            e.printStackTrace();
                                        }

                                        return null;
                                    })
                                    .toArray()
                    );
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }
}
