package jetbrains.jetpad.base.edt;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

public class RunningEdtManager_ArrowTest {
  @Test
  public void getEdtReturnsManager() {
    RunningEdtManager manager = new RunningEdtManager();
    assertSame(manager, manager.getEdt());
  }

  @Test
  public void scheduleExecutesTaskImmediately() {
    RunningEdtManager manager = new RunningEdtManager();
    List<String> events = new ArrayList<>();

    manager.schedule(new Runnable() {
      @Override
      public void run() {
        events.add("run");
      }
    });

    assertEquals(Arrays.asList("run"), events);
    assertTrue(manager.isEmpty());
  }

  @Test
  public void tasksScheduledDuringExecutionAreFlushedInOrder() {
    RunningEdtManager manager = new RunningEdtManager();
    List<String> events = new ArrayList<>();

    manager.schedule(new Runnable() {
      @Override
      public void run() {
        events.add("first");
        manager.schedule(new Runnable() {
          @Override
          public void run() {
            events.add("second");
          }
        });
      }
    });

    assertEquals(Arrays.asList("first", "second"), events);
    assertTrue(manager.isEmpty());
  }

  @Test
  public void flushExecutesQueuedTasksAndRemovesThem() {
    RunningEdtManager manager = new RunningEdtManager();
    List<String> events = new ArrayList<>();
    manager.addTaskToQueue(new Runnable() {
      @Override
      public void run() {
        events.add("first");
      }
    });
    manager.addTaskToQueue(new Runnable() {
      @Override
      public void run() {
        events.add("second");
      }
    });

    assertEquals(2, manager.size());
    manager.flush();

    assertEquals(Arrays.asList("first", "second"), events);
    assertTrue(manager.isEmpty());
  }

  @Test
  public void flushWithTaskLimitLeavesRemainingTasksQueued() {
    RunningEdtManager manager = new RunningEdtManager();
    List<String> events = new ArrayList<>();
    manager.addTaskToQueue(new Runnable() {
      @Override
      public void run() {
        events.add("first");
      }
    });
    manager.addTaskToQueue(new Runnable() {
      @Override
      public void run() {
        events.add("second");
      }
    });

    manager.flush(1);

    assertEquals(Arrays.asList("first"), events);
    assertEquals(1, manager.size());
  }

  @Test
  public void finishFlushesQueuedTasksAndStopsManager() {
    RunningEdtManager manager = new RunningEdtManager();
    List<String> events = new ArrayList<>();
    manager.addTaskToQueue(new Runnable() {
      @Override
      public void run() {
        events.add("queued");
      }
    });

    manager.finish();

    assertEquals(Arrays.asList("queued"), events);
    assertTrue(manager.isStopped());
    assertTrue(manager.isEmpty());
  }

  @Test
  public void killClearsQueuedTasksAndStopsManager() {
    RunningEdtManager manager = new RunningEdtManager();
    manager.addTaskToQueue(new Runnable() {
      @Override
      public void run() {
        fail("Queued task should not run");
      }
    });

    manager.kill();

    assertTrue(manager.isStopped());
    assertTrue(manager.isEmpty());
  }

  @Test
  public void scheduleAfterStopThrowsEdtException() {
    RunningEdtManager manager = new RunningEdtManager();
    manager.kill();

    try {
      manager.schedule(new Runnable() {
        @Override
        public void run() {
        }
      });
      fail("Expected EdtException");
    } catch (EdtException expected) {
      assertTrue(manager.isStopped());
    }
  }

  @Test
  public void stoppingTwiceThrowsIllegalStateException() {
    RunningEdtManager manager = new RunningEdtManager();
    manager.finish();

    try {
      manager.finish();
      fail("Expected IllegalStateException");
    } catch (IllegalStateException expected) {
      assertTrue(manager.isStopped());
    }
  }

  @Test
  public void toStringIncludesProvidedName() {
    RunningEdtManager manager = new RunningEdtManager("test");
    assertTrue(manager.toString().startsWith("RunningEdtManager@"));
    assertTrue(manager.toString().endsWith(" (test)"));
  }
}
