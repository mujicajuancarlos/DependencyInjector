package ch.jalu.injector.handlers.instantiation;

import ch.jalu.injector.handlers.Handler;

import javax.annotation.Nullable;

/**
 * Provides an {@link Instantiation} for classes when applicable.
 */
public interface InstantiationProvider extends Handler {

    /**
     * Provides an instantiation method for the given class if available.
     *
     * @param clazz the class to process
     * @param <T> the class' type
     * @return the instantiation for the class, or {@code null} if not possible
     */
    @Nullable
    <T> Instantiation<T> get(Class<T> clazz);

}