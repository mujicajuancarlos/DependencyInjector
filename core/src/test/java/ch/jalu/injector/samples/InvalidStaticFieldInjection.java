package ch.jalu.injector.samples;

import javax.inject.Inject;

/**
 * Sample class - attempted field injection on a static member.
 */
public class InvalidStaticFieldInjection {

    @Inject
    private ProvidedClass providedClass;
    @Inject
    protected static AlphaService alphaService;

    InvalidStaticFieldInjection() { }

}