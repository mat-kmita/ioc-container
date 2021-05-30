package org.mateusz.ioc;

import java.util.List;

public class InstanceCreator<To> extends AbstractCreator<To> {
    private To object;

    public InstanceCreator(To object, Class<To> classObject, Container container) {
        super(classObject, container);
        this.object = object;
    }

    @Override
    public To create() {
        return object;
    }

    @Override
    protected To createObject(List<Class<?>> dependent) {
        return this.object;
    }
}
