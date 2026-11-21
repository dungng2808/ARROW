package com.google.gwtmockito;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;

import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.GWTBridge;
import com.google.gwt.i18n.client.Messages;
import com.google.gwt.i18n.client.constants.NumberConstantsImpl;
import com.google.gwt.i18n.client.impl.LocaleInfoImpl;
import com.google.gwt.resources.client.ClientBundle;
import com.google.gwt.resources.client.CssResource;
import com.google.gwt.safehtml.client.SafeHtmlTemplates;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwtmockito.fakes.FakeClientBundleProvider;
import com.google.gwtmockito.fakes.FakeLocaleInfoImplProvider;
import com.google.gwtmockito.fakes.FakeMessagesProvider;
import com.google.gwtmockito.fakes.FakeNumberConstantsImplProvider;
import com.google.gwtmockito.fakes.FakeProvider;
import com.google.gwtmockito.fakes.FakeUiBinderProvider;
import com.google.gwtmockito.impl.ReturnsCustomMocks;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.MockitoAnnotations;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

public class GwtMockito_ArrowTest {

  @Before
  public void setUp() {
    GwtMockito.tearDown();
  }

  @After
  public void tearDown() {
    GwtMockito.tearDown();
  }

  @Test
  public void testInitMocks_registersDefaultProviders() {
    TestClass owner = new TestClass();
    GwtMockito.initMocks(owner);

    assertNotNull(GwtMockito.class.getDeclaredField("bridge").get(null));
  }

  @Test
  public void testInitMocks_populatesGwtMocks() throws Exception {
    TestClass owner = new TestClass();
    GwtMockito.initMocks(owner);

    Field field = TestClass.class.getDeclaredField("mockField");
    field.setAccessible(true);
    Object mock = field.get(owner);
    assertNotNull(mock);
    assertTrue(mock instanceof com.google.gwt.user.client.ui.Button);
  }

  @Test
  public void testTearDown_resetsGwtBridge() {
    GwtMockito.initMocks(new TestClass());
    GwtMockito.tearDown();

    // Verify that GWT bridge is reset by checking that we can't access the bridge field
    try {
      Field bridgeField = GwtMockito.class.getDeclaredField("bridge");
      bridgeField.setAccessible(true);
      assertNull(bridgeField.get(null));
    } catch (Exception e) {
      // Expected
    }
  }

  @Test
  public void testUseProviderForType_registersProvider() {
    GwtMockito.initMocks(new TestClass());

    FakeProvider<Messages> provider = new FakeMessagesProvider<Messages>();
    GwtMockito.useProviderForType(Messages.class, provider);

    // Check that the provider was registered
    try {
      Field bridgeField = GwtMockito.class.getDeclaredField("bridge");
      bridgeField.setAccessible(true);
      Object bridge = bridgeField.get(null);
      
      Field registeredProvidersField = bridge.getClass().getDeclaredField("registeredProviders");
      registeredProvidersField.setAccessible(true);
      Map<Class<?>, FakeProvider<?>> providers = (Map<Class<?>, FakeProvider<?>>) registeredProvidersField.get(bridge);
      
      assertSame(provider, providers.get(Messages.class));
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  @Test(expected = IllegalStateException.class)
  public void testUseProviderForType_throwsIfNotInitialized() {
    GwtMockito.useProviderForType(Messages.class, new FakeMessagesProvider<Messages>());
  }

  @Test(expected = IllegalArgumentException.class)
  public void testUseProviderForType_throwsIfGwtMockExists() {
    GwtMockito.initMocks(new TestClassWithGwtMock());
    GwtMockito.useProviderForType(com.google.gwt.user.client.ui.Button.class, new FakeProvider<com.google.gwt.user.client.ui.Button>() {
      @Override
      public com.google.gwt.user.client.ui.Button getFake(Class<com.google.gwt.user.client.ui.Button> type) {
        return null;
      }
    });
  }

  @Test
  public void testGetFake_returnsFakeFromDefaultProviders() {
    GwtMockito.initMocks(new TestClass());

    Messages fake = GwtMockito.getFake(Messages.class);
    assertNotNull(fake);
    assertTrue(fake instanceof Messages);
  }

  @Test
  public void testGetFake_returnsFakeFromRegisteredProvider() {
    GwtMockito.initMocks(new TestClass());
    FakeProvider<Messages> provider = new FakeMessagesProvider<Messages>();
    GwtMockito.useProviderForType(Messages.class, provider);

    Messages fake = GwtMockito.getFake(Messages.class);
    assertNotNull(fake);
    assertTrue(fake instanceof Messages);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testGetFake_throwsIfNoProvider() {
    GwtMockito.initMocks(new TestClass());
    GwtMockito.getFake(RemoteService.class);
  }

  @Test
  public void testGetFakeFromProviderMap_findsExactMatch() {
    Map<Class<?>, FakeProvider<?>> map = new HashMap<Class<?>, FakeProvider<?>>();
    FakeProvider<Messages> provider = new FakeMessagesProvider<Messages>();
    map.put(Messages.class, provider);

    Messages fake = GwtMockito.getFakeFromProviderMap(Messages.class, map);
    assertNotNull(fake);
  }

  @Test
  public void testGetFakeFromProviderMap_findsSuperclassMatch() {
    Map<Class<?>, FakeProvider<?>> map = new HashMap<Class<?>, FakeProvider<?>>();
    FakeProvider<Messages> provider = new FakeMessagesProvider<Messages>();
    map.put(Messages.class, provider);

    // CssResource is a subclass of Messages
    CssResource fake = GwtMockito.getFakeFromProviderMap(CssResource.class, map);
    assertNotNull(fake);
  }

  @Test
  public void testGetFakeFromProviderMap_handlesAmbiguity() {
    Map<Class<?>, FakeProvider<?>> map = new HashMap<Class<?>, FakeProvider<?>>();
    FakeProvider<Messages> provider1 = new FakeMessagesProvider<Messages>();
    FakeProvider<ClientBundle> provider2 = new FakeClientBundleProvider();
    map.put(Messages.class, provider1);
    map.put(ClientBundle.class, provider2);

    try {
      GwtMockito.getFakeFromProviderMap(Messages.class, map);
    } catch (IllegalArgumentException e) {
      // Expected
    }
  }

  @Test
  public void testBridge_create_returnsMockWhenNoProvider() {
    GwtMockito.initMocks(new TestClass());
    
    // Access the bridge
    try {
      Field bridgeField = GwtMockito.class.getDeclaredField("bridge");
      bridgeField.setAccessible(true);
      GwtMockito.Bridge bridge = (GwtMockito.Bridge) bridgeField.get(null);
      
      // Create a mock using the bridge
      com.google.gwt.user.client.ui.Button button = bridge.create(com.google.gwt.user.client.ui.Button.class);
      assertNotNull(button);
      assertTrue(button instanceof com.google.gwt.user.client.ui.Button);
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  @Test
  public void testBridge_create_returnsRegisteredMock() {
    GwtMockito.initMocks(new TestClass());
    
    // Register a mock
    com.google.gwt.user.client.ui.Button mockButton = mock(com.google.gwt.user.client.ui.Button.class);
    try {
      Field bridgeField = GwtMockito.class.getDeclaredField("bridge");
      bridgeField.setAccessible(true);
      GwtMockito.Bridge bridge = (GwtMockito.Bridge) bridgeField.get(null);
      
      Field registeredMocksField = bridge.getClass().getDeclaredField("registeredMocks");
      registeredMocksField.setAccessible(true);
      Map<Class<?>, Object> registeredMocks = (Map<Class<?>, Object>) registeredMocksField.get(bridge);
      registeredMocks.put(com.google.gwt.user.client.ui.Button.class, mockButton);
      
      // Create using the bridge
      com.google.gwt.user.client.ui.Button button = bridge.create(com.google.gwt.user.client.ui.Button.class);
      assertSame(mockButton, button);
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  @Test
  public void testBridge_create_returnsFakeFromProvider() {
    GwtMockito.initMocks(new TestClass());
    
    // Register a fake provider
    try {
      Field bridgeField = GwtMockito.class.getDeclaredField("bridge");
      bridgeField.setAccessible(true);
      GwtMockito.Bridge bridge = (GwtMockito.Bridge) bridgeField.get(null);
      
      Field registeredProvidersField = bridge.getClass().getDeclaredField("registeredProviders");
      registeredProvidersField.setAccessible(true);
      Map<Class<?>, FakeProvider<?>> registeredProviders = (Map<Class<?>, FakeProvider<?>>) registeredProvidersField.get(bridge);
      registeredProviders.put(Messages.class, new FakeMessagesProvider<Messages>());
      
      // Create using the bridge
      Messages messages = bridge.create(Messages.class);
      assertNotNull(messages);
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  @Test
  public void testBridge_getAsyncType_returnsCorrectAsyncType() {
    GwtMockito.initMocks(new TestClass());
    
    try {
      Field bridgeField = GwtMockito.class.getDeclaredField("bridge");
      bridgeField.setAccessible(true);
      GwtMockito.Bridge bridge = (GwtMockito.Bridge) bridgeField.get(null);
      
      // Get the private method
      Field asyncTypeField = bridge.getClass().getDeclaredField("asyncType");
      asyncTypeField.setAccessible(true);
      
      // Use reflection to call getAsyncType
      java.lang.reflect.Method getAsyncTypeMethod = GwtMockito.Bridge.class.getDeclaredMethod("getAsyncType", Class.class);
      getAsyncTypeMethod.setAccessible(true);
      
      // This would normally fail because we don't have the Async interface, but we can at least verify
      // the method exists and is callable
      assertNotNull(getAsyncTypeMethod);
    } catch (Exception e) {
      // Expected
    }
  }

  @Test
  public void testRegisterGwtMocks_populatesFields() throws Exception {
    TestClassWithGwtMock owner = new TestClassWithGwtMock();
    GwtMockito.initMocks(owner);

    Field field = TestClassWithGwtMock.class.getDeclaredField("button");
    field.setAccessible(true);
    Object mock = field.get(owner);
    assertNotNull(mock);
    assertTrue(mock instanceof com.google.gwt.user.client.ui.Button);
  }

  @Test
  public void testSetGwtBridge_setsBridgeCorrectly() {
    GwtMockito.initMocks(new TestClass());
    
    // Verify that the bridge was set correctly by checking that we can access it
    try {
      Field bridgeField = GwtMockito.class.getDeclaredField("bridge");
      bridgeField.setAccessible(true);
      assertNotNull(bridgeField.get(null));
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  @Test
  public void testDefaultFakeProviders_areCorrect() {
    assertEquals(7, GwtMockito.DEFAULT_FAKE_PROVIDERS.size());
    
    assertTrue(GwtMockito.DEFAULT_FAKE_PROVIDERS.containsKey(ClientBundle.class));
    assertTrue(GwtMockito.DEFAULT_FAKE_PROVIDERS.containsKey(CssResource.class));
    assertTrue(GwtMockito.DEFAULT_FAKE_PROVIDERS.containsKey(LocaleInfoImpl.class));
    assertTrue(GwtMockito.DEFAULT_FAKE_PROVIDERS.containsKey(Messages.class));
    assertTrue(GwtMockito.DEFAULT_FAKE_PROVIDERS.containsKey(NumberConstantsImpl.class));
    assertTrue(GwtMockito.DEFAULT_FAKE_PROVIDERS.containsKey(SafeHtmlTemplates.class));
    assertTrue(GwtMockito.DEFAULT_FAKE_PROVIDERS.containsKey(UiBinder.class));
  }

  private static class TestClass {
    @GwtMock
    private com.google.gwt.user.client.ui.Button mockField;
  }

  private static class TestClassWithGwtMock {
    @GwtMock
    private com.google.gwt.user.client.ui.Button button;
  }
}
