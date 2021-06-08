package org.mateusz.ioc;

import org.junit.jupiter.api.Test;
import org.mateusz.ioc.exceptions.NoConstructorInBeanException;
import org.mateusz.ioc.exceptions.NotInContainerException;
import org.mateusz.ioc.exceptions.ObjectCreationException;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit test.
 */
public class AppTest 
{
    @Test
    public void shouldReturnObjectOfCorrectType()
    {
        SimpleContainer container = new SimpleContainer();
        container.registerType(A.class, false);

        assertTrue(container.resolve(A.class) instanceof A);
    }

    @Test
    public void shouldReturnSubclassOfCorrectType() {
        SimpleContainer container = new SimpleContainer();
        container.registerType(A.class, B.class, false);

        assertTrue(container.resolve(A.class) instanceof B);
    }

    @Test
    public void shouldReturnUpdatedType() {
        SimpleContainer container = new SimpleContainer();
        container.registerType(A.class, B.class, false);
        container.registerType(A.class, C.class, false);
        assertTrue(container.resolve(A.class) instanceof C);
    }

    @Test
    public void shouldReturnDifferentObjectsForPrototypePolicy() {
        SimpleContainer container = new SimpleContainer();
        container.registerType(A.class, B.class, false);
        assertNotSame(container.resolve(A.class), container.resolve(A.class));
    }

    @Test
    public void shouldReturnTheSameObjectForSingletonPolicy() {
        SimpleContainer container = new SimpleContainer();
        container.registerType(A.class, B.class, true);
        assertSame(container.resolve(A.class), container.resolve(A.class));
    }

    @Test
    public void shouldThrowExceptionForUnregisteredType() {
        SimpleContainer container = new SimpleContainer();
        assertThrows(NotInContainerException.class, () -> container.resolve(A.class));
    }

    @Test
    public void shouldThrowErrorForNotFoundConstructor() {
        SimpleContainer container = new SimpleContainer();
        container.registerType(A.class, D.class, false);
        assertThrows(ObjectCreationException.class, () -> container.resolve(A.class));
    }
}

class A {
    public A() {
    }
}

class B extends A {
    public B() {
    }
}

class C extends A {
    public C() {
    }
}

class D extends A {

}
