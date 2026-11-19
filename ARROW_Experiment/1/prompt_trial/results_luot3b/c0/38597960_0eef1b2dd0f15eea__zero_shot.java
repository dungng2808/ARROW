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
  public void fetchCountReturnsValueAfterSetting() {
    Timeline timeline = new Timeline();
    timeline.setFetchCount(7);

    assertEquals(7, timeline.getFetchCount());
  }

  @Test
  public void fetchCountCanBeSetToNegativeValue() {
    Timeline timeline = new Timeline();
    timeline.setFetchCount(-3);

    assertEquals(-3, timeline.getFetchCount());
  }
}
