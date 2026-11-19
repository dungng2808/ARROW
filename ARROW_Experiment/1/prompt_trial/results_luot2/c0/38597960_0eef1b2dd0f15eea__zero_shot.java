package book.twju.chapter_1;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class Timeline_ArrowTest {

  @Test
  public void fetchCountIsZeroByDefault() {
    Timeline timeline = new Timeline();

    assertEquals(0, timeline.getFetchCount());
  }

  @Test
  public void setFetchCountUpdatesValue() {
    Timeline timeline = new Timeline();

    timeline.setFetchCount(5);

    assertEquals(5, timeline.getFetchCount());
  }

  @Test
  public void setFetchCountAcceptsNegativeValue() {
    Timeline timeline = new Timeline();

    timeline.setFetchCount(-3);

    assertEquals(-3, timeline.getFetchCount());
  }
}
