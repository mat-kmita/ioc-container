package org.mateusz.ioc;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

public class PrototypeCreator<To> extends AbstractCreator<To> {

    public PrototypeCreator(Class<To> classObject) {
        super(classObject);
    }


    @Override
    public To create() {
        Constructor<To> constructor;
        try {
            constructor = super.findNoArgConstructor();
        } catch (Exception e) {
            throw new Error("Couldn't construct object of type " + toClass.getName());
        }

        try {
            return constructor.newInstance();
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException e) {
            e.printStackTrace();
        }

        // Shouldn't reach
        return null;
    }
}
