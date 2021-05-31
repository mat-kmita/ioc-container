package org.mateusz.ioc;

import java.util.ArrayList;
import java.util.List;

public class SingletonCreator<To> extends AbstractCreator<To> {

    private static Object instance = null;

    public SingletonCreator(Class<To> classObject, Container container) {
        super(classObject, container);
    }

    @Override
    public To create() {
        try {
            this.createObject(new ArrayList<>());
            return (To) instance;
        } catch (Exception e) {
            e.printStackTrace();
            throw new Error("Error while creating an object of class " + super.toClass);
        }
    }

    @Override
    protected To createObject(List<Class<?>> dependent) throws Exception {
        synchronized (this) {
            if (instance == null) {
                instance = super.createObject(dependent);
            }
        }

        return (To) instance;
    }
}
