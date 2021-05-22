package org.mateusz.ioc;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

public class PrototypeCreator<To> extends AbstractCreator<To> {

    public PrototypeCreator(Class<To> classObject) {
        super(classObject);
    }


    @Override
    public To create() {
        Constructor<To> constructor = super.findNoArgConstructor();

        try {
            return (To) constructor.newInstance();
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException e) {
            e.printStackTrace();
        }

        // Shouldn't reach
        return null;
    }
}
