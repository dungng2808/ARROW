package cx.corp.lacuna.core.domain;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

public class NativeProcessImpl_ArrowTest {

    @Test
    public void defaultConstructorInitializesPidToZeroAndStringsToNull() {
        NativeProcessImpl process = new NativeProcessImpl();

        assertEquals(0, process.getPid());
        assertNull(process.getDescription());
        assertNull(process.getOwner());
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
        process.setDescription("server");
        process.setOwner("bob");

        assertEquals(7, process.getPid());
        assertEquals("server", process.getDescription());
        assertEquals("bob", process.getOwner());
    }

    @Test
    public void settersAcceptNullStrings() {
        NativeProcessImpl process = new NativeProcessImpl(1, "description", "owner");
        process.setDescription(null);
        process.setOwner(null);

        assertNull(process.getDescription());
        assertNull(process.getOwner());
    }

    @Test
    public void equalProcessesHaveEqualHashCodes() {
        NativeProcessImpl first = new NativeProcessImpl(3, "task", "user");
        NativeProcessImpl second = new NativeProcessImpl(3, "task", "user");

        assertTrue(first.equals(second));
        assertEquals(first.hashCode(), second.hashCode());
    }

    @Test
    public void equalsHandlesIdentityNullAndDifferentClass() {
        NativeProcessImpl process = new NativeProcessImpl(3, "task", "user");

        assertTrue(process.equals(process));
        assertFalse(process.equals(null));
        assertFalse(process.equals("not a process"));
    }

    @Test
    public void equalsDetectsDifferentFields() {
        NativeProcessImpl original = new NativeProcessImpl(3, "task", "user");

        assertFalse(original.equals(new NativeProcessImpl(4, "task", "user")));
        assertFalse(original.equals(new NativeProcessImpl(3, "other", "user")));
        assertFalse(original.equals(new NativeProcessImpl(3, "task", "other")));
    }

    @Test
    public void equalsHandlesNullFields() {
        NativeProcessImpl first = new NativeProcessImpl(0, null, null);
        NativeProcessImpl second = new NativeProcessImpl(0, null, null);

        assertTrue(first.equals(second));
        assertEquals(first.hashCode(), second.hashCode());
        assertFalse(first.equals(new NativeProcessImpl(0, "", null)));
    }

    @Test
    public void toStringFormatsProcessFields() {
        NativeProcessImpl process = new NativeProcessImpl(12, "worker", "alice");

        assertEquals("{ PID: 12, owner: alice, description: worker }", process.toString());
    }

    @Test
    public void constantsRepresentUnknownValuesAsEmptyStrings() {
        assertEquals("", NativeProcessImpl.UNKNOWN_OWNER);
        assertEquals("", NativeProcessImpl.UNKNOWN_DESCRIPTION);
    }
}
