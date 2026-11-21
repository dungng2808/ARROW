package foundation.stack.datamill.configuration;

import foundation.stack.datamill.values.Value;
import org.junit.Test;
import rx.functions.Func1;

import java.util.Arrays;
import java.util.Collections;
import java.util.Optional;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

public class Wiring_ArrowTest {

    public static class SimpleDependency {
        public SimpleDependency() {
        }
    }

    public static class Dependent {
        private final SimpleDependency dependency;

        public Dependent(SimpleDependency dependency) {
            this.dependency = dependency;
        }
    }

    @Test
    public void defaultConstructorProvidesConcreteClassFactory() {
        Wiring wiring = new Wiring();

        assertNotNull(wiring.getFactoryChain());
        assertNotNull(wiring.newInstance(SimpleDependency.class));
    }

    @Test
    public void newInstanceCreatesDistinctInstances() {
        Wiring wiring = new Wiring();

        SimpleDependency first = wiring.newInstance(SimpleDependency.class);
        SimpleDependency second = wiring.newInstance(SimpleDependency.class);

        assertNotSame(first, second);
    }

    @Test
    public void singletonReturnsSameInstance() {
        Wiring wiring = new Wiring();

        SimpleDependency first = wiring.singleton(SimpleDependency.class);
        SimpleDependency second = wiring.singleton(SimpleDependency.class);

        assertSame(first, second);
    }

    @Test
    public void defaultScopedUsesSingletonScopeByDefault() {
        Wiring wiring = new Wiring();

        SimpleDependency first = wiring.defaultScoped(SimpleDependency.class);
        SimpleDependency second = wiring.defaultScoped(SimpleDependency.class);

        assertSame(first, second);
    }

    @Test
    public void newInstanceConstructsDependencies() {
        Wiring wiring = new Wiring();

        Dependent dependent = wiring.newInstance(Dependent.class);

        assertNotNull(dependent.dependency);
    }

    @Test
    public void getNamedWithoutPropertySourceIsEmpty() {
        Wiring wiring = new Wiring();

        Optional<Value> value = wiring.getNamed("missing");

        assertFalse(value.isPresent());
    }

    @Test(expected = IllegalStateException.class)
    public void getRequiredNamedWithoutPropertySourceThrows() {
        new Wiring().getRequiredNamed("missing");
    }

    @Test
    public void withConsumerReturnsConsumerResult() {
        Wiring wiring = new Wiring();
        String result = wiring.with(new Func1<Wiring, String>() {
            @Override
            public String call(Wiring supplied) {
                return supplied == wiring ? "same" : "different";
            }
        });

        assertEquals("same", result);
    }

    @Test
    public void withFactoryChainUsesProvidedChain() {
        FactoryChain chain = FactoryChains.forAnyConcreteClass();
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
    public void withPropertySourceAndFactoryChainPassesWiringToConsumer() {
        FactoryChain chain = FactoryChains.forAnyConcreteClass();
        Wiring wiring = new Wiring();
        Wiring result = wiring.with(null, chain, new Func1<Wiring, Wiring>() {
            @Override
            public Wiring call(Wiring supplied) {
                return supplied;
            }
        });

        assertNotNull(result);
        assertSame(chain, result.getFactoryChain());
    }

    @Test
    public void newInstanceAcceptsQualifierCollection() {
        Wiring wiring = new Wiring();

        SimpleDependency dependency = wiring.newInstance(
                SimpleDependency.class, Arrays.asList("one", "two"));

        assertNotNull(dependency);
    }

    @Test
    public void singletonAcceptsEmptyQualifierCollection() {
        Wiring wiring = new Wiring();

        SimpleDependency dependency = wiring.singleton(
                SimpleDependency.class, Collections.<String>emptyList());

        assertNotNull(dependency);
    }
}
