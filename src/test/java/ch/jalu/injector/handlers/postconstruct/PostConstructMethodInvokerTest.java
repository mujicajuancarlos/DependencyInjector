package ch.jalu.injector.handlers.postconstruct;

import ch.jalu.injector.TestUtils.ExceptionCatcher;
import ch.jalu.injector.samples.BetaManager;
import ch.jalu.injector.samples.PostConstructTestClass;
import ch.jalu.injector.samples.ProvidedClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import javax.annotation.PostConstruct;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

/**
 * Test for {@link PostConstructMethodInvoker}.
 */
public class PostConstructMethodInvokerTest {

    private PostConstructMethodInvoker postConstructInvoker = new PostConstructMethodInvoker();

    @Rule
    public ExpectedException expectedException = ExpectedException.none();
    private ExceptionCatcher exceptionCatcher = new ExceptionCatcher(expectedException);

    @Test
    public void shouldExecutePostConstructMethod() {
        // given
        PostConstructTestClass testClass = new PostConstructTestClass(123, new ProvidedClass(""));

        // when
        postConstructInvoker.process(testClass);

        // then
        assertThat(testClass.wasPostConstructCalled(), equalTo(true));
    }

    @Test
    public void shouldThrowForInvalidPostConstructMethod() {
        // given
        WithParams withParams = new WithParams();

        // expect
        exceptionCatcher.expect("@PostConstruct method may not be static or have any parameters",
            WithParams.class);

        // when
        postConstructInvoker.process(withParams);
    }

    @Test
    public void shouldThrowForStaticPostConstructMethod() {
        // given
        Static classWithStaticMethod = new Static();

        // expect
        exceptionCatcher.expect("@PostConstruct method may not be static or have any parameters",
            Static.class);

        // when
        postConstructInvoker.process(classWithStaticMethod);
    }

    @Test
    public void shouldForwardExceptionFromPostConstruct() {
        // given
        ThrowsException throwsException = new ThrowsException();

        // expect
        exceptionCatcher.expect("Could not invoke method", ThrowsException.class);

        // when
        postConstructInvoker.process(throwsException);
    }

    @Test
    public void shouldThrowForMultiplePostConstructMethods() {
        // given
        MultiplePostConstructs multiplePostConstructs =
            new MultiplePostConstructs();

        // expect
        exceptionCatcher.expect("Multiple methods with @PostConstruct",
            MultiplePostConstructs.class);

        // when
        postConstructInvoker.process(multiplePostConstructs);
    }

    @Test
    public void shouldThrowForPostConstructNotReturningVoid() {
        // given
        NotVoidReturnType notVoidReturnType = new NotVoidReturnType();

        // expect
        exceptionCatcher.expect("@PostConstruct method must have return type void",
            NotVoidReturnType.class);

        // when
        postConstructInvoker.process(notVoidReturnType);
    }
    
    
    // ---------------
    // Test classes
    // ---------------
    public static final class WithParams {
        @PostConstruct
        public void invalidPostConstr(BetaManager betaManager) {
        }
    }

    public static final class Static {
        @PostConstruct
        public static void invalidMethod() {
            // --
        }
    }

    public static final class ThrowsException {
        @PostConstruct
        public void throwingPostConstruct() {
            throw new IllegalStateException("Exception in post construct");
        }
    }

    public static final class NotVoidReturnType {
        @PostConstruct
        public int returnsInt() {
            return 42;
        }
    }

    public static final class MultiplePostConstructs {
        @PostConstruct
        public void postConstruct1() {
            // --
        }
        @PostConstruct
        public void postConstruct2() {
            // --
        }
    }
}