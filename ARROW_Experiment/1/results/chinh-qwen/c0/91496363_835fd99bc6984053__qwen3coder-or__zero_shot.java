package org.dominokit.domino.service.discovery;

import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.servicediscovery.Record;
import io.vertx.servicediscovery.ServiceDiscovery;
import io.vertx.servicediscovery.ServiceReference;
import org.junit.Test;
import org.mockito.Mockito;

import java.util.List;
import java.util.function.Function;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

public class VertxServiceDiscovery_ArrowTest {

    @Test
    public void testConstructor() {
        Vertx vertx = Mockito.mock(Vertx.class);
        ServiceDiscovery serviceDiscovery = Mockito.mock(ServiceDiscovery.class);
        when(ServiceDiscovery.create(vertx)).thenReturn(serviceDiscovery);

        VertxServiceDiscovery discovery = new VertxServiceDiscovery(vertx);
        assertNotNull(discovery);
    }

    @Test
    public void testServiceDiscoveryMethod() {
        Vertx vertx = Mockito.mock(Vertx.class);
        ServiceDiscovery serviceDiscovery = Mockito.mock(ServiceDiscovery.class);
        when(ServiceDiscovery.create(vertx)).thenReturn(serviceDiscovery);

        VertxServiceDiscovery discovery = new VertxServiceDiscovery(vertx);
        assertEquals(serviceDiscovery, discovery.serviceDiscovery());
    }

    @Test
    public void testHttpMethod() {
        Vertx vertx = Mockito.mock(Vertx.class);
        ServiceDiscovery serviceDiscovery = Mockito.mock(ServiceDiscovery.class);
        when(ServiceDiscovery.create(vertx)).thenReturn(serviceDiscovery);

        VertxServiceDiscovery discovery = new VertxServiceDiscovery(vertx);
        HttpServiceDiscovery httpServiceDiscovery = discovery.http();
        assertNotNull(httpServiceDiscovery);
    }

    @Test
    public void testEventBusMethod() {
        Vertx vertx = Mockito.mock(Vertx.class);
        ServiceDiscovery serviceDiscovery = Mockito.mock(ServiceDiscovery.class);
        when(ServiceDiscovery.create(vertx)).thenReturn(serviceDiscovery);

        VertxServiceDiscovery discovery = new VertxServiceDiscovery(vertx);
        EventBusServiceDiscovery eventBusServiceDiscovery = discovery.eventBus();
        assertNotNull(eventBusServiceDiscovery);
    }

    @Test
    public void testMessageSourceMethod() {
        Vertx vertx = Mockito.mock(Vertx.class);
        ServiceDiscovery serviceDiscovery = Mockito.mock(ServiceDiscovery.class);
        when(ServiceDiscovery.create(vertx)).thenReturn(serviceDiscovery);

        VertxServiceDiscovery discovery = new VertxServiceDiscovery(vertx);
        MessageSourceServiceDiscovery messageSourceServiceDiscovery = discovery.messageSource();
        assertNotNull(messageSourceServiceDiscovery);
    }

    @Test
    public void testJdbcMethod() {
        Vertx vertx = Mockito.mock(Vertx.class);
        ServiceDiscovery serviceDiscovery = Mockito.mock(ServiceDiscovery.class);
        when(ServiceDiscovery.create(vertx)).thenReturn(serviceDiscovery);

        VertxServiceDiscovery discovery = new VertxServiceDiscovery(vertx);
        JDBCServiceDiscovery jdbcServiceDiscovery = discovery.jdbc();
        assertNotNull(jdbcServiceDiscovery);
    }

    @Test
    public void testRedisMethod() {
        Vertx vertx = Mockito.mock(Vertx.class);
        ServiceDiscovery serviceDiscovery = Mockito.mock(ServiceDiscovery.class);
        when(ServiceDiscovery.create(vertx)).thenReturn(serviceDiscovery);

        VertxServiceDiscovery discovery = new VertxServiceDiscovery(vertx);
        RedisServiceDiscovery redisServiceDiscovery = discovery.redis();
        assertNotNull(redisServiceDiscovery);
    }

    @Test
    public void testMongoMethod() {
        Vertx vertx = Mockito.mock(Vertx.class);
        ServiceDiscovery serviceDiscovery = Mockito.mock(ServiceDiscovery.class);
        when(ServiceDiscovery.create(vertx)).thenReturn(serviceDiscovery);

        VertxServiceDiscovery discovery = new VertxServiceDiscovery(vertx);
        MongoServiceDiscovery mongoServiceDiscovery = discovery.mongo();
        assertNotNull(mongoServiceDiscovery);
    }

    @Test
    public void testUnpublish() {
        Vertx vertx = Mockito.mock(Vertx.class);
        ServiceDiscovery serviceDiscovery = Mockito.mock(ServiceDiscovery.class);
        when(ServiceDiscovery.create(vertx)).thenReturn(serviceDiscovery);

        VertxServiceDiscovery discovery = new VertxServiceDiscovery(vertx);
        Record record = Mockito.mock(Record.class);
        Handler<AsyncResult<Void>> handler = Mockito.mock(Handler.class);

        discovery.unpublish(record, handler);
        verify(serviceDiscovery).unpublish(any(String.class), eq(handler));
    }

    @Test
    public void testPublishRecord() {
        Vertx vertx = Mockito.mock(Vertx.class);
        ServiceDiscovery serviceDiscovery = Mockito.mock(ServiceDiscovery.class);
        when(ServiceDiscovery.create(vertx)).thenReturn(serviceDiscovery);

        VertxServiceDiscovery discovery = new VertxServiceDiscovery(vertx);
        Record record = Mockito.mock(Record.class);
        Handler<AsyncResult<Record>> handler = Mockito.mock(Handler.class);

        discovery.publishRecord(record, handler);
        verify(serviceDiscovery).publish(eq(record), eq(handler));
    }

    @Test
    public void testLookupWithFilter() {
        Vertx vertx = Mockito.mock(Vertx.class);
        ServiceDiscovery serviceDiscovery = Mockito.mock(ServiceDiscovery.class);
        when(ServiceDiscovery.create(vertx)).thenReturn(serviceDiscovery);

        VertxServiceDiscovery discovery = new VertxServiceDiscovery(vertx);
        Function<Record, Boolean> filter = Mockito.mock(Function.class);
        Handler<AsyncResult<Record>> handler = Mockito.mock(Handler.class);

        discovery.lookup(filter, handler);
        verify(serviceDiscovery).getRecord(eq(filter), eq(false), eq(handler));
    }

    @Test
    public void testLookupIncludeOutOfService() {
        Vertx vertx = Mockito.mock(Vertx.class);
        ServiceDiscovery serviceDiscovery = Mockito.mock(ServiceDiscovery.class);
        when(ServiceDiscovery.create(vertx)).thenReturn(serviceDiscovery);

        VertxServiceDiscovery discovery = new VertxServiceDiscovery(vertx);
        Function<Record, Boolean> filter = Mockito.mock(Function.class);
        Handler<AsyncResult<Record>> handler = Mockito.mock(Handler.class);

        discovery.lookupIncludeOutOfService(filter, handler);
        verify(serviceDiscovery).getRecord(eq(filter), eq(true), eq(handler));
    }

    @Test(expected = VertxServiceDiscovery.InvalidFilterException.class)
    public void testLookupWithNullFilter() {
        Vertx vertx = Mockito.mock(Vertx.class);
        ServiceDiscovery serviceDiscovery = Mockito.mock(ServiceDiscovery.class);
        when(ServiceDiscovery.create(vertx)).thenReturn(serviceDiscovery);

        VertxServiceDiscovery discovery = new VertxServiceDiscovery(vertx);
        discovery.lookup(null, Mockito.mock(Handler.class));
    }

    @Test
    public void testLookupByJson() {
        Vertx vertx = Mockito.mock(Vertx.class);
        ServiceDiscovery serviceDiscovery = Mockito.mock(ServiceDiscovery.class);
        when(ServiceDiscovery.create(vertx)).thenReturn(serviceDiscovery);

        VertxServiceDiscovery discovery = new VertxServiceDiscovery(vertx);
        JsonObject jsonFilter = Mockito.mock(JsonObject.class);
        Handler<AsyncResult<Record>> handler = Mockito.mock(Handler.class);

        discovery.lookupByJson(jsonFilter, handler);
        verify(serviceDiscovery).getRecord(eq(jsonFilter), eq(handler));
    }

    @Test
    public void testLookupAllWithFilter() {
        Vertx vertx = Mockito.mock(Vertx.class);
        ServiceDiscovery serviceDiscovery = Mockito.mock(ServiceDiscovery.class);
        when(ServiceDiscovery.create(vertx)).thenReturn(serviceDiscovery);

        VertxServiceDiscovery discovery = new VertxServiceDiscovery(vertx);
        Function<Record, Boolean> filter = Mockito.mock(Function.class);
        Handler<AsyncResult<List<Record>>> handler = Mockito.mock(Handler.class);

        discovery.lookupAll(filter, handler);
        verify(serviceDiscovery).getRecords(eq(filter), eq(false), eq(handler));
    }

    @Test
    public void testLookupAllIncludeOutOfService() {
        Vertx vertx = Mockito.mock(Vertx.class);
        ServiceDiscovery serviceDiscovery = Mockito.mock(ServiceDiscovery.class);
        when(ServiceDiscovery.create(vertx)).thenReturn(serviceDiscovery);

        VertxServiceDiscovery discovery = new VertxServiceDiscovery(vertx);
        Function<Record, Boolean> filter = Mockito.mock(Function.class);
        Handler<AsyncResult<List<Record>>> handler = Mockito.mock(Handler.class);

        discovery.lookupAllIncludeOutOfService(filter, handler);
        verify(serviceDiscovery).getRecords(eq(filter), eq(true), eq(handler));
    }

    @Test(expected = VertxServiceDiscovery.InvalidFilterException.class)
    public void testLookupAllWithNullFilter() {
        Vertx vertx = Mockito.mock(Vertx.class);
        ServiceDiscovery serviceDiscovery = Mockito.mock(ServiceDiscovery.class);
        when(ServiceDiscovery.create(vertx)).thenReturn(serviceDiscovery);

        VertxServiceDiscovery discovery = new VertxServiceDiscovery(vertx);
        discovery.lookupAll(null, Mockito.mock(Handler.class));
    }

    @Test
    public void testLookupAllByJson() {
        Vertx vertx = Mockito.mock(Vertx.class);
        ServiceDiscovery serviceDiscovery = Mockito.mock(ServiceDiscovery.class);
        when(ServiceDiscovery.create(vertx)).thenReturn(serviceDiscovery);

        VertxServiceDiscovery discovery = new VertxServiceDiscovery(vertx);
        JsonObject jsonFilter = Mockito.mock(JsonObject.class);
        Handler<AsyncResult<List<Record>>> handler = Mockito.mock(Handler.class);

        discovery.lookupAllByJson(jsonFilter, handler);
        verify(serviceDiscovery).getRecords(eq(jsonFilter), eq(handler));
    }

    @Test
    public void testLookupForAReference() {
        Vertx vertx = Mockito.mock(Vertx.class);
        ServiceDiscovery serviceDiscovery = Mockito.mock(ServiceDiscovery.class);
        when(ServiceDiscovery.create(vertx)).thenReturn(serviceDiscovery);

        VertxServiceDiscovery discovery = new VertxServiceDiscovery(vertx);
        Record record = Mockito.mock(Record.class);
        ServiceReference reference = Mockito.mock(ServiceReference.class);
        when(serviceDiscovery.getReference(record)).thenReturn(reference);

        ServiceReference result = discovery.lookupForAReference(record);
        assertEquals(reference, result);
    }

    @Test
    public void testLookupForAReferenceWithConfiguration() {
        Vertx vertx = Mockito.mock(Vertx.class);
        ServiceDiscovery serviceDiscovery = Mockito.mock(ServiceDiscovery.class);
        when(ServiceDiscovery.create(vertx)).thenReturn(serviceDiscovery);

        VertxServiceDiscovery discovery = new VertxServiceDiscovery(vertx);
        Record record = Mockito.mock(Record.class);
        JsonObject configuration = Mockito.mock(JsonObject.class);
        ServiceReference reference = Mockito.mock(ServiceReference.class);
        when(serviceDiscovery.getReferenceWithConfiguration(record, configuration)).thenReturn(reference);

        ServiceReference result = discovery.lookupForAReferenceWithConfiguration(record, configuration);
        assertEquals(reference, result);
    }

    @Test
    public void testReleaseServiceObject() {
        Vertx vertx = Mockito.mock(Vertx.class);
        ServiceDiscovery serviceDiscovery = Mockito.mock(ServiceDiscovery.class);
        when(ServiceDiscovery.create(vertx)).thenReturn(serviceDiscovery);

        VertxServiceDiscovery discovery = new VertxServiceDiscovery(vertx);
        Object object = new Object();

        discovery.releaseServiceObject(object);
        verify(ServiceDiscovery.class);
        ServiceDiscovery.releaseServiceObject(serviceDiscovery, object);
    }
}
