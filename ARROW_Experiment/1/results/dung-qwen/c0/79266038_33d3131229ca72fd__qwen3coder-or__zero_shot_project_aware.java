package cx.corp.lacuna.core.domain;

import org.junit.Test;

import static org.junit.Assert.*;

public class NativeProcessImpl_ArrowTest {

    @Test
    public void testDefaultConstructor() {
        NativeProcessImpl process = new NativeProcessImpl();
        assertEquals(0, process.getPid());
        assertEquals(NativeProcessImpl.UNKNOWN_DESCRIPTION, process.getDescription());
        assertEquals(NativeProcessImpl.UNKNOWN_OWNER, process.getOwner());
    }

    @Test
    public void testParameterizedConstructor() {
        NativeProcessImpl process = new NativeProcessImpl(123, "test desc", "test owner");
        assertEquals(123, process.getPid());
        assertEquals("test desc", process.getDescription());
        assertEquals("test owner", process.getOwner());
    }

    @Test
    public void testSetPid() {
        NativeProcessImpl process = new NativeProcessImpl();
        process.setPid(456);
        assertEquals(456, process.getPid());
    }

    @Test
    public void testSetDescription() {
        NativeProcessImpl process = new NativeProcessImpl();
        process.setDescription("new desc");
        assertEquals("new desc", process.getDescription());
    }

    @Test
    public void testSetOwner() {
        NativeProcessImpl process = new NativeProcessImpl();
        process.setOwner("new owner");
        assertEquals("new owner", process.getOwner());
    }

    @Test
    public void testEqualsSameObject() {
        NativeProcessImpl process = new NativeProcessImpl(1, "desc", "owner");
        assertTrue(process.equals(process));
    }

    @Test
    public void testEqualsNull() {
        NativeProcessImpl process = new NativeProcessImpl();
        assertFalse(process.equals(null));
    }

    @Test
    public void testEqualsDifferentClass() {
        NativeProcessImpl process = new NativeProcessImpl();
        assertFalse(process.equals("not a process"));
    }

    @Test
    public void testEqualsEqualProcesses() {
        NativeProcessImpl p1 = new NativeProcessImpl(1, "desc", "owner");
        NativeProcessImpl p2 = new NativeProcessImpl(1, "desc", "owner");
        assertTrue(p1.equals(p2));
        assertTrue(p2.equals(p1));
    }

    @Test
    public void testEqualsDifferentPid() {
        NativeProcessImpl p1 = new NativeProcessImpl(1, "desc", "owner");
        NativeProcessImpl p2 = new NativeProcessImpl(2, "desc", "owner");
        assertFalse(p1.equals(p2));
    }

    @Test
    public void testEqualsDifferentDescription() {
        NativeProcessImpl p1 = new NativeProcessImpl(1, "desc1", "owner");
        NativeProcessImpl p2 = new NativeProcessImpl(1, "desc2", "owner");
        assertFalse(p1.equals(p2));
    }

    @Test
    public void testEqualsDifferentOwner() {
        NativeProcessImpl p1 = new NativeProcessImpl(1, "desc", "owner1");
        NativeProcessImpl p2 = new NativeProcessImpl(1, "desc", "owner2");
        assertFalse(p1.equals(p2));
    }

    @Test
    public void testHashCodeEqualProcesses() {
        NativeProcessImpl p1 = new NativeProcessImpl(1, "desc", "owner");
        NativeProcessImpl p2 = new NativeProcessImpl(1, "desc", "owner");
        assertEquals(p1.hashCode(), p2.hashCode());
    }

    @Test
    public void testToString() {
        NativeProcessImpl process = new NativeProcessImpl(123, "test desc", "test owner");
        String expected = "{ PID: 123, owner: test owner, description: test desc }";
        assertEquals(expected, process.toString());
    }
}
