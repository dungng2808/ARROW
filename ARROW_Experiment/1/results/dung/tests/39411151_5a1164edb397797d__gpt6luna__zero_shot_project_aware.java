package foundation.stack.datamill.configuration;

import foundation.stack.datamill.values.Value;
import org.junit.Test;
import rx.functions.Func1;

import java.util.Arrays;
import java.util.Collections;
import java.util.Optional;

import static org.junit.Assert.*;

public class Wiring_ArrowTest {
    public static class NoArgType {
        public NoArgType() {
        }
    }

    @Test
    public void defaultConstructorProvidesConcreteClassFactory() {
        Wiring wiring = new Wiring();

        assertNotNull(wiring.getFactoryChain());
        assertNotNull(wiring.newInstance(NoArgType.class));
    }

    @Test
    public void getNamedReturnsEmptyWhenNoPropertySourceIsConfigured() {
        Wiring wiring = new Wiring();

        assertEquals(Optional.empty(), wiring.getNamed("missing"));
    }

    @Test(expected = IllegalStateException.class)
    public void getRequiredNamedThrowsWhenPropertyIsMissing() {
        new Wiring().getRequiredNamed("missing");
    }

    @Test
    public void newInstanceCreatesDistinctInstances() {
        Wiring wiring = new Wiring();

        NoArgType first = wiring.newInstance(NoArgType.class);
        NoArgType second = wiring.newInstance(NoArgType.class);

        assertNotSame(first, second);
    }

    @Test
    public void singletonReturnsSameInstance() {
        Wiring wiring = new Wiring();

        NoArgType first = wiring.singleton(NoArgType.class);
        NoArgType second = wiring.singleton(NoArgType.class);

        assertSame(first, second);
    }

    @Test
    public void defaultScopedUsesDefaultSingletonScope() {
        Wiring wiring = new Wiring();

        NoArgType first = wiring.defaultScoped(NoArgType.class);
        NoArgType second = wiring.defaultScoped(NoArgType.class);

        assertSame(first, second);
    }

    @Test
    public void withConsumerReturnsConsumerResult() {
        Wiring wiring = new Wiring();
        String expected = "result";

        String actual = wiring.with(new Func1<Wiring, String>() {
            @Override
            public String call(Wiring supplied) {
                return "result";
            }
        });

        assertEquals(expected, actual);
    }

    @Test
    public void withPropertySourceAndFactoryChainPassesConfiguredWiringToConsumer() {
        Wiring wiring = new Wiring();
        FactoryChain chain = wiring.getFactoryChain();

        FactoryChain actual = wiring.with(null, chain, new Func1<Wiring, FactoryChain>() {
            @Override
            public FactoryChain call(Wiring supplied) {
                return supplied.getFactoryChain();
            }
        });

        assertSame(chain, actual);
    }

    @Test
    public void withFactoryChainPassesConfiguredChainToConsumer() {
        Wiring wiring = new Wiring();
        FactoryChain chain = wiring.getFactoryChain();

        FactoryChain actual = wiring.with(chain, new Func1<Wiring, FactoryChain>() {
            @Override
            public FactoryChain call(Wiring supplied) {
                return supplied.getFactoryChain();
            }
        });

        assertSame(chain, actual);
    }

    @Test
    public void withPropertySourcePassesExistingFactoryChainToConsumer() {
        Wiring wiring = new Wiring();
        FactoryChain chain = wiring.getFactoryChain();

        FactoryChain actual = wiring.with((PropertySource) null, new Func1<Wiring, FactoryChain>() {
            @Override
            public FactoryChain call(Wiring supplied) {
                return supplied.getFactoryChain();
            }
        });

        assertSame(chain, actual);
    }
}
