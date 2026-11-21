package com.google.gwtmockito;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import com.google.gwt.core.client.GWT;
import com.google.gwt.i18n.client.Messages;
import com.google.gwt.resources.client.ClientBundle;
import org.junit.After;
import org.junit.Test;

public class GwtMockito_ArrowTest {

  @After
  public void tearDown() {
    GwtMockito.tearDown();
  }

  @Test
  public void getFakeReturnsDefaultClientBundleFake() {
    ClientBundle fake = GwtMockito.getFake(ClientBundle.class);
    assertNotNull(fake);
    assertTrue(ClientBundle.class.isInstance(fake));
  }

  @Test
  public void getFakeReturnsDefaultMessagesFake() {
    Messages fake = GwtMockito.getFake(Messages.class);
    assertNotNull(fake);
    assertTrue(Messages.class.isInstance(fake));
  }

  @Test
  public void getFakeRejectsTypeWithoutProvider() {
    try {
      GwtMockito.getFake(String.class);
      fail("Expected an IllegalArgumentException");
    } catch (IllegalArgumentException expected) {
      assertTrue(expected.getMessage().contains("String"));
    }
  }

  @Test
  public void initMocksEnablesGwtCreate() {
    GwtMockito.initMocks(this);
    Object created = GWT.create(UnprovidedType.class);
    assertNotNull(created);
    assertTrue(UnprovidedType.class.isInstance(created));
  }

  @Test
  public void useProviderForTypeRejectsCallBeforeInitialization() {
    try {
      GwtMockito.useProviderForType(String.class, null);
      fail("Expected an IllegalStateException");
    } catch (IllegalStateException expected) {
      assertTrue(expected.getMessage().contains("initMocks"));
    }
  }

  @Test
  public void initMocksRegistersDefaultProvidersForGwtCreate() {
    GwtMockito.initMocks(this);
    ClientBundle created = GWT.create(ClientBundle.class);
    assertNotNull(created);
    assertTrue(ClientBundle.class.isInstance(created));
  }

  @Test
  public void getFakeUsesMostSpecificDefaultProvider() {
    GwtMockito.initMocks(this);
    Messages fake = GwtMockito.getFake(Messages.class);
    assertNotNull(fake);
    assertTrue(Messages.class.isInstance(fake));
  }

  @Test
  public void tearDownCanBeCalledWithoutInitialization() {
    GwtMockito.tearDown();
    assertEquals("java.lang.String", String.class.getName());
  }

  private interface UnprovidedType {
  }
}
