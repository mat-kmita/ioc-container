package org.mateusz.ioc;

import java.util.ArrayList;

public class PrototypeCreator<To> extends AbstractCreator<To> {

    public PrototypeCreator(Class<To> classObject, Container container) {
        super(classObject, container);
    }

    @Override
    public To create() {
        return super.createObject(new ArrayList<>());
    }
}
