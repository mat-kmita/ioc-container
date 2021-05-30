package org.mateusz.ioc;

public class InstanceCreator<To> extends AbstractCreator<To> {
    private To object;

    public InstanceCreator(To object, Class<To> classObject) {
        super(classObject);
        this.object = object;
    }

    @Override
    public To create() {
        return object;
    }
}
