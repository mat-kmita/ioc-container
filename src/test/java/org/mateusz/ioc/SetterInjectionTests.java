package org.mateusz.ioc;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class SetterInjectionTests {

    @Test
    public void shouldInjectDependencyUsingSetter() {
        SimpleContainer c = new SimpleContainer();
        c.registerType(SettersA.class, false);
        c.registerType(SettersB.class, false);
        c.registerType(SettersC.class, false);
        SettersA a = c.resolve(SettersA.class);

        assertNotNull(a);
        assertNotNull(a.getB());
        assertNotNull(a.getC());
    }

    @Test
    public void shouldDetectCycleWhenUsingSetterInjection() {
        SimpleContainer c = new SimpleContainer();

        c.registerType(SettersD.class, false);
        c.registerType(SettersE.class, false);
        assertThrows(Exception.class, () -> c.resolve(SettersD.class));
    }

    @Test
    public void shouldNotUseNotAnnotatedSetter() {
//        SimpleContainer c = new SimpleContainer();
//
//        c.registerType();
//
//        assertNull();
    }
}

class SettersD {
    private SettersE e;

    public SettersD() {
    }

    public SettersE getE() {
        return e;
    }

    @DependencyMethod
    public void setE(SettersE e) {
        this.e = e;
    }
}

class SettersE {
    private SettersD d;

    public SettersE() {
    }

    public SettersD getD() {
        return d;
    }

    @DependencyMethod
    public void setD(SettersD d) {
        this.d = d;
    }
}

class SettersB {
    public SettersB() { }
}


class SettersC {
    public SettersC() { }
}


class SettersA {
    private SettersB b;
    private SettersC c;

    public SettersA(SettersC c) {
        this.c = c;
    }

    @DependencyMethod
    public void setB(SettersB b) {
        this.b = b;
    }

    public SettersB getB() {
        return b;
    }

    public SettersC getC() {
        return c;
    }
}

