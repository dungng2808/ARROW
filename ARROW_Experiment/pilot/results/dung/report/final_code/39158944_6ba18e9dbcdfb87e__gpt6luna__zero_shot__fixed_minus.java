package io.wcm.samples.core.business.navigation.impl;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class NavigationManagerImpl_ArrowTest {

  @Test
  public void classCanBeLoaded() {
    assertNotNull(NavigationManagerImpl.class);
    assertEquals("NavigationManagerImpl", NavigationManagerImpl.class.getSimpleName());
  }

}
