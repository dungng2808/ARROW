package foundation.stack.datamill.configuration;

import foundation.stack.datamill.values.Value;
import org.junit.Test;
import rx.functions.Func1;

import java.util.Arrays;
import java.util.Collections;
import java.util.Optional;

import static org.junit.Assert.*;

public class Wiring_ArrowTest {
    public static class SimpleType {
        public SimpleType() {
        }
    }

    @Test
    public void getFactoryChainReturnsConfiguredChain() {
        FactoryChain chain = FactoryChains.forAnyConcreteClass();
        Wiring wiring = new Wiring(chain);

        assertSame(chain, wiring.getFactoryChain());
    }

    @Test
    public void getNamedWithoutPropertySourceIsEmpty() {
        Wiring wiring = new Wiring();

        assertEquals(Optional.empty(), wiring.getNamed("missing"));
    }

    @Test
    public void getRequiredNamedWithoutPropertySourceThrows() {
        Wiring wiring = new Wiring();

        try {
            wiring.getRequiredNamed("missing");
            fail("Expected an IllegalStateException");
        } catch (IllegalStateException expected) {
            assertEquals("Required named property [missing] not present!", expected.getMessage());
        }
    }

    @Test
    public void newInstanceCreatesConcreteType() {
        Wiring wiring = new Wiring();

        assertNotNull(wiring.newInstance(SimpleType.class));
    }

    @Test
    public void newInstanceCreatesDifferentInstances() {
        Wiring wiring = new Wiring();

        SimpleType first = wiring.newInstance(SimpleType.class);
        SimpleType second = wiring.newInstance(SimpleType.class);

        assertNotSame(first, second);
    }

    @Test
    public void singletonReturnsSameInstance() {
        Wiring wiring = new Wiring();

        SimpleType first = wiring.singleton(SimpleType.class);
        SimpleType second = wiring.singleton(SimpleType.class);

        assertSame(first, second);
    }

    @Test
    public void defaultScopedUsesDefaultSingletonScope() {
        Wiring wiring = new Wiring();

        SimpleType first = wiring.defaultScoped(SimpleType.class);
        SimpleType second = wiring.defaultScoped(SimpleType.class);

        assertSame(first, second);
    }

    @Test
    public void withConsumerUsesCurrentWiring() {
        final Wiring wiring = new Wiring();

        String result = wiring.with(new Func1<Wiring, String>() {
            @Override
            public String call(Wiring supplied) {
                return supplied == wiring ? "same" : "different";
            }
        });

        assertEquals("same", result);
    }

    @Test
    public void withPropertySourceAndFactoryChainUsesProvidedWiring() {
        final FactoryChain chain = FactoryChains.forAnyConcreteClass();
        Wiring wiring = new Wiring();

        FactoryChain result = wiring.with(null, chain, new Func1<Wiring, FactoryChain>() {
            @Override
            public FactoryChain call(Wiring supplied) {
                return supplied.getFactoryChain();
            }
        });

        assertSame(chain, result);
    }

    @Test
    public void withFactoryChainUsesProvidedFactoryChain() {
        final FactoryChain chain = FactoryChains.forAnyConcreteClass();
        Wiring wiring = new Wiring();

        FactoryChain result = wiring.with(chain, new Func1<Wiring, FactoryChain>() {
            @Override
            public FactoryChain call(Wiring supplied) {
                return supplied.getFactoryChain();
            }
        });

        assertSame(chain, result);
    }

    @Test
    public void withPropertySourceUsesExistingFactoryChain() {
        final FactoryChain chain = FactoryChains.forAnyConcreteClass();
        Wiring wiring = new Wiring(chain);

        FactoryChain result = wiring.with((PropertySource) null, new Func1<Wiring, FactoryChain>() {
            @Override
            public FactoryChain call(Wiring supplied) {
                return supplied.getFactoryChain();
            }
        });

        assertSame(chain, result);
    }
}
