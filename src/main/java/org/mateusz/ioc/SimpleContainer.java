package org.mateusz.ioc;

import java.util.HashMap;
import java.util.Map;

public class SimpleContainer {

    private Map<Class, ICreator> registeredTypes;

    public SimpleContainer() {
        this.registeredTypes = new HashMap<>();
    }

    public <T> void registerType(Class<T> type, boolean isSingleton) {
        registeredTypes.remove(type);

        if(isSingleton) {
            registeredTypes.put(type, new SingletonCreator<>(type));
        } else {
            registeredTypes.put(type, new PrototypeCreator<>(type));
        }
    }

    public <T, U extends T> void registerType(Class<T> from, Class<U> to, boolean isSingleton) {
        registeredTypes.remove(from);

        if(isSingleton) {
            registeredTypes.put(from, new SingletonCreator<>(to));
        } else {
            registeredTypes.put(from, new PrototypeCreator<>(to));
        }
    }

    public <T> T resolve(Class type) {
        System.out.println("Resolving: " + type);

        if(!registeredTypes.containsKey(type)) {
            throw new IllegalArgumentException("Invalid type " + type);
        }

        return (T) registeredTypes.get(type).create();
    }

}
