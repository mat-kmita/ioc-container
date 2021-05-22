package org.mateusz.ioc;

import java.lang.reflect.Constructor;
import java.util.Arrays;
import java.util.Comparator;

public abstract class AbstractCreator<To> implements ICreator<To> {

    protected Class<To> toClass;

    public AbstractCreator(Class<To> classObject) {
        this.toClass = classObject;
    }

    protected Constructor<To> findNoArgConstructor() {
        Constructor<To> constructor = (Constructor<To>) Arrays.stream(toClass.getConstructors()).
                min(Comparator.comparingInt(Constructor::getParameterCount))
                .orElseThrow(IllegalAccessError::new);

        if(constructor.getParameterCount() != 0) {
            throw new IllegalArgumentException("There is no 0 argument exception!");
        }

        return constructor;
    }

}
