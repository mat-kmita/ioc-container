package org.mateusz.ioc;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class BuildUpTests {

    @Test
    public void shouldInjectObjects() {
        SimpleContainer c = new SimpleContainer();
        c.registerType(BuildUpB.class, true);
        c.registerType(BuildUpC.class, false);
        c.registerType(BuildUpD.class, false);

        BuildUpB b = c.resolve(BuildUpB.class);

        BuildUpA a = new BuildUpA();

        assertNull(a.getB());

        c.buildUp(a);

        assertNotNull(a.getB());
        assertSame(b, a.getB());
    }
}


class BuildUpA {
    private BuildUpB b;

    public BuildUpA() {
    }

    public BuildUpB getB() {
        return b;
    }

    @DependencyMethod
    public void setB(BuildUpB b) {
        this.b = b;
    }
}

class BuildUpB {
    private BuildUpC c;
    private BuildUpD d;

    @DependencyConstructor
    public BuildUpB(BuildUpC c) {
        this.c = c;
    }


    public BuildUpB(BuildUpC c, BuildUpD d) {
        this.c = c;
        this.d = d;
    }

    public BuildUpC getC() {
        return c;
    }

    public void setC(BuildUpC c) {
        this.c = c;
    }

    public BuildUpD getD() {
        return d;
    }

    @DependencyMethod
    public void setD(BuildUpD d) {
        this.d = d;
    }
}

class BuildUpD {
    public BuildUpD() {
    }
}

class BuildUpC {
    public BuildUpC() {
    }
}


