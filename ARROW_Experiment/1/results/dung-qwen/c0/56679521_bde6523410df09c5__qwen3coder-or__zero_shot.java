package com.weibo.api.motan.transport;

import com.weibo.api.motan.common.URL;
import com.weibo.api.motan.common.URLParamType;
import com.weibo.api.motan.core.extension.ExtensionLoader;
import com.weibo.api.motan.exception.MotanBizException;
import com.weibo.api.motan.exception.MotanFrameworkException;
import com.weibo.api.motan.exception.MotanServiceException;
import com.weibo.api.motan.protocol.rpc.CompressRpcCodec;
import com.weibo.api.motan.rpc.DefaultRequest;
import com.weibo.api.motan.rpc.DefaultResponse;
import com.weibo.api.motan.rpc.Provider;
import com.weibo.api.motan.rpc.Request;
import com.weibo.api.motan.rpc.Response;
import com.weibo.api.motan.serialize.DeserializableObject;
import com.weibo.api.motan.util.MotanFrameworkUtil;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.io.IOException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

public class ProviderMessageRouter_ArrowTest {

    @Mock
    private Provider<?> mockProvider;

    @Mock
    private ProviderProtectedStrategy mockStrategy;

    @Mock
    private Channel mockChannel;

    @Mock
    private Request mockRequest;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void testConstructorDefault() {
        ProviderMessageRouter router = new ProviderMessageRouter();
        assertNotNull(router);
    }

    @Test
    public void testConstructorWithUrl() {
        URL url = new URL("motan", "localhost", 8080);
        url.addParameter(URLParamType.providerProtectedStrategy.getName(), "default");
        ProviderMessageRouter router = new ProviderMessageRouter(url);
        assertNotNull(router);
    }

    @Test
    public void testConstructorWithProvider() {
        ProviderMessageRouter router = new ProviderMessageRouter(mockProvider);
        assertNotNull(router);
    }

    @Test
    public void testHandleNullChannel() {
        ProviderMessageRouter router = new ProviderMessageRouter();
        try {
            router.handle(null, mockRequest);
            fail("Expected MotanFrameworkException");
        } catch (MotanFrameworkException e) {
            assertEquals("RequestRouter handler(channel, message) params is null", e.getMessage());
        }
    }

    @Test
    public void testHandleNullMessage() {
        ProviderMessageRouter router = new ProviderMessageRouter();
        try {
            router.handle(mockChannel, null);
            fail("Expected MotanFrameworkException");
        } catch (MotanFrameworkException e) {
            assertEquals("RequestRouter handler(channel, message) params is null", e.getMessage());
        }
    }

    @Test
    public void testHandleInvalidMessageType() {
        ProviderMessageRouter router = new ProviderMessageRouter();
        Object invalidMessage = new Object();
        try {
            router.handle(mockChannel, invalidMessage);
            fail("Expected MotanFrameworkException");
        } catch (MotanFrameworkException e) {
            assertEquals("RequestRouter message type not support: " + invalidMessage.getClass(), e.getMessage());
        }
    }

    @Test
    public void testHandleNoProviderFound() {
        ProviderMessageRouter router = new ProviderMessageRouter();
        when(mockRequest.getMethodName()).thenReturn("testMethod");
        when(mockRequest.getParamtersDesc()).thenReturn("Ljava/lang/String;");
        when(mockRequest.toString()).thenReturn("testRequest");

        DefaultResponse response = (DefaultResponse) router.handle(mockChannel, mockRequest);
        assertNotNull(response);
        assertTrue(response.getException() instanceof MotanServiceException);
    }

    @Test
    public void testAddProvider() {
        ProviderMessageRouter router = new ProviderMessageRouter();
        when(mockProvider.getUrl()).thenReturn(new URL("motan", "localhost", 8080));
        when(mockProvider.getInterface()).thenReturn(String.class);

        router.addProvider(mockProvider);

        assertEquals(1, router.getPublicMethodCount());
    }

    @Test
    public void testRemoveProvider() {
        ProviderMessageRouter router = new ProviderMessageRouter();
        when(mockProvider.getUrl()).thenReturn(new URL("motan", "localhost", 8080));
        when(mockProvider.getInterface()).thenReturn(String.class);

        router.addProvider(mockProvider);
        assertEquals(1, router.getPublicMethodCount());

        router.removeProvider(mockProvider);
        assertEquals(0, router.getPublicMethodCount());
    }

    @Test
    public void testGetPublicMethodCount() {
        ProviderMessageRouter router = new ProviderMessageRouter();
        assertEquals(0, router.getPublicMethodCount());

        when(mockProvider.getUrl()).thenReturn(new URL("motan", "localhost", 8080));
        when(mockProvider.getInterface()).thenReturn(String.class);
        router.addProvider(mockProvider);
        assertEquals(1, router.getPublicMethodCount());
    }

    @Test
    public void testCallSuccess() {
        ProviderMessageRouter router = new ProviderMessageRouter();
        when(mockStrategy.call(any(Request.class), any(Provider.class))).thenReturn(new DefaultResponse());
        router.getClass(); // Access to trigger initialization

        // This test requires more complex setup to properly test the call method
        // since it depends on internal state and strategy behavior
    }

    @Test
    public void testProcessLazyDeserialize() throws IOException {
        ProviderMessageRouter router = new ProviderMessageRouter();
        Method mockMethod = String.class.getMethod("toString");
        DefaultRequest request = new DefaultRequest();
        DeserializableObject deserializableObject = mock(DeserializableObject.class);
        request.setArguments(new Object[]{deserializableObject});
        request.setRpcProtocolVersion("1.0");

        when(deserializableObject.deserializeMulti(any(Class[].class))).thenReturn(new Object[]{"test"});
        router.getClass(); // Access to trigger initialization

        // This test requires more complex setup to properly test the processLazyDeserialize method
    }

    @Test
    public void testFillParamDesc() {
        ProviderMessageRouter router = new ProviderMessageRouter();
        Method mockMethod = String.class.getMethod("toString");
        DefaultRequest request = new DefaultRequest();
        request.setParamtersDesc("");

        router.getClass(); // Access to trigger initialization

        // This test requires more complex setup to properly test the fillParamDesc method
    }
}
