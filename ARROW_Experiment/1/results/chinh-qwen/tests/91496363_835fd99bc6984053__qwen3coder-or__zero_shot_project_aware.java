package org.dominokit.domino.service.discovery;

import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.servicediscovery.Record;
import io.vertx.servicediscovery.ServiceDiscovery;
import io.vertx.servicediscovery.ServiceReference;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.List;
import java.util.function.Function;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertSame;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.*;

public class VertxServiceDiscovery_ArrowTest {

    @Mock
    private Vertx vertx;

    @Mock
    private ServiceDiscovery serviceDiscovery;

    @Mock
    private Record record;

    @Mock
    private JsonObject jsonObject;

    @Mock
    private AsyncResult<Record> asyncResultRecord;

    @Mock
    private AsyncResult<List<Record>> asyncResultList;

    @Mock
    private ServiceReference serviceReference;

    private VertxServiceDiscovery vertxServiceDiscovery;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        when(ServiceDiscovery.create(vertx)).thenReturn(serviceDiscovery);
        vertxServiceDiscovery = new VertxServiceDiscovery(vertx);
    }

    @Test
    public void shouldCreateServiceDiscoveryInstance() {
        assertThat(vertxServiceDiscovery.serviceDiscovery()).isSameAs(serviceDiscovery);
    }

    @Test
    public void shouldReturnHttpServiceDiscovery() {
        HttpServiceDiscovery httpServiceDiscovery = vertxServiceDiscovery.http();
        assertThat(httpServiceDiscovery).isNotNull();
    }

    @Test
    public void shouldReturnEventBusServiceDiscovery() {
        EventBusServiceDiscovery eventBusServiceDiscovery = vertxServiceDiscovery.eventBus();
        assertThat(eventBusServiceDiscovery).isNotNull();
    }

    @Test
    public void shouldReturnMessageSourceServiceDiscovery() {
        MessageSourceServiceDiscovery messageSourceServiceDiscovery = vertxServiceDiscovery.messageSource();
        assertThat(messageSourceServiceDiscovery).isNotNull();
    }

    @Test
    public void shouldReturnJDBCServiceDiscovery() {
        JDBCServiceDiscovery jdbcServiceDiscovery = vertxServiceDiscovery.jdbc();
        assertThat(jdbcServiceDiscovery).isNotNull();
    }

    @Test
    public void shouldReturnRedisServiceDiscovery() {
        RedisServiceDiscovery redisServiceDiscovery = vertxServiceDiscovery.redis();
        assertThat(redisServiceDiscovery).isNotNull();
    }

    @Test
    public void shouldReturnMongoServiceDiscovery() {
        MongoServiceDiscovery mongoServiceDiscovery = vertxServiceDiscovery.mongo();
        assertThat(mongoServiceDiscovery).isNotNull();
    }

    @Test
    public void shouldUnpublishRecord() {
        Handler<AsyncResult<Void>> handler = mock(Handler.class);

        vertxServiceDiscovery.unpublish(record, handler);

        verify(serviceDiscovery).unpublish(eq(record.getRegistration()), eq(handler));
    }

    @Test
    public void shouldPublishRecord() {
        Handler<AsyncResult<Record>> handler = mock(Handler.class);

        vertxServiceDiscovery.publishRecord(record, handler);

        verify(serviceDiscovery).publish(eq(record), eq(handler));
    }

    @Test
    public void shouldLookupWithFilter() {
        Function<Record, Boolean> filter = mock(Function.class);
        Handler<AsyncResult<Record>> handler = mock(Handler.class);

        vertxServiceDiscovery.lookup(filter, handler);

        verify(serviceDiscovery).getRecord(eq(filter), eq(false), eq(handler));
    }

    @Test
    public void shouldLookupIncludeOutOfServiceWithFilter() {
        Function<Record, Boolean> filter = mock(Function.class);
        Handler<AsyncResult<Record>> handler = mock(Handler.class);

        vertxServiceDiscovery.lookupIncludeOutOfService(filter, handler);

        verify(serviceDiscovery).getRecord(eq(filter), eq(true), eq(handler));
    }

    @Test(expected = VertxServiceDiscovery.InvalidFilterException.class)
    public void shouldThrowInvalidFilterExceptionWhenLookupWithNullFilter() {
        vertxServiceDiscovery.lookup(null, mock(Handler.class));
    }

    @Test(expected = VertxServiceDiscovery.InvalidFilterException.class)
    public void shouldThrowInvalidFilterExceptionWhenLookupIncludeOutOfServiceWithNullFilter() {
        vertxServiceDiscovery.lookupIncludeOutOfService(null, mock(Handler.class));
    }

    @Test
    public void shouldLookupByJson() {
        Handler<AsyncResult<Record>> handler = mock(Handler.class);

        vertxServiceDiscovery.lookupByJson(jsonObject, handler);

        verify(serviceDiscovery).getRecord(eq(jsonObject), eq(handler));
    }

    @Test
    public void shouldLookupAllWithFilter() {
        Function<Record, Boolean> filter = mock(Function.class);
        Handler<AsyncResult<List<Record>>> handler = mock(Handler.class);

        vertxServiceDiscovery.lookupAll(filter, handler);

        verify(serviceDiscovery).getRecords(eq(filter), eq(false), eq(handler));
    }

    @Test
    public void shouldLookupAllIncludeOutOfServiceWithFilter() {
        Function<Record, Boolean> filter = mock(Function.class);
        Handler<AsyncResult<List<Record>>> handler = mock(Handler.class);

        vertxServiceDiscovery.lookupAllIncludeOutOfService(filter, handler);

        verify(serviceDiscovery).getRecords(eq(filter), eq(true), eq(handler));
    }

    @Test(expected = VertxServiceDiscovery.InvalidFilterException.class)
    public void shouldThrowInvalidFilterExceptionWhenLookupAllWithNullFilter() {
        vertxServiceDiscovery.lookupAll(null, mock(Handler.class));
    }

    @Test(expected = VertxServiceDiscovery.InvalidFilterException.class)
    public void shouldThrowInvalidFilterExceptionWhenLookupAllIncludeOutOfServiceWithNullFilter() {
        vertxServiceDiscovery.lookupAllIncludeOutOfService(null, mock(Handler.class));
    }

    @Test
    public void shouldLookupAllByJson() {
        Handler<AsyncResult<List<Record>>> handler = mock(Handler.class);

        vertxServiceDiscovery.lookupAllByJson(jsonObject, handler);

        verify(serviceDiscovery).getRecords(eq(jsonObject), eq(handler));
    }

    @Test
    public void shouldLookupForAReference() {
        when(serviceDiscovery.getReference(record)).thenReturn(serviceReference);

        ServiceReference result = vertxServiceDiscovery.lookupForAReference(record);

        assertSame(serviceReference, result);
    }

    @Test
    public void shouldLookupForAReferenceWithConfiguration() {
        JsonObject configuration = mock(JsonObject.class);
        when(serviceDiscovery.getReferenceWithConfiguration(record, configuration)).thenReturn(serviceReference);

        ServiceReference result = vertxServiceDiscovery.lookupForAReferenceWithConfiguration(record, configuration);

        assertSame(serviceReference, result);
    }

    @Test
    public void shouldReleaseServiceObject() {
        Object object = new Object();

        vertxServiceDiscovery.releaseServiceObject(object);

        verify(ServiceDiscovery.class);
        ServiceDiscovery.releaseServiceObject(serviceDiscovery, object);
    }
}
