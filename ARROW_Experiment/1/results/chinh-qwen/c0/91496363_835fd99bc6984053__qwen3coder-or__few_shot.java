package org.dominokit.domino.service.discovery;

import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.servicediscovery.Record;
import io.vertx.servicediscovery.ServiceDiscovery;
import io.vertx.servicediscovery.ServiceReference;
import org.dominokit.domino.service.discovery.type.*;
import org.junit.Before;
import org.junit.Test;

import java.util.List;
import java.util.function.Function;

import static org.mockito.Mockito.*;

public class VertxServiceDiscovery_ArrowTest {

    private Vertx vertx;
    private ServiceDiscovery serviceDiscovery;
    private VertxServiceDiscovery vertxServiceDiscovery;

    @Before
    public void setUp() {
        vertx = mock(Vertx.class);
        serviceDiscovery = mock(ServiceDiscovery.class);
        vertxServiceDiscovery = new VertxServiceDiscovery(vertx);
        
        // Mock the ServiceDiscovery creation
        try {
            // We need to set up the serviceDiscovery field using reflection since it's private
            java.lang.reflect.Field field = VertxServiceDiscovery.class.getDeclaredField("serviceDiscovery");
            field.setAccessible(true);
            field.set(vertxServiceDiscovery, serviceDiscovery);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    public void testHttp() {
        HttpServiceDiscovery httpServiceDiscovery = vertxServiceDiscovery.http();
        assertNotNull(httpServiceDiscovery);
    }

    @Test
    public void testEventBus() {
        EventBusServiceDiscovery eventBusServiceDiscovery = vertxServiceDiscovery.eventBus();
        assertNotNull(eventBusServiceDiscovery);
    }

    @Test
    public void testMessageSource() {
        MessageSourceServiceDiscovery messageSourceServiceDiscovery = vertxServiceDiscovery.messageSource();
        assertNotNull(messageSourceServiceDiscovery);
    }

    @Test
    public void testJdbc() {
        JDBCServiceDiscovery jdbcServiceDiscovery = vertxServiceDiscovery.jdbc();
        assertNotNull(jdbcServiceDiscovery);
    }

    @Test
    public void testRedis() {
        RedisServiceDiscovery redisServiceDiscovery = vertxServiceDiscovery.redis();
        assertNotNull(redisServiceDiscovery);
    }

    @Test
    public void testMongo() {
        MongoServiceDiscovery mongoServiceDiscovery = vertxServiceDiscovery.mongo();
        assertNotNull(mongoServiceDiscovery);
    }

    @Test
    public void testUnpublish() {
        Record record = mock(Record.class);
        Handler<AsyncResult<Void>> handler = mock(Handler.class);
        
        vertxServiceDiscovery.unpublish(record, handler);
        
        verify(serviceDiscovery).unpublish(anyString(), eq(handler));
    }

    @Test
    public void testPublishRecord() {
        Record record = mock(Record.class);
        Handler<AsyncResult<Record>> handler = mock(Handler.class);
        
        vertxServiceDiscovery.publishRecord(record, handler);
        
        verify(serviceDiscovery).publish(eq(record), eq(handler));
    }

    @Test
    public void testLookup() {
        Function<Record, Boolean> filter = mock(Function.class);
        Handler<AsyncResult<Record>> handler = mock(Handler.class);
        
        vertxServiceDiscovery.lookup(filter, handler);
        
        verify(serviceDiscovery).getRecord(eq(filter), eq(false), eq(handler));
    }

    @Test
    public void testLookupIncludeOutOfService() {
        Function<Record, Boolean> filter = mock(Function.class);
        Handler<AsyncResult<Record>> handler = mock(Handler.class);
        
        vertxServiceDiscovery.lookupIncludeOutOfService(filter, handler);
        
        verify(serviceDiscovery).getRecord(eq(filter), eq(true), eq(handler));
    }

    @Test(expected = VertxServiceDiscovery.InvalidFilterException.class)
    public void testLookupWithNullFilter() {
        vertxServiceDiscovery.lookup(null, mock(Handler.class));
    }

    @Test
    public void testLookupByJson() {
        JsonObject jsonFilter = mock(JsonObject.class);
        Handler<AsyncResult<Record>> handler = mock(Handler.class);
        
        vertxServiceDiscovery.lookupByJson(jsonFilter, handler);
        
        verify(serviceDiscovery).getRecord(eq(jsonFilter), eq(handler));
    }

    @Test
    public void testLookupAll() {
        Function<Record, Boolean> filter = mock(Function.class);
        Handler<AsyncResult<List<Record>>> handler = mock(Handler.class);
        
        vertxServiceDiscovery.lookupAll(filter, handler);
        
        verify(serviceDiscovery).getRecords(eq(filter), eq(false), eq(handler));
    }

    @Test
    public void testLookupAllIncludeOutOfService() {
        Function<Record, Boolean> filter = mock(Function.class);
        Handler<AsyncResult<List<Record>>> handler = mock(Handler.class);
        
        vertxServiceDiscovery.lookupAllIncludeOutOfService(filter, handler);
        
        verify(serviceDiscovery).getRecords(eq(filter), eq(true), eq(handler));
    }

    @Test(expected = VertxServiceDiscovery.InvalidFilterException.class)
    public void testLookupAllWithNullFilter() {
        vertxServiceDiscovery.lookupAll(null, mock(Handler.class));
    }

    @Test
    public void testLookupAllByJson() {
        JsonObject jsonFilter = mock(JsonObject.class);
        Handler<AsyncResult<List<Record>>> handler = mock(Handler.class);
        
        vertxServiceDiscovery.lookupAllByJson(jsonFilter, handler);
        
        verify(serviceDiscovery).getRecords(eq(jsonFilter), eq(handler));
    }

    @Test
    public void testLookupForAReference() {
        Record record = mock(Record.class);
        ServiceReference reference = mock(ServiceReference.class);
        
        when(serviceDiscovery.getReference(eq(record))).thenReturn(reference);
        
        ServiceReference result = vertxServiceDiscovery.lookupForAReference(record);
        
        assertEquals(reference, result);
    }

    @Test
    public void testLookupForAReferenceWithConfiguration() {
        Record record = mock(Record.class);
        JsonObject configuration = mock(JsonObject.class);
        ServiceReference reference = mock(ServiceReference.class);
        
        when(serviceDiscovery.getReferenceWithConfiguration(eq(record), eq(configuration)))
                .thenReturn(reference);
        
        ServiceReference result = vertxServiceDiscovery.lookupForAReferenceWithConfiguration(record, configuration);
        
        assertEquals(reference, result);
    }

    @Test
    public void testReleaseServiceObject() {
        Object object = new Object();
        
        vertxServiceDiscovery.releaseServiceObject(object);
        
        verify(ServiceDiscovery.class);
        ServiceDiscovery.releaseServiceObject(eq(serviceDiscovery), eq(object));
    }
}
