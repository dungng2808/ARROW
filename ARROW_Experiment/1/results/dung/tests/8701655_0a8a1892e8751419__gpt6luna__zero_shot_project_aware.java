package jetbrains.jetpad.base.edt;

import jetbrains.jetpad.base.Registration;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

public class RunningEdtManager_ArrowTest {
  @Test
  public void defaultConstructorAndGetEdt() {
    RunningEdtManager manager = new RunningEdtManager();

    assertEquals("", manager.toString().substring(manager.toString().indexOf(')') + 1));
    assertEquals(manager, manager.getEdt());
    assertFalse(manager.isStopped());
    assertTrue(manager.isEmpty());
    assertEquals(0, manager.size());
  }

  @Test
  public void namedConstructorIncludesName() {
    RunningEdtManager manager = new RunningEdtManager("test");

    assertTrue(manager.toString().endsWith(" (test)"));
  }

  @Test
  public void scheduleRunsImmediately() {
    RunningEdtManager manager = new RunningEdtManager();
    final int[] count = {0};

    manager.schedule(new Runnable() {
      @Override
      public void run() {
        count[0]++;
      }
    });

    assertEquals(1, count[0]);
    assertTrue(manager.isEmpty());
  }

  @Test
  public void scheduleQueuesTasksAddedDuringExecution() {
    RunningEdtManager manager = new RunningEdtManager();
    final int[] count = {0};

    manager.schedule(new Runnable() {
      @Override
      public void run() {
        count[0]++;
        manager.schedule(new Runnable() {
          @Override
          public void run() {
            count[0]++;
          }
        });
      }
    });

    assertEquals(2, count[0]);
    assertTrue(manager.isEmpty());
  }

  @Test
  public void flushRunsQueuedTasks() {
    RunningEdtManager manager = new RunningEdtManager();
    final int[] count = {0};
    manager.addTaskToQueue(new Runnable() {
      @Override
      public void run() {
        count[0]++;
      }
    });

    manager.flush();

    assertEquals(1, count[0]);
    assertTrue(manager.isEmpty());
  }

  @Test
  public void flushWithZeroLeavesTasksQueued() {
    RunningEdtManager manager = new RunningEdtManager();
    manager.addTaskToQueue(new Runnable() {
      @Override
      public void run() {
        fail();
      }
    });

    manager.flush(0);

    assertEquals(1, manager.size());
  }

  @Test
  public void finishFlushesTasksAndStopsManager() {
    RunningEdtManager manager = new RunningEdtManager();
    final int[] count = {0};
    manager.addTaskToQueue(new Runnable() {
      @Override
      public void run() {
        count[0]++;
      }
    });

    manager.finish();

    assertEquals(1, count[0]);
    assertTrue(manager.isStopped());
    assertTrue(manager.isEmpty());
  }

  @Test
  public void killClearsTasksAndStopsManager() {
    RunningEdtManager manager = new RunningEdtManager();
    manager.addTaskToQueue(new Runnable() {
      @Override
      public void run() {
        fail();
      }
    });

    manager.kill();

    assertTrue(manager.isStopped());
    assertTrue(manager.isEmpty());
  }

  @Test
  public void scheduleAfterStopThrowsEdtException() {
    final RunningEdtManager manager = new RunningEdtManager();
    manager.kill();

    try {
      manager.schedule(new Runnable() {
        @Override
        public void run() {
        }
      });
      fail();
    } catch (EdtException expected) {
      assertNotNull(expected);
    }
  }

  @Test
  public void finishAfterStopThrowsIllegalStateException() {
    RunningEdtManager manager = new RunningEdtManager();
    manager.finish();

    try {
      manager.finish();
      fail();
    } catch (IllegalStateException expected) {
      assertNotNull(expected.getMessage());
    }
  }

  @Test
  public void delayedScheduleIsUnsupported() {
    RunningEdtManager manager = new RunningEdtManager();

    try {
      Registration registration = manager.schedule(1, new Runnable() {
        @Override
        public void run() {
        }
      });
      fail("Expected unsupported operation, got " + registration);
    } catch (UnsupportedOperationException expected) {
      assertNotNull(expected);
    }
  }

  @Test
  public void repeatingScheduleIsUnsupported() {
    RunningEdtManager manager = new RunningEdtManager();

    try {
      Registration registration = manager.scheduleRepeating(1, new Runnable() {
        @Override
        public void run() {
        }
      });
      fail("Expected unsupported operation, got " + registration);
    } catch (UnsupportedOperationException expected) {
      assertNotNull(expected);
    }
  }
}
