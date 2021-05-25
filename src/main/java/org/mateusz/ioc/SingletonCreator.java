package org.mateusz.ioc;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

public class SingletonCreator<To> extends AbstractCreator<To> {

    private static Object instance = null;

    public SingletonCreator(Class<To> classObject) {
        super(classObject);
    }

    @Override
    public To create() {
        synchronized (this) {
            if(instance == null) {

                Constructor<To> constructor;
                try {
                    constructor = super.findNoArgConstructor();
                } catch (Exception e) {
                    throw new Error("Couldn't construct object of type " + toClass.getName());
                }

                try {
                    instance = constructor.newInstance();
                } catch (InstantiationException | IllegalAccessException | InvocationTargetException e) {
                    e.printStackTrace();
                }
            }
        }

        return (To) instance;
    }
}
