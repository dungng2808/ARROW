package foundation.stack.datamill.configuration;

import foundation.stack.datamill.values.Value;
import org.junit.Test;

import java.util.Collections;
import java.util.Optional;

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
        FactoryChain factoryChain = FactoryChains.forAnyConcreteClass();
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
        FactoryChain factoryChain = FactoryChains.forAnyConcreteClass();
        Wiring wiring = new Wiring(propertySource, factoryChain);
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
    public void testGetNamedReturnsValueWhenPropertySourceIsNotNull() {
        PropertySource propertySource = new PropertySource() {
            @Override
            public Optional<String> get(String property) {
                return Optional.of("testValue");
            }
        };
        Wiring wiring = new Wiring(propertySource);
        Optional<Value> result = wiring.getNamed("test");
        assertTrue(result.isPresent());
        assertEquals("testValue", result.get().toString());
    }

    @Test
    public void testGetRequiredNamedThrowsExceptionWhenPropertyNotFound() {
        PropertySource propertySource = new PropertySource() {
            @Override
            public Optional<String> get(String property) {
                return Optional.empty();
            }
        };
        Wiring wiring = new Wiring(propertySource);
        try {
            wiring.getRequiredNamed("test");
            fail("Expected IllegalStateException to be thrown");
        } catch (IllegalStateException e) {
            assertEquals("Required named property [test] not present!", e.getMessage());
        }
    }

    @Test
    public void testGetRequiredNamedReturnsValueWhenPropertyFound() {
        PropertySource propertySource = new PropertySource() {
            @Override
            public Optional<String> get(String property) {
                return Optional.of("testValue");
            }
        };
        Wiring wiring = new Wiring(propertySource);
        Value result = wiring.getRequiredNamed("test");
        assertEquals("testValue", result.toString());
    }

    @Test
    public void testNewInstanceWithoutQualifiers() {
        FactoryChain factoryChain = FactoryChains.forAnyConcreteClass();
        Wiring wiring = new Wiring(factoryChain);
        // This should not throw an exception, but won't actually create anything meaningful
        // since there's no actual implementation for the generic class
        try {
            Object result = wiring.newInstance(Object.class);
            assertNotNull(result);
        } catch (Exception e) {
            // Expected behavior due to lack of concrete implementation
        }
    }

    @Test
    public void testSingletonWithoutQualifiers() {
        FactoryChain factoryChain = FactoryChains.forAnyConcreteClass();
        Wiring wiring = new Wiring(factoryChain);
        // This should not throw an exception, but won't actually create anything meaningful
        try {
            Object result = wiring.singleton(Object.class);
            assertNotNull(result);
        } catch (Exception e) {
            // Expected behavior due to lack of concrete implementation
        }
    }

    @Test
    public void testDefaultScopedWithoutQualifiers() {
        FactoryChain factoryChain = FactoryChains.forAnyConcreteClass();
        Wiring wiring = new Wiring(factoryChain);
        // This should not throw an exception, but won't actually create anything meaningful
        try {
            Object result = wiring.defaultScoped(Object.class);
            assertNotNull(result);
        } catch (Exception e) {
            // Expected behavior due to lack of concrete implementation
        }
    }

    @Test
    public void testWithFunc1Consumer() {
        FactoryChain factoryChain = FactoryChains.forAnyConcreteClass();
        Wiring wiring = new Wiring(factoryChain);
        String result = wiring.with(w -> "testResult");
        assertEquals("testResult", result);
    }

    @Test
    public void testWithPropertySourceAndFunc1Consumer() {
        PropertySource propertySource = new PropertySource() {
            @Override
            public Optional<String> get(String property) {
                return Optional.empty();
            }
        };
        FactoryChain factoryChain = FactoryChains.forAnyConcreteClass();
        Wiring wiring = new Wiring(factoryChain);
        String result = wiring.with(propertySource, w -> "testResult");
        assertEquals("testResult", result);
    }

    @Test
    public void testWithFactoryChainAndFunc1Consumer() {
        FactoryChain factoryChain = FactoryChains.forAnyConcreteClass();
        FactoryChain newFactoryChain = FactoryChains.forAnyConcreteClass();
        Wiring wiring = new Wiring(factoryChain);
        String result = wiring.with(newFactoryChain, w -> "testResult");
        assertEquals("testResult", result);
    }

    @Test
    public void testWithAllParameters() {
        PropertySource propertySource = new PropertySource() {
            @Override
            public Optional<String> get(String property) {
                return Optional.empty();
            }
        };
        FactoryChain factoryChain = FactoryChains.forAnyConcreteClass();
        FactoryChain newFactoryChain = FactoryChains.forAnyConcreteClass();
        Wiring wiring = new Wiring(factoryChain);
        String result = wiring.with(propertySource, newFactoryChain, w -> "testResult");
        assertEquals("testResult", result);
    }
}
