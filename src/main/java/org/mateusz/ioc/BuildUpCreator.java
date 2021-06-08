package org.mateusz.ioc;

import org.mateusz.ioc.exceptions.setter_injection.NoArgumentsDependencyMethodException;
import org.mateusz.ioc.exceptions.setter_injection.NotVoidDependencyMethodException;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

public class BuildUpCreator<To> extends AbstractCreator<To> {

    private To instance;

    public BuildUpCreator(Class<To> classObject, Container container, To instance) {
        super(classObject, container);
        this.instance = instance;
    }

    @Override
    public To create() {
        List<Class<?>> dependencies = new ArrayList<>();
        dependencies.add(super.toClass);
        try {
            for(Method setter : findDependencyMethodSetters()) {
                List<Object> parameters = constructParametersForSetter(setter, dependencies);
                setter.invoke(instance, parameters.toArray());
            }

        } catch (NotVoidDependencyMethodException | NoArgumentsDependencyMethodException | IllegalAccessException |
                InvocationTargetException e) {
            e.printStackTrace();
        }

        return instance;
    }
}
