package org.mateusz.ioc;

import java.util.Optional;

public interface Container {
    Optional<ICreator<?>> getCreator(Class<?> type);
}
