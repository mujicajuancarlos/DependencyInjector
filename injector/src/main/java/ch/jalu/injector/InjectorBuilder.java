package ch.jalu.injector;

import ch.jalu.injector.exceptions.InjectorException;
import ch.jalu.injector.handlers.Handler;
import ch.jalu.injector.handlers.annotationvalues.AnnotationValueHandler;
import ch.jalu.injector.handlers.dependency.DependencyHandler;
import ch.jalu.injector.handlers.dependency.FactoryDependencyHandler;
import ch.jalu.injector.handlers.dependency.SavedAnnotationsHandler;
import ch.jalu.injector.handlers.dependency.SingletonStoreDependencyHandler;
import ch.jalu.injector.handlers.instantiation.DefaultInjectionProvider;
import ch.jalu.injector.handlers.instantiation.InstantiationProvider;
import ch.jalu.injector.handlers.postconstruct.PostConstructHandler;
import ch.jalu.injector.handlers.postconstruct.PostConstructMethodInvoker;
import ch.jalu.injector.handlers.preconstruct.PreConstructHandler;
import ch.jalu.injector.handlers.preconstruct.PreConstructPackageValidator;
import ch.jalu.injector.handlers.provider.ProviderHandler;
import ch.jalu.injector.handlers.provider.ProviderHandlerImpl;
import ch.jalu.injector.utils.InjectorUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

/**
 * Configures and creates an {@link Injector}.
 */
public class InjectorBuilder {

    private InjectorConfig config;

    /**
     * Creates a new builder.
     *
     * @since 0.1
     */
    public InjectorBuilder() {
        config = new InjectorConfig();
    }

    /**
     * Returns all handlers that are added to the injector by default.
     *
     * @param rootPackage the root package of the project (to limit injection and scanning to)
     * @return all default handlers
     * @see #addDefaultHandlers(String)
     * @since 0.1
     */
    public static List<Handler> createDefaultHandlers(String rootPackage) {
        InjectorUtils.checkNotNull(rootPackage, "root package may not be null");
        return new ArrayList<>(Arrays.asList(
            // PreConstruct
            new PreConstructPackageValidator(rootPackage),
            // (Annotation, Object) handler
            new SavedAnnotationsHandler(),
            // Provider / Factory / SingletonStore
            new ProviderHandlerImpl(),
            new FactoryDependencyHandler(),
            new SingletonStoreDependencyHandler(),
            // Instantiation provider
            new DefaultInjectionProvider(),
            // PostConstruct
            new PostConstructMethodInvoker()));
    }

    /**
     * Creates all default handlers of type {@link InstantiationProvider}. Useful if you want to create your own
     * preconstruct (etc.) handlers but want to use the default instantiation providers.
     * <p>
     * Use {@link #createDefaultHandlers(String)} or {@link #addDefaultHandlers(String)} otherwise.
     *
     * @return default instantiation providers
     * @since 0.1
     */
    public static List<InstantiationProvider> createInstantiationProviders() {
        return new ArrayList<>(Arrays.asList(
            new ProviderHandlerImpl(),
            new DefaultInjectionProvider()));
    }

    /**
     * Convenience method for adding all default handlers to the injector configuration.
     * To obtain an injector with all defaults, you can simply do:
     * <code>
     *   Injector injector = new InjectorBuilder().addDefaultHandlers("your.package.here").create();
     * </code>
     *
     * @param rootPackage the root package of the project
     * @return the builder
     * @since 0.1
     */
    public InjectorBuilder addDefaultHandlers(String rootPackage) {
        return addHandlers(createDefaultHandlers(rootPackage));
    }

    /**
     * Add handlers to the config. Note that <b>the order of the handlers matters.</b> Handlers are
     * separated by their subtype and then executed in the order as provided.
     *
     * @param handlers the handlers to add to the injector
     * @return the builder
     * @since 0.1
     */
    public InjectorBuilder addHandlers(Handler... handlers) {
        return addHandlers(Arrays.asList(handlers));
    }

    /**
     * Add handlers to the config. Note that <b>the order of the handlers matters.</b> Handlers are
     * separated by their subtype and then executed in the order as provided.
     *
     * @param handlers the handlers to add to the injector
     * @return the builder
     * @since 0.1
     */
    public InjectorBuilder addHandlers(Iterable<? extends Handler> handlers) {
        new HandlerCollector()
            .registerType(AnnotationValueHandler.class, config::addAnnotationValueHandlers)
            .registerType(ProviderHandler.class, config::addProviderHandlers)
            .registerType(PreConstructHandler.class, config::addPreConstructHandlers)
            .registerType(InstantiationProvider.class, config::addInstantiationProviders)
            .registerType(DependencyHandler.class, config::addDependencyHandlers)
            .registerType(PostConstructHandler.class, config::addPostConstructHandlers)
        .process(handlers);
        return this;
    }

    /**
     * Creates an injector with the configurations set to the builder.
     *
     * @return the injector
     * @since 0.1
     */
    public Injector create() {
        return new InjectorImpl(config);
    }

    @SuppressWarnings("unchecked")
    private static final class HandlerCollector {

        private final Map<Class, List> handlersByType = new HashMap<>();
        private final Map<Class, Consumer<List>> handlerListSetters = new HashMap<>();

        <T extends Handler> HandlerCollector registerType(Class<T> subType, Consumer<List<T>> handlerListSetter) {
            if (handlersByType.containsKey(subType)) {
                throw new IllegalStateException("Already provided " + subType);
            }
            handlersByType.put(subType, new ArrayList<>());
            handlerListSetters.put(subType, (Consumer) handlerListSetter);
            return this;
        }

        void process(Iterable<? extends Handler> handlers) {
            for (Handler handler : handlers) {
                process(handler);
            }
            for (Map.Entry<Class, Consumer<List>> listSetter : handlerListSetters.entrySet()) {
                listSetter.getValue().accept(
                    handlersByType.get(listSetter.getKey()));
            }
        }

        private void process(Handler handler) {
            boolean foundSubtype = false;
            for (Class subtype : handlersByType.keySet()) {
                foundSubtype |= addHandler(subtype, handler);
            }
            if (!foundSubtype) {
                throw new InjectorException(String.format("Class '%s' must extend a known Handler subtype",
                    handler.getClass().getName()));
            }
        }

        private boolean addHandler(Class clazz, Handler handler) {
            if (clazz.isInstance(handler)) {
                handlersByType.get(clazz).add(handler);
                return true;
            }
            return false;
        }
    }

}