package cx.corp.lacuna.core.domain;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;

public class NativeProcessImpl_ArrowTest {

    @Test
    public void constructorAndGettersReturnProvidedValues() {
        NativeProcessImpl process = new NativeProcessImpl(42, "worker", "alice");

        assertEquals(42, process.getPid());
        assertEquals("worker", process.getDescription());
        assertEquals("alice", process.getOwner());
    }

    @Test
    public void defaultConstructorUsesJavaDefaultValues() {
        NativeProcessImpl process = new NativeProcessImpl();

        assertEquals(0, process.getPid());
        assertEquals(null, process.getDescription());
        assertEquals(null, process.getOwner());
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
    public void equalsRecognizesSameAndEquivalentObjects() {
        NativeProcessImpl process = new NativeProcessImpl(3, "editor", "carol");
        NativeProcessImpl equivalent = new NativeProcessImpl(3, "editor", "carol");

        assertTrue(process.equals(process));
        assertTrue(process.equals(equivalent));
        assertTrue(equivalent.equals(process));
    }

    @Test
    public void equalsRejectsNullDifferentClassAndDifferentValues() {
        NativeProcessImpl process = new NativeProcessImpl(3, "editor", "carol");

        assertFalse(process.equals(null));
        assertFalse(process.equals("editor"));
        assertFalse(process.equals(new NativeProcessImpl(4, "editor", "carol")));
        assertFalse(process.equals(new NativeProcessImpl(3, "other", "carol")));
        assertFalse(process.equals(new NativeProcessImpl(3, "editor", "other")));
    }

    @Test
    public void equalObjectsHaveEqualHashCodes() {
        NativeProcessImpl process = new NativeProcessImpl(9, "daemon", "dana");
        NativeProcessImpl equivalent = new NativeProcessImpl(9, "daemon", "dana");

        assertEquals(process.hashCode(), equivalent.hashCode());
    }

    @Test
    public void toStringFormatsProcessDetails() {
        NativeProcessImpl process = new NativeProcessImpl(12, "worker", "alice");

        assertEquals("{ PID: 12, owner: alice, description: worker }", process.toString());
    }

    @Test
    public void unknownConstantsAreEmptyStrings() {
        assertEquals("", NativeProcessImpl.UNKNOWN_OWNER);
        assertEquals("", NativeProcessImpl.UNKNOWN_DESCRIPTION);
    }
}
