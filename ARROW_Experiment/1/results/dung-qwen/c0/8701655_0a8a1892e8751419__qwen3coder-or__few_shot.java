package jetbrains.jetpad.base.edt;

import jetbrains.jetpad.base.Registration;
import org.junit.Test;

import static org.junit.Assert.*;

public class RunningEdtManager_ArrowTest {

    @Test
    public void testDefaultConstructor() {
        RunningEdtManager manager = new RunningEdtManager();
        assertNotNull(manager);
        assertEquals("", manager.toString());
    }

    @Test
    public void testNamedConstructor() {
        RunningEdtManager manager = new RunningEdtManager("test");
        assertNotNull(manager);
        assertTrue(manager.toString().contains("test"));
    }

    @Test
    public void testGetEdt() {
        RunningEdtManager manager = new RunningEdtManager();
        EventDispatchThread edt = manager.getEdt();
        assertEquals(manager, edt);
    }

    @Test
    public void testIsStoppedInitially() {
        RunningEdtManager manager = new RunningEdtManager();
        assertFalse(manager.isStopped());
    }

    @Test
    public void testSizeAndIsEmpty() {
        RunningEdtManager manager = new RunningEdtManager();
        assertEquals(0, manager.size());
        assertTrue(manager.isEmpty());

        manager.addTaskToQueue(() -> {});
        assertEquals(1, manager.size());
        assertFalse(manager.isEmpty());
    }

    @Test
    public void testSchedule() {
        RunningEdtManager manager = new RunningEdtManager();
        final boolean[] executed = {false};
        
        manager.schedule(() -> executed[0] = true);
        
        assertTrue(executed[0]);
        assertEquals(0, manager.size());
    }

    @Test
    public void testFinish() {
        RunningEdtManager manager = new RunningEdtManager();
        manager.finish();
        assertTrue(manager.isStopped());
    }

    @Test
    public void testKill() {
        RunningEdtManager manager = new RunningEdtManager();
        manager.addTaskToQueue(() -> {});
        manager.kill();
        assertTrue(manager.isStopped());
        assertEquals(0, manager.size());
    }

    @Test
    public void testFlush() {
        RunningEdtManager manager = new RunningEdtManager();
        final int[] counter = {0};
        
        manager.addTaskToQueue(() -> counter[0]++);
        manager.addTaskToQueue(() -> counter[0] += 10);
        
        manager.flush();
        
        assertEquals(11, counter[0]);
        assertEquals(0, manager.size());
    }

    @Test
    public void testFlushWithTaskNumber() {
        RunningEdtManager manager = new RunningEdtManager();
        final int[] counter = {0};
        
        manager.addTaskToQueue(() -> counter[0]++);
        manager.addTaskToQueue(() -> counter[0] += 10);
        manager.addTaskToQueue(() -> counter[0] += 100);
        
        manager.flush(2);
        
        assertEquals(11, counter[0]);
        assertEquals(1, manager.size());
    }

    @Test(expected = EdtException.class)
    public void testScheduleAfterFinish() {
        RunningEdtManager manager = new RunningEdtManager();
        manager.finish();
        manager.schedule(() -> {});
    }

    @Test(expected = IllegalStateException.class)
    public void testFinishTwice() {
        RunningEdtManager manager = new RunningEdtManager();
        manager.finish();
        manager.finish();
    }

    @Test(expected = IllegalStateException.class)
    public void testKillTwice() {
        RunningEdtManager manager = new RunningEdtManager();
        manager.kill();
        manager.kill();
    }

    @Test(expected = IllegalStateException.class)
    public void testRecursiveFlush() {
        RunningEdtManager manager = new RunningEdtManager();
        manager.schedule(() -> manager.flush());
    }

    @Test
    public void testToString() {
        RunningEdtManager manager = new RunningEdtManager("test");
        String result = manager.toString();
        assertTrue(result.contains("RunningEdtManager@"));
        assertTrue(result.contains("test"));
    }
}
