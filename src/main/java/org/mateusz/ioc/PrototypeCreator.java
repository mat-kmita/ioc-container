package org.mateusz.ioc;

import java.util.ArrayList;

public class PrototypeCreator<To> extends AbstractCreator<To> {

    public PrototypeCreator(Class<To> classObject, Container container) {
        super(classObject, container);
    }

    @Override
    public To create() {
        try {
            return super.createObject(new ArrayList<>());
        } catch (Exception e) {
            e.printStackTrace();
            throw new Error("Error while creating an object of class " + this.toClass);
        }
    }
}
