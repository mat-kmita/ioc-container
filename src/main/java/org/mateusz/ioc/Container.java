package org.mateusz.ioc;

public interface Container {
    <T> ICreator<T> getCreator(Class<T> type);
}
