package ch.jalu.injector.handlers.dependency;

import ch.jalu.injector.context.UnresolvedContext;
import ch.jalu.injector.exceptions.InjectorException;
import ch.jalu.injector.handlers.Handler;
import ch.jalu.injector.handlers.instantiation.Instantiation;
import ch.jalu.injector.handlers.instantiation.SimpleObjectResolution;
import ch.jalu.injector.utils.InjectorUtils;

import java.lang.annotation.Annotation;
import java.util.HashMap;
import java.util.Map;

/**
 * Simple handler that allows you to save a value for a custom annotation. Use this
 * if you have a handful of custom values you want to be able to use as dependency.
 * Create custom annotations to identify them by and register them before using the
 * injector.
 * <p>
 * Don't forget that annotations need to have their {@link java.lang.annotation.Retention retention}
 * set to runtime in order to be visible.
 */
public class SavedAnnotationsHandler implements Handler {

    private Map<Class<?>, Object> storedValues = new HashMap<>();

    @Override
    public Instantiation<?> get(UnresolvedContext context) {
        Object o;
        for (Annotation annotation : context.getIdentifier().getAnnotations()) {
            if ((o = storedValues.get(annotation.annotationType())) != null) {
                return new SimpleObjectResolution<>(o);
            }
        }
        return null;
    }

    @Override
    public void onAnnotation(Class<? extends Annotation> annotation, Object object) {
        InjectorUtils.checkNotNull(object, "Object may not be null");
        if (storedValues.containsKey(annotation)) {
            throw new InjectorException("Value already registered for @" + annotation.getSimpleName());
        }
        storedValues.put(annotation, object);
    }
}
