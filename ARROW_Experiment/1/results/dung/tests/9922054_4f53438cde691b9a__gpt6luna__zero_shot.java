package com.google.gwtmockito;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import com.google.gwt.i18n.client.Messages;
import com.google.gwt.resources.client.CssResource;
import com.google.gwtmockito.fakes.FakeProvider;
import org.junit.After;
import org.junit.Test;

public class GwtMockito_ArrowTest {

  @After
  public void tearDown() {
    GwtMockito.tearDown();
  }

  @Test
  public void getFakeReturnsDefaultFakeForMessages() {
    Messages fake = GwtMockito.getFake(Messages.class);

    assertNotNull(fake);
    assertTrue(Messages.class.isInstance(fake));
  }

  @Test
  public void getFakeReturnsDefaultFakeForCssResource() {
    CssResource fake = GwtMockito.getFake(CssResource.class);

    assertNotNull(fake);
    assertTrue(CssResource.class.isInstance(fake));
  }

  @Test(expected = IllegalArgumentException.class)
  public void getFakeRejectsTypeWithoutProvider() {
    GwtMockito.getFake(String.class);
  }

  @Test
  public void useProviderForTypeRegistersProviderAfterInitialization() {
    GwtMockito.initMocks(this);
    FakeProvider<String> provider = new FakeProvider<String>() {
      @Override
      public String getFake(Class<?> type) {
        return "provided";
      }
    };

    GwtMockito.useProviderForType(String.class, provider);

    assertEquals("provided", GwtMockito.getFake(String.class));
  }

  @Test(expected = IllegalStateException.class)
  public void useProviderForTypeRequiresInitialization() {
    GwtMockito.useProviderForType(String.class, new FakeProvider<String>() {
      @Override
      public String getFake(Class<?> type) {
        return "provided";
      }
    });
  }

  @Test
  public void initMocksAllowsProviderRegistrationForUnmockedType() {
    GwtMockito.initMocks(this);
    FakeProvider<String> provider = new FakeProvider<String>() {
      @Override
      public String getFake(Class<?> type) {
        return "registered";
      }
    };

    GwtMockito.useProviderForType(String.class, provider);

    assertSame(provider, provider);
    assertEquals("registered", GwtMockito.getFake(String.class));
  }
}
