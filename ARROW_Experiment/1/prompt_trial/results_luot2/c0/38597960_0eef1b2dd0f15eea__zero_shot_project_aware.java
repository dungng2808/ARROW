package book.twju.chapter_1;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class Timeline_ArrowTest {

  @Test
  public void fetchCountDefaultsToZero() {
    Timeline timeline = new Timeline();

    assertEquals(0, timeline.getFetchCount());
  }

  @Test
  public void fetchCountCanBeSet() {
    Timeline timeline = new Timeline();
    timeline.setFetchCount(5);

    assertEquals(5, timeline.getFetchCount());
  }

  @Test
  public void fetchCountCanBeSetToNegativeValue() {
    Timeline timeline = new Timeline();
    timeline.setFetchCount(-3);

    assertEquals(-3, timeline.getFetchCount());
  }
}
