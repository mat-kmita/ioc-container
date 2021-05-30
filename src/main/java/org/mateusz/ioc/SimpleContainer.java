package org.mateusz.ioc;

import java.util.HashMap;
import java.util.Map;

public class SimpleContainer implements Container {

    private Map<Class, ICreator> registeredTypes;

    public SimpleContainer() {
        this.registeredTypes = new HashMap<>();
    }

    public <T> void registerType(Class<T> type, boolean isSingleton) {
        registeredTypes.remove(type);

        if(isSingleton) {
            registeredTypes.put(type, new SingletonCreator<>(type, this));
        } else {
            registeredTypes.put(type, new PrototypeCreator<>(type, this));
        }
    }

    public <T, U extends T> void registerType(Class<T> from, Class<U> to, boolean isSingleton) {
        registeredTypes.remove(from);

        if(isSingleton) {
            registeredTypes.put(from, new SingletonCreator<>(to, this));
        } else {
            registeredTypes.put(from, new PrototypeCreator<>(to, this));
        }
    }

    public <T> void registerInstance(T instance) {
        registeredTypes.remove(instance.getClass());

        registeredTypes.put(instance.getClass(), new InstanceCreator<T>(instance, (Class<T>) instance.getClass(), this));
    }

    public <T> T resolve(Class type) {
        System.out.println("Resolving: " + type);

        if(!registeredTypes.containsKey(type)) {
            throw new IllegalArgumentException("Invalid type " + type);
        }

        return (T) registeredTypes.get(type).create();
    }


    @Override
    public <T> ICreator<T> getCreator(Class<T> type) {
        if(!this.registeredTypes.containsKey(type)) throw new Error("Type not found in the container: " + type);

        return this.registeredTypes.get(type);
    }
}
