package foundation.stack.datamill.configuration;

import foundation.stack.datamill.values.StringValue;
import foundation.stack.datamill.values.Value;
import org.junit.Test;
import rx.functions.Func1;

import java.util.*;

import static org.junit.Assert.*;

public class Wiring_ArrowTest {

    @Test
    public void testDefaultConstructor() {
        Wiring wiring = new Wiring();
        assertNotNull(wiring);
        assertNull(wiring.getFactoryChain());
    }

    @Test
    public void testConstructorWithPropertySource() {
        PropertySource propertySource = new PropertySource() {
            @Override
            public Optional<String> get(String property) {
                return Optional.empty();
            }
        };
        Wiring wiring = new Wiring(propertySource);
        assertNotNull(wiring);
        assertNotNull(wiring.getFactoryChain());
    }

    @Test
    public void testConstructorWithFactoryChain() {
        FactoryChain factoryChain = new FactoryChain() {
            @Override
            public Object call(Wiring wiring, Class type, Collection<String> qualifiers) {
                return null;
            }
        };
        Wiring wiring = new Wiring(factoryChain);
        assertNotNull(wiring);
        assertEquals(factoryChain, wiring.getFactoryChain());
    }

    @Test
    public void testConstructorWithPropertySourceAndFactoryChain() {
        PropertySource propertySource = new PropertySource() {
            @Override
            public Optional<String> get(String property) {
                return Optional.empty();
            }
        };
        FactoryChain factoryChain = new FactoryChain() {
            @Override
            public Object call(Wiring wiring, Class type, Collection<String> qualifiers) {
                return null;
            }
        };
        Wiring wiring = new Wiring(propertySource, factoryChain);
        assertNotNull(wiring);
        assertEquals(factoryChain, wiring.getFactoryChain());
    }

    @Test
    public void testConstructorWithPropertySourceAndFactoryChainAndScope() {
        PropertySource propertySource = new PropertySource() {
            @Override
            public Optional<String> get(String property) {
                return Optional.empty();
            }
        };
        FactoryChain factoryChain = new FactoryChain() {
            @Override
            public Object call(Wiring wiring, Class type, Collection<String> qualifiers) {
                return null;
            }
        };
        Scope scope = new Scope() {
            @Override
            public Object resolve(Wiring wiring, FactoryChain factoryChain, Class type, Collection<String> qualifiers) {
                return null;
            }
        };
        Wiring wiring = new Wiring(propertySource, factoryChain, scope);
        assertNotNull(wiring);
        assertEquals(factoryChain, wiring.getFactoryChain());
    }

    @Test
    public void testGetNamedReturnsEmptyWhenPropertySourceIsNull() {
        Wiring wiring = new Wiring();
        Optional<Value> result = wiring.getNamed("test");
        assertFalse(result.isPresent());
    }

    @Test
    public void testGetNamedReturnsValueWhenPropertySourceIsPresent() {
        PropertySource propertySource = new PropertySource() {
            @Override
            public Optional<String> get(String property) {
                if ("test".equals(property)) {
                    return Optional.of("value");
                }
                return Optional.empty();
            }
        };
        Wiring wiring = new Wiring(propertySource);
        Optional<Value> result = wiring.getNamed("test");
        assertTrue(result.isPresent());
        assertEquals("value", ((StringValue) result.get()).getString());
    }

    @Test
    public void testGetRequiredNamedThrowsExceptionWhenPropertyNotPresent() {
        PropertySource propertySource = new PropertySource() {
            @Override
            public Optional<String> get(String property) {
                return Optional.empty();
            }
        };
        Wiring wiring = new Wiring(propertySource);
        try {
            wiring.getRequiredNamed("test");
            fail("Expected IllegalStateException");
        } catch (IllegalStateException e) {
            assertEquals("Required named property [test] not present!", e.getMessage());
        }
    }

    @Test
    public void testGetRequiredNamedReturnsValueWhenPropertyPresent() {
        PropertySource propertySource = new PropertySource() {
            @Override
            public Optional<String> get(String property) {
                if ("test".equals(property)) {
                    return Optional.of("value");
                }
                return Optional.empty();
            }
        };
        Wiring wiring = new Wiring(propertySource);
        Value result = wiring.getRequiredNamed("test");
        assertEquals("value", ((StringValue) result).getString());
    }

    @Test
    public void testNewInstanceWithoutQualifiers() {
        FactoryChain factoryChain = new FactoryChain() {
            @Override
            public Object call(Wiring wiring, Class type, Collection<String> qualifiers) {
                return new TestClass();
            }
        };
        Wiring wiring = new Wiring(factoryChain);
        TestClass result = wiring.newInstance(TestClass.class);
        assertNotNull(result);
    }

    @Test
    public void testNewInstanceWithQualifiers() {
        FactoryChain factoryChain = new FactoryChain() {
            @Override
            public Object call(Wiring wiring, Class type, Collection<String> qualifiers) {
                return new TestClass();
            }
        };
        Wiring wiring = new Wiring(factoryChain);
        TestClass result = wiring.newInstance(TestClass.class, "qualifier1", "qualifier2");
        assertNotNull(result);
    }

    @Test
    public void testSingletonWithoutQualifiers() {
        FactoryChain factoryChain = new FactoryChain() {
            @Override
            public Object call(Wiring wiring, Class type, Collection<String> qualifiers) {
                return new TestClass();
            }
        };
        Wiring wiring = new Wiring(factoryChain);
        TestClass result1 = wiring.singleton(TestClass.class);
        TestClass result2 = wiring.singleton(TestClass.class);
        assertSame(result1, result2);
    }

    @Test
    public void testSingletonWithQualifiers() {
        FactoryChain factoryChain = new FactoryChain() {
            @Override
            public Object call(Wiring wiring, Class type, Collection<String> qualifiers) {
                return new TestClass();
            }
        };
        Wiring wiring = new Wiring(factoryChain);
        TestClass result1 = wiring.singleton(TestClass.class, "qualifier1");
        TestClass result2 = wiring.singleton(TestClass.class, "qualifier1");
        assertSame(result1, result2);
    }

    @Test
    public void testDefaultScopedWithoutQualifiers() {
        FactoryChain factoryChain = new FactoryChain() {
            @Override
            public Object call(Wiring wiring, Class type, Collection<String> qualifiers) {
                return new TestClass();
            }
        };
        Wiring wiring = new Wiring(factoryChain);
        TestClass result1 = wiring.defaultScoped(TestClass.class);
        TestClass result2 = wiring.defaultScoped(TestClass.class);
        assertSame(result1, result2);
    }

    @Test
    public void testDefaultScopedWithQualifiers() {
        FactoryChain factoryChain = new FactoryChain() {
            @Override
            public Object call(Wiring wiring, Class type, Collection<String> qualifiers) {
                return new TestClass();
            }
        };
        Wiring wiring = new Wiring(factoryChain);
        TestClass result1 = wiring.defaultScoped(TestClass.class, "qualifier1");
        TestClass result2 = wiring.defaultScoped(TestClass.class, "qualifier1");
        assertSame(result1, result2);
    }

    @Test
    public void testWithPropertySourceAndFactoryChain() {
        PropertySource propertySource = new PropertySource() {
            @Override
            public Optional<String> get(String property) {
                return Optional.empty();
            }
        };
        FactoryChain factoryChain = new FactoryChain() {
            @Override
            public Object call(Wiring wiring, Class type, Collection<String> qualifiers) {
                return new TestClass();
            }
        };
        Wiring wiring = new Wiring();
        TestClass result = wiring.with(propertySource, factoryChain, w -> w.newInstance(TestClass.class));
        assertNotNull(result);
    }

    @Test
    public void testWithPropertySourceOnly() {
        PropertySource propertySource = new PropertySource() {
            @Override
            public Optional<String> get(String property) {
                return Optional.empty();
            }
        };
        Wiring wiring = new Wiring();
        TestClass result = wiring.with(propertySource, w -> w.newInstance(TestClass.class));
        assertNotNull(result);
    }

    @Test
    public void testWithFactoryChainOnly() {
        FactoryChain factoryChain = new FactoryChain() {
            @Override
            public Object call(Wiring wiring, Class type, Collection<String> qualifiers) {
                return new TestClass();
            }
        };
        Wiring wiring = new Wiring();
        TestClass result = wiring.with(factoryChain, w -> w.newInstance(TestClass.class));
        assertNotNull(result);
    }

    @Test
    public void testWithNoParameters() {
        Wiring wiring = new Wiring();
        TestClass result = wiring.with(w -> w.newInstance(TestClass.class));
        assertNotNull(result);
    }

    @Test
    public void testWiringCopyConstructor() {
        FactoryChain factoryChain = new FactoryChain() {
            @Override
            public Object call(Wiring wiring, Class type, Collection<String> qualifiers) {
                return new TestClass();
            }
        };
        Wiring original = new Wiring(factoryChain);
        Wiring copy = new Wiring(original, factoryChain);
        assertNotNull(copy);
    }

    private static class TestClass {
    }
}
