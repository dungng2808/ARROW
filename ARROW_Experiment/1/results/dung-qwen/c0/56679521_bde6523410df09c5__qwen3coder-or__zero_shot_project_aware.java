package com.weibo.api.motan.transport;

import com.weibo.api.motan.common.URLParamType;
import com.weibo.api.motan.core.extension.ExtensionLoader;
import com.weibo.api.motan.exception.MotanFrameworkException;
import com.weibo.api.motan.exception.MotanServiceException;
import com.weibo.api.motan.protocol.rpc.CompressRpcCodec;
import com.weibo.api.motan.rpc.*;
import com.weibo.api.motan.serialize.DeserializableObject;
import com.weibo.api.motan.util.MotanFrameworkUtil;
import com.weibo.api.motan.util.ReflectUtil;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.io.IOException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

public class ProviderMessageRouter_ArrowTest {

    @Mock
    private Provider<?> mockProvider;

    @Mock
    private Channel mockChannel;

    @Mock
    private Request mockRequest;

    @Mock
    private Response mockResponse;

    @Mock
    private ProviderProtectedStrategy mockStrategy;

    @Mock
    private Method mockMethod;

    private ProviderMessageRouter router;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        router = new ProviderMessageRouter();
    }

    @Test
    public void testConstructorDefault() {
        assertNotNull(router);
        assertNotNull(router.getClass().getDeclaredField("strategy"));
    }

    @Test
    public void testConstructorWithUrl() {
        URL url = mock(URL.class);
        when(url.getParameter(eq(URLParamType.providerProtectedStrategy.getName()), anyString())).thenReturn("default");

        ProviderMessageRouter routerWithUrl = new ProviderMessageRouter(url);
        assertNotNull(routerWithUrl);
    }

    @Test
    public void testConstructorWithProvider() {
        Provider<?> provider = mock(Provider.class);
        URL url = mock(URL.class);
        when(provider.getUrl()).thenReturn(url);
        when(url.getParameter(anyString(), anyString())).thenReturn("default");

        ProviderMessageRouter routerWithProvider = new ProviderMessageRouter(provider);
        assertNotNull(routerWithProvider);
    }

    @Test(expected = MotanFrameworkException.class)
    public void testHandleNullChannel() throws Exception {
        Request request = mock(Request.class);
        router.handle(null, request);
    }

    @Test(expected = MotanFrameworkException.class)
    public void testHandleNullMessage() throws Exception {
        router.handle(mockChannel, null);
    }

    @Test(expected = MotanFrameworkException.class)
    public void testHandleInvalidMessageType() throws Exception {
        router.handle(mockChannel, "invalid message");
    }

    @Test
    public void testHandleProviderNotFound() throws Exception {
        Request request = mock(Request.class);
        when(request.getMethodName()).thenReturn("testMethod");
        when(request.getParamtersDesc()).thenReturn("testDesc");
        when(request.getSerializeNumber()).thenReturn((short) 1);
        when(request.getRpcProtocolVersion()).thenReturn("1.0");

        String serviceKey = "testGroup/testInterface/testVersion";
        when(MotanFrameworkUtil.getServiceKey(eq(request))).thenReturn(serviceKey);

        Object result = router.handle(mockChannel, request);
        assertThat(result, instanceOf(Response.class));
    }

    @Test
    public void testAddProvider() throws Exception {
        Provider<?> provider = mock(Provider.class);
        URL url = mock(URL.class);
        when(provider.getUrl()).thenReturn(url);
        when(url.getParameter(anyString(), anyString())).thenReturn("default");
        when(provider.getInterface()).thenReturn(String.class);

        List<Method> methods = mock(List.class);
        when(ReflectUtil.getPublicMethod(String.class)).thenReturn(methods);
        when(methods.size()).thenReturn(2);

        router.addProvider(provider);

        assertEquals(2, router.getPublicMethodCount());
    }

    @Test
    public void testRemoveProvider() throws Exception {
        Provider<?> provider = mock(Provider.class);
        URL url = mock(URL.class);
        when(provider.getUrl()).thenReturn(url);
        when(url.getParameter(anyString(), anyString())).thenReturn("default");
        when(provider.getInterface()).thenReturn(String.class);

        List<Method> methods = mock(List.class);
        when(ReflectUtil.getPublicMethod(String.class)).thenReturn(methods);
        when(methods.size()).thenReturn(2);

        router.addProvider(provider);
        assertEquals(2, router.getPublicMethodCount());

        router.removeProvider(provider);
        assertEquals(0, router.getPublicMethodCount());
    }

    @Test(expected = MotanFrameworkException.class)
    public void testAddProviderAlreadyExists() throws Exception {
        Provider<?> provider = mock(Provider.class);
        URL url = mock(URL.class);
        when(provider.getUrl()).thenReturn(url);
        when(url.getParameter(anyString(), anyString())).thenReturn("default");

        router.addProvider(provider);
        router.addProvider(provider);
    }

    @Test
    public void testCallSuccess() throws Exception {
        Request request = mock(Request.class);
        Provider<?> provider = mock(Provider.class);
        when(provider.getUrl()).thenReturn(mock(URL.class));
        when(request.getSerializeNumber()).thenReturn((short) 1);
        when(request.getRpcProtocolVersion()).thenReturn("1.0");

        when(mockStrategy.call(eq(request), eq(provider))).thenReturn(mockResponse);

        // Set up the strategy field via reflection
        java.lang.reflect.Field strategyField = ProviderMessageRouter.class.getDeclaredField("strategy");
        strategyField.setAccessible(true);
        strategyField.set(router, mockStrategy);

        Response result = router.call(request, provider);
        assertEquals(mockResponse, result);
    }

    @Test
    public void testCallException() throws Exception {
        Request request = mock(Request.class);
        Provider<?> provider = mock(Provider.class);
        when(provider.getUrl()).thenReturn(mock(URL.class));

        when(mockStrategy.call(eq(request), eq(provider))).thenThrow(new RuntimeException("test"));

        // Set up the strategy field via reflection
        java.lang.reflect.Field strategyField = ProviderMessageRouter.class.getDeclaredField("strategy");
        strategyField.setAccessible(true);
        strategyField.set(router, mockStrategy);

        Response result = router.call(request, provider);
        assertThat(result, instanceOf(Response.class));
    }

    @Test
    public void testFillParamDescWhenMethodIsNull() {
        Request request = mock(DefaultRequest.class);
        when(request.getParamtersDesc()).thenReturn(null);
        Method method = null;

        router.getClass().getDeclaredMethod("fillParamDesc", Request.class, Method.class).invoke(router, request, method);
        verify(request, never()).setParamtersDesc(anyString());
    }

    @Test
    public void testFillParamDescWhenParamDescIsNotBlank() {
        Request request = mock(DefaultRequest.class);
        when(request.getParamtersDesc()).thenReturn("someDesc");
        Method method = mock(Method.class);

        router.getClass().getDeclaredMethod("fillParamDesc", Request.class, Method.class).invoke(router, request, method);
        verify(request, never()).setParamtersDesc(anyString());
    }

    @Test
    public void testFillParamDescNormalCase() throws Exception {
        Request request = mock(DefaultRequest.class);
        when(request.getParamtersDesc()).thenReturn("");
        Method method = mock(Method.class);
        when(ReflectUtil.getMethodParamDesc(method)).thenReturn("paramDesc");

        router.getClass().getDeclaredMethod("fillParamDesc", Request.class, Method.class).invoke(router, request, method);
        verify(request).setParamtersDesc("paramDesc");
    }

    @Test
    public void testProcessLazyDeserializeWhenMethodIsNull() {
        Request request = mock(DefaultRequest.class);
        Method method = null;

        router.getClass().getDeclaredMethod("processLazyDeserialize", Request.class, Method.class).invoke(router, request, method);
        verify(request, never()).getArguments();
    }

    @Test
    public void testProcessLazyDeserializeNormalCase() throws Exception {
        Request request = mock(DefaultRequest.class);
        Method method = mock(Method.class);
        Object[] args = {"arg1"};
        DeserializableObject deserializableObject = mock(DeserializableObject.class);
        when(deserializableObject.deserializeMulti(any(Class[].class))).thenReturn(args);
        when(request.getArguments()).thenReturn(new Object[]{deserializableObject});

        router.getClass().getDeclaredMethod("processLazyDeserialize", Request.class, Method.class).invoke(router, request, method);
        verify(request).setArguments(args);
    }

    @Test(expected = MotanFrameworkException.class)
    public void testProcessLazyDeserializeIOException() throws Exception {
        Request request = mock(DefaultRequest.class);
        Method method = mock(Method.class);
        DeserializableObject deserializableObject = mock(DeserializableObject.class);
        when(deserializableObject.deserializeMulti(any(Class[].class))).thenThrow(new IOException("test"));
        when(request.getArguments()).thenReturn(new Object[]{deserializableObject});

        router.getClass().getDeclaredMethod("processLazyDeserialize", Request.class, Method.class).invoke(router, request, method);
    }
}
