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
        this.createObject(new ArrayList<>());

        return (To) instance;
    }

    @Override
    protected To createObject(List<Class<?>> dependent) {
        synchronized (this) {
            if (instance == null) {
                instance = super.createObject(dependent);
            }
        }

        return (To) instance;
    }
}
