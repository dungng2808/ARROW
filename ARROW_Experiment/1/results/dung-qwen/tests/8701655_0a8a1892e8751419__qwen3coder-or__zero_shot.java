package jetbrains.jetpad.base.edt;

import jetbrains.jetpad.base.Registration;
import org.junit.Test;

import static org.junit.Assert.*;

public class RunningEdtManager_ArrowTest {

    @Test
    public void testConstructorDefault() {
        RunningEdtManager manager = new RunningEdtManager();
        assertNotNull(manager);
    }

    @Test
    public void testConstructorWithName() {
        RunningEdtManager manager = new RunningEdtManager("test");
        assertNotNull(manager);
    }

    @Test
    public void testGetEdt() {
        RunningEdtManager manager = new RunningEdtManager();
        assertEquals(manager, manager.getEdt());
    }

    @Test
    public void testIsStoppedInitially() {
        RunningEdtManager manager = new RunningEdtManager();
        assertFalse(manager.isStopped());
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
        manager.kill();
        assertTrue(manager.isStopped());
    }

    @Test
    public void testSchedule() {
        RunningEdtManager manager = new RunningEdtManager();
        final boolean[] executed = {false};
        manager.schedule(new Runnable() {
            @Override
            public void run() {
                executed[0] = true;
            }
        });
        assertTrue(executed[0]);
    }

    @Test(expected = EdtException.class)
    public void testScheduleAfterFinish() {
        RunningEdtManager manager = new RunningEdtManager();
        manager.finish();
        manager.schedule(new Runnable() {
            @Override
            public void run() {
            }
        });
    }

    @Test
    public void testSize() {
        RunningEdtManager manager = new RunningEdtManager();
        assertEquals(0, manager.size());
        manager.schedule(new Runnable() {
            @Override
            public void run() {
            }
        });
        assertEquals(0, manager.size());
    }

    @Test
    public void testIsEmpty() {
        RunningEdtManager manager = new RunningEdtManager();
        assertTrue(manager.isEmpty());
        manager.schedule(new Runnable() {
            @Override
            public void run() {
            }
        });
        assertTrue(manager.isEmpty());
    }

    @Test
    public void testFlush() {
        RunningEdtManager manager = new RunningEdtManager();
        manager.flush();
        // Should not throw
    }

    @Test
    public void testFlushWithTasks() {
        RunningEdtManager manager = new RunningEdtManager();
        manager.addTaskToQueue(new Runnable() {
            @Override
            public void run() {
            }
        });
        manager.flush();
        // Should not throw
    }

    @Test(expected = IllegalStateException.class)
    public void testRecursiveFlush() {
        RunningEdtManager manager = new RunningEdtManager();
        manager.flush(new Supplier<Integer>() {
            @Override
            public Integer get() {
                manager.flush();
                return 0;
            }
        });
    }

    @Test
    public void testToString() {
        RunningEdtManager manager = new RunningEdtManager("test");
        String result = manager.toString();
        assertTrue(result.contains("RunningEdtManager"));
        assertTrue(result.contains("test"));
    }

    @Test
    public void testToStringEmptyName() {
        RunningEdtManager manager = new RunningEdtManager("");
        String result = manager.toString();
        assertTrue(result.contains("RunningEdtManager"));
        assertFalse(result.contains("("));
    }
}
