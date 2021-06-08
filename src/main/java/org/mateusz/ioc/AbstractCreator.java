package org.mateusz.ioc;

import java.lang.reflect.Constructor;
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

    protected Optional<Constructor<To>> findDependencyConstructor() throws Exception {
        List<Constructor<To>> annotatedConstructors = this.getAllConstructors().stream()
                .filter(c -> c.isAnnotationPresent(DependencyConstructor.class))
                .collect(Collectors.toList());

        if (annotatedConstructors.size() > 1) {
            throw new Exception("Only one @DependencyConstructor is allowed!");
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

    protected Constructor<To> findBestMatchConstructor(List<Class<?>> dependent) throws Exception {
        List<Constructor<To>> constructorList;
        constructorList = findMaxArgsConstructors();

        if (constructorList.size() > 1) {
            throw new Exception("More than one constructor is suitable for creating an object of type " + this.toClass);
        }

        if (constructorList.size() < 1) {
            throw new Exception("No constructor found in " + this.toClass);
        }

        //
//        if (!hasNoCycle(dependent, Arrays.asList(constructor.getParameterTypes()))) {
//            throw new Exception("Dependencies cycle detected in " + this.toClass);
//        }

        return constructorList.get(0);
    }

    private Object createObjectFromContainer(Class<?> aClass, List<Class<?>> updatedDependent) throws Exception {
        Optional<ICreator<?>> optionalICreator = this.container.getCreator(aClass);
        ICreator<?> creator = optionalICreator.orElseThrow(() -> new Exception(aClass + " not found in the given container!"));
        return ((AbstractCreator<?>) creator).createObject(updatedDependent);
    }

    private List<Object> constructParametersForSetter(Method setter, List<Class<?>> dependencies) throws Exception {
//        if(!hasNoCycle(dependencies, Arrays.asList(setter.getParameterTypes()))) {
//            throw new Exception("Dependencies cycle detected in " + this.toClass + " in @DependencyMethod method " + setter.getName());
//        }

        List<Object> list = new ArrayList<>();
        for (Class<?> aClass : setter.getParameterTypes()) {
            Object objectFromContainer = this.createObjectFromContainer(aClass, dependencies);
            list.add(objectFromContainer);
        }
        return list;
    }

    protected To createObject(List<Class<?>> dependent) throws Exception {
        if(!hasNoCycle(dependent, Collections.singletonList(toClass))) {
            throw new Exception("Cycle detected in " + toClass);
        }

        Constructor<To> constructor = null;
        try {
            constructor = findDependencyConstructor().orElse(findBestMatchConstructor(dependent));
        } catch (Exception e) {
            e.printStackTrace();
            throw new Exception("Couldn't create an object of type " + this.toClass);
        }

        List<Class<?>> updatedDependent = new ArrayList<>(dependent);
        updatedDependent.add(this.toClass);

        try {
             final To instance = constructor
                    .newInstance(
                            Arrays.stream(constructor.getParameterTypes())
                                    .map(aClass -> {
                                        try {
                                            return createObjectFromContainer(aClass, updatedDependent);
                                        } catch (Exception e) {
                                            e.printStackTrace();
                                        }

                                        // shouldn't reach
                                        return null;
                                    })
                                    .toArray()
                    );

            for(Method setter : findDependencyMethodSetters()) {
                List<Object> parameters = constructParametersForSetter(setter, updatedDependent);
                setter.invoke(instance, parameters.toArray());
            }

            return instance;
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }
}
