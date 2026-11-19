package jetbrains.jetpad.base.edt;

import org.junit.Test;

import static org.junit.Assert.*;

public class RunningEdtManager_ArrowTest {
  @Test
  public void getEdtReturnsManager() {
    RunningEdtManager manager = new RunningEdtManager();
    assertSame(manager, manager.getEdt());
  }

  @Test
  public void scheduleExecutesTaskImmediately() {
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
  public void tasksScheduledDuringExecutionAreFlushed() {
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
  public void flushExecutesQueuedTasks() {
    RunningEdtManager manager = new RunningEdtManager();
    final int[] count = {0};
    manager.addTaskToQueue(new Runnable() {
      @Override
      public void run() {
        count[0]++;
      }
    });
    manager.addTaskToQueue(new Runnable() {
      @Override
      public void run() {
        count[0] += 2;
      }
    });

    manager.flush();

    assertEquals(3, count[0]);
    assertTrue(manager.isEmpty());
  }

  @Test
  public void flushWithTaskLimitLeavesRemainingTasksQueued() {
    RunningEdtManager manager = new RunningEdtManager();
    final int[] count = {0};
    manager.addTaskToQueue(new Runnable() {
      @Override
      public void run() {
        count[0]++;
      }
    });
    manager.addTaskToQueue(new Runnable() {
      @Override
      public void run() {
        count[0]++;
      }
    });

    manager.flush(1);

    assertEquals(1, count[0]);
    assertEquals(1, manager.size());
  }

  @Test
  public void finishFlushesQueuedTasksAndStopsManager() {
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
  public void toStringIncludesProvidedName() {
    RunningEdtManager manager = new RunningEdtManager("worker");
    assertTrue(manager.toString().matches("RunningEdtManager@[0-9a-f]+ \\(worker\\)"));
  }

  @Test(expected = EdtException.class)
  public void schedulingAfterFinishThrows() {
    RunningEdtManager manager = new RunningEdtManager();
    manager.finish();
    manager.schedule(new Runnable() {
      @Override
      public void run() {
      }
    });
  }

  @Test(expected = IllegalStateException.class)
  public void finishingTwiceThrows() {
    RunningEdtManager manager = new RunningEdtManager();
    manager.finish();
    manager.finish();
  }

  @Test(expected = IllegalStateException.class)
  public void recursiveFlushThrows() {
    final RunningEdtManager manager = new RunningEdtManager();
    manager.addTaskToQueue(new Runnable() {
      @Override
      public void run() {
        manager.flush();
      }
    });

    manager.flush();
  }

  @Test(expected = UnsupportedOperationException.class)
  public void delayedScheduleIsUnsupported() {
    RunningEdtManager manager = new RunningEdtManager();
    manager.schedule(1, new Runnable() {
      @Override
      public void run() {
      }
    });
  }

  @Test(expected = UnsupportedOperationException.class)
  public void repeatingScheduleIsUnsupported() {
    RunningEdtManager manager = new RunningEdtManager();
    manager.scheduleRepeating(1, new Runnable() {
      @Override
      public void run() {
      }
    });
  }
}
