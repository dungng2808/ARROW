package com.google.gwtmockito;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.fail;

import com.google.gwtmockito.fakes.FakeProvider;
import org.junit.After;
import org.junit.Test;

public class GwtMockito_ArrowTest {

  @After
  public void tearDown() {
    GwtMockito.tearDown();
  }

  @Test
  public void getFakeUsesDefaultClientBundleProvider() {
    Object fake = GwtMockito.getFake(com.google.gwt.resources.client.ClientBundle.class);
    assertNotNull(fake);
    assertEquals(com.google.gwt.resources.client.ClientBundle.class, fake.getClass().getInterfaces()[0]);
  }

  @Test
  public void getFakeUsesRegisteredProvider() {
    GwtMockito.initMocks(this);
    final Object expected = new Object();
    GwtMockito.useProviderForType(CustomType.class, new FakeProvider<CustomType>() {
      @Override
      public CustomType getFake(Class<CustomType> type) {
        return (CustomType) expected;
      }
    });

    CustomType actual = GwtMockito.getFake(CustomType.class);
    assertSame(expected, actual);
  }

  @Test
  public void getFakeThrowsWhenNoProviderExists() {
    try {
      GwtMockito.getFake(CustomType.class);
      fail("Expected an IllegalArgumentException");
    } catch (IllegalArgumentException expected) {
      assertEquals("No fake provider has been registered for CustomType. Call useProviderForType to register a provider before calling getFake.",
          expected.getMessage());
    }
  }

  @Test
  public void useProviderForTypeRequiresInitialization() {
    try {
      GwtMockito.useProviderForType(CustomType.class, null);
      fail("Expected an IllegalStateException");
    } catch (IllegalStateException expected) {
      assertEquals("Must call initMocks() before calling useProviderForType()", expected.getMessage());
    }
  }

  private static class CustomType {
  }
}
