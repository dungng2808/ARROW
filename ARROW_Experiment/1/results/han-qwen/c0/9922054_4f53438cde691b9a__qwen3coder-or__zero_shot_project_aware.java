package com.google.gwtmockito;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
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

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.lang.reflect.Field;
import java.util.Map;

public class GwtMockito_ArrowTest {

  @Before
  public void setUp() {
    // Ensure clean state before each test
    GwtMockito.tearDown();
  }

  @After
  public void tearDown() {
    GwtMockito.tearDown();
  }

  @Test
  public void testInitMocks_SetsUpBridgeAndRegistersDefaults() {
    // Arrange
    TestClass owner = new TestClass();

    // Act
    GwtMockito.initMocks(owner);

    // Assert
    assertNotNull("Bridge should be set", getPrivateBridge());
    assertEquals("Should have default providers", 7, getRegisteredProviders().size());
  }

  @Test
  public void testTearDown_ResetsBridge() {
    // Arrange
    TestClass owner = new TestClass();
    GwtMockito.initMocks(owner);

    // Act
    GwtMockito.tearDown();

    // Assert
    assertNull("Bridge should be reset", getPrivateBridge());
  }

  @Test
  public void testUseProviderForType_RegistersProvider() {
    // Arrange
    TestClass owner = new TestClass();
    GwtMockito.initMocks(owner);
    FakeProvider<ClientBundle> provider = new FakeClientBundleProvider();

    // Act
    GwtMockito.useProviderForType(ClientBundle.class, provider);

    // Assert
    assertTrue("Provider should be registered", getRegisteredProviders().containsKey(ClientBundle.class));
  }

  @Test(expected = IllegalStateException.class)
  public void testUseProviderForType_ThrowsIfNotInitialized() {
    // Act & Assert
    GwtMockito.useProviderForType(ClientBundle.class, new FakeClientBundleProvider());
  }

  @Test(expected = IllegalArgumentException.class)
  public void testUseProviderForType_ThrowsIfGwtMockExists() {
    // Arrange
    TestClassWithGwtMock owner = new TestClassWithGwtMock();
    GwtMockito.initMocks(owner);

    // Act & Assert
    GwtMockito.useProviderForType(ClientBundle.class, new FakeClientBundleProvider());
  }

  @Test
  public void testGetFake_ReturnsFakeFromDefaultProviders() {
    // Act
    Messages fake = GwtMockito.getFake(Messages.class);

    // Assert
    assertNotNull("Should return a fake", fake);
    assertTrue("Should be instance of Messages", fake instanceof Messages);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testGetFake_ThrowsIfNoProvider() {
    // Act & Assert
    GwtMockito.getFake(RemoteService.class);
  }

  @Test
  public void testGetFakeFromProviderMap_FindsExactMatch() {
    // Arrange
    Map<Class<?>, FakeProvider<?>> providers = getPrivateDefaultProviders();

    // Act
    Object result = GwtMockito.getFakeFromProviderMap(Messages.class, providers);

    // Assert
    assertNotNull("Should find a provider", result);
    assertTrue("Should be instance of Messages", result instanceof Messages);
  }

  @Test
  public void testGetFakeFromProviderMap_FindsSuperclassMatch() {
    // Arrange
    Map<Class<?>, FakeProvider<?>> providers = getPrivateDefaultProviders();

    // Act
    Object result = GwtMockito.getFakeFromProviderMap(LocaleInfoImpl.class, providers);

    // Assert
    assertNotNull("Should find a provider", result);
    assertTrue("Should be instance of LocaleInfoImpl", result instanceof LocaleInfoImpl);
  }

  @Test
  public void testBridgeCreatesMockWhenNoProvider() {
    // Arrange
    TestClass owner = new TestClass();
    GwtMockito.initMocks(owner);

    // Act
    Object result = getPrivateBridge().create(String.class);

    // Assert
    assertNotNull("Should return a mock", result);
    assertTrue("Should be a mock", result instanceof String);
  }

  @Test
  public void testBridgeCreatesRegisteredMock() {
    // Arrange
    TestClass owner = new TestClass();
    GwtMockito.initMocks(owner);
    MockTestClass mock = mock(MockTestClass.class);
    getPrivateBridge().registeredMocks.put(MockTestClass.class, mock);

    // Act
    Object result = getPrivateBridge().create(MockTestClass.class);

    // Assert
    assertSame("Should return registered mock", mock, result);
  }

  @Test
  public void testBridgeCreatesFakeFromProvider() {
    // Arrange
    TestClass owner = new TestClass();
    GwtMockito.initMocks(owner);
    GwtMockito.useProviderForType(Messages.class, new FakeMessagesProvider<Messages>());

    // Act
    Object result = getPrivateBridge().create(Messages.class);

    // Assert
    assertNotNull("Should return a fake", result);
    assertTrue("Should be instance of Messages", result instanceof Messages);
  }

  @Test
  public void testBridgeCreatesAsyncTypeForRemoteService() {
    // Arrange
    TestClass owner = new TestClass();
    GwtMockito.initMocks(owner);

    // Act
    Object result = getPrivateBridge().create(TestRemoteService.class);

    // Assert
    assertNotNull("Should return a mock", result);
    assertTrue("Should be instance of TestRemoteServiceAsync", result instanceof TestRemoteServiceAsync);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testBridgeCreatesAsyncType_ThrowsIfNoAsyncInterface() {
    // Arrange
    TestClass owner = new TestClass();
    GwtMockito.initMocks(owner);

    // Act & Assert
    getPrivateBridge().create(TestRemoteServiceWithoutAsync.class);
  }

  @Test
  public void testRegisterGwtMocks_InjectsMockIntoField() throws Exception {
    // Arrange
    TestClassWithGwtMock owner = new TestClassWithGwtMock();
    GwtMockito.initMocks(owner);

    // Act
    Field field = TestClassWithGwtMock.class.getDeclaredField("mockField");
    field.setAccessible(true);
    Object result = field.get(owner);

    // Assert
    assertNotNull("Should inject mock into field", result);
    assertTrue("Should be a mock", result instanceof Messages);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testRegisterGwtMocks_ThrowsOnMultipleMocks() throws Exception {
    // Arrange
    TestClassWithMultipleGwtMocks owner = new TestClassWithMultipleGwtMocks();

    // Act & Assert
    GwtMockito.initMocks(owner);
  }

  // Helper methods to access private fields
  private GWTBridge getPrivateBridge() {
    try {
      Field bridgeField = GwtMockito.class.getDeclaredField("bridge");
      bridgeField.setAccessible(true);
      return (GWTBridge) bridgeField.get(null);
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  private Map<Class<?>, FakeProvider<?>> getRegisteredProviders() {
    try {
      Field providersField = GwtMockito.Bridge.class.getDeclaredField("registeredProviders");
      providersField.setAccessible(true);
      return (Map<Class<?>, FakeProvider<?>>) providersField.get(getPrivateBridge());
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  private Map<Class<?>, FakeProvider<?>> getPrivateDefaultProviders() {
    try {
      Field providersField = GwtMockito.class.getDeclaredField("DEFAULT_FAKE_PROVIDERS");
      providersField.setAccessible(true);
      return (Map<Class<?>, FakeProvider<?>>) providersField.get(null);
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  // Test classes
  private static class TestClass {
  }

  private static class TestClassWithGwtMock {
    @GwtMock
    Messages mockField;
  }

  private static class TestClassWithMultipleGwtMocks {
    @GwtMock
    Messages mockField1;

    @GwtMock
    Messages mockField2;
  }

  private static class MockTestClass {
  }

  private static class TestRemoteService implements RemoteService {
  }

  private static class TestRemoteServiceAsync extends TestRemoteService {
  }

  private static class TestRemoteServiceWithoutAsync implements RemoteService {
  }
}
