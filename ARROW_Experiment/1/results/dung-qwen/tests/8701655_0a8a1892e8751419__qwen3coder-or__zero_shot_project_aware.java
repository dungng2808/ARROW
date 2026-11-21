package jetbrains.jetpad.base.edt;

import jetbrains.jetpad.base.Registration;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;

public class RunningEdtManager_ArrowTest {

    @Test
    public void testConstructorDefault() {
        RunningEdtManager manager = new RunningEdtManager();
        assertEquals("", manager.toString());
    }

    @Test
    public void testConstructorWithName() {
        RunningEdtManager manager = new RunningEdtManager("test");
        assertTrue(manager.toString().contains("test"));
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

    @Test(expected = IllegalStateException.class)
    public void testFinishTwice() {
        RunningEdtManager manager = new RunningEdtManager();
        manager.finish();
        manager.finish();
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
        final List<String> executed = new ArrayList<>();
        manager.schedule(new Runnable() {
            @Override
            public void run() {
                executed.add("executed");
            }
        });
        assertEquals(1, executed.size());
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
        manager.flush(new Runnable() {
            @Override
            public void run() {
                manager.flush();
            }
        });
    }

    @Test
    public void testToString() {
        RunningEdtManager manager = new RunningEdtManager("test");
        assertTrue(manager.toString().contains("test"));
    }

    @Test
    public void testGetCurrentTime() {
        RunningEdtManager manager = new RunningEdtManager();
        long time = manager.getCurrentTime();
        assertTrue(time > 0);
    }

    @Test(expected = UnsupportedOperationException.class)
    public void testScheduleRepeating() {
        RunningEdtManager manager = new RunningEdtManager();
        manager.scheduleRepeating(100, new Runnable() {
            @Override
            public void run() {
            }
        });
    }

    @Test(expected = UnsupportedOperationException.class)
    public void testScheduleWithDelay() {
        RunningEdtManager manager = new RunningEdtManager();
        manager.schedule(100, new Runnable() {
            @Override
            public void run() {
            }
        });
    }
}
