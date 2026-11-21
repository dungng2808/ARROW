package cx.corp.lacuna.core.domain;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;

public class NativeProcessImpl_ArrowTest {

    @Test
    public void defaultConstructorInitializesPidToZeroAndStringsToNull() {
        NativeProcessImpl process = new NativeProcessImpl();

        assertEquals(0, process.getPid());
        assertEquals(null, process.getDescription());
        assertEquals(null, process.getOwner());
    }

    @Test
    public void constructorStoresProvidedValues() {
        NativeProcessImpl process = new NativeProcessImpl(42, "worker", "alice");

        assertEquals(42, process.getPid());
        assertEquals("worker", process.getDescription());
        assertEquals("alice", process.getOwner());
    }

    @Test
    public void settersUpdateValues() {
        NativeProcessImpl process = new NativeProcessImpl();
        process.setPid(7);
        process.setDescription("service");
        process.setOwner("bob");

        assertEquals(7, process.getPid());
        assertEquals("service", process.getDescription());
        assertEquals("bob", process.getOwner());
    }

    @Test
    public void equalsReturnsTrueForSameInstance() {
        NativeProcessImpl process = new NativeProcessImpl(1, "app", "owner");

        assertTrue(process.equals(process));
    }

    @Test
    public void equalsReturnsTrueForMatchingValues() {
        NativeProcessImpl first = new NativeProcessImpl(1, "app", "owner");
        NativeProcessImpl second = new NativeProcessImpl(1, "app", "owner");

        assertTrue(first.equals(second));
    }

    @Test
    public void equalsReturnsFalseForDifferentValues() {
        NativeProcessImpl first = new NativeProcessImpl(1, "app", "owner");
        NativeProcessImpl second = new NativeProcessImpl(2, "app", "owner");

        assertFalse(first.equals(second));
    }

    @Test
    public void equalsReturnsFalseForNull() {
        NativeProcessImpl process = new NativeProcessImpl();

        assertFalse(process.equals(null));
    }

    @Test
    public void hashCodeIsEqualForMatchingValues() {
        NativeProcessImpl first = new NativeProcessImpl(1, "app", "owner");
        NativeProcessImpl second = new NativeProcessImpl(1, "app", "owner");

        assertEquals(first.hashCode(), second.hashCode());
    }

    @Test
    public void toStringFormatsProcessValues() {
        NativeProcessImpl process = new NativeProcessImpl(42, "worker", "alice");

        assertEquals("{ PID: 42, owner: alice, description: worker }", process.toString());
    }

    @Test
    public void unknownConstantsAreEmptyStrings() {
        assertEquals("", NativeProcessImpl.UNKNOWN_OWNER);
        assertEquals("", NativeProcessImpl.UNKNOWN_DESCRIPTION);
    }
}
