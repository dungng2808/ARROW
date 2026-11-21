package org.dominokit.domino.service.discovery;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.fail;

import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.servicediscovery.Record;
import io.vertx.servicediscovery.ServiceDiscovery;
import java.util.function.Function;
import org.junit.Test;

public class VertxServiceDiscovery_ArrowTest {

  @Test
  public void serviceDiscoveryReturnsCreatedDiscovery() {
    Vertx vertx = Vertx.vertx();
    try {
      VertxServiceDiscovery discovery = new VertxServiceDiscovery(vertx);
      assertNotNull(discovery.serviceDiscovery());
    } finally {
      vertx.close();
    }
  }

  @Test
  public void discoveryAdaptersAreCreated() {
    Vertx vertx = Vertx.vertx();
    try {
      VertxServiceDiscovery discovery = new VertxServiceDiscovery(vertx);
      assertNotNull(discovery.http());
      assertNotNull(discovery.eventBus());
      assertNotNull(discovery.messageSource());
      assertNotNull(discovery.jdbc());
      assertNotNull(discovery.redis());
      assertNotNull(discovery.mongo());
    } finally {
      vertx.close();
    }
  }

  @Test
  public void lookupRejectsNullFilter() {
    Vertx vertx = Vertx.vertx();
    try {
      VertxServiceDiscovery discovery = new VertxServiceDiscovery(vertx);
      try {
        discovery.lookup(null, null);
        fail("Expected InvalidFilterException");
      } catch (VertxServiceDiscovery.InvalidFilterException expected) {
        // Expected.
      }
    } finally {
      vertx.close();
    }
  }

  @Test
  public void lookupIncludingOutOfServiceRejectsNullFilter() {
    Vertx vertx = Vertx.vertx();
    try {
      VertxServiceDiscovery discovery = new VertxServiceDiscovery(vertx);
      try {
        discovery.lookupIncludeOutOfService(null, null);
        fail("Expected InvalidFilterException");
      } catch (VertxServiceDiscovery.InvalidFilterException expected) {
        // Expected.
      }
    } finally {
      vertx.close();
    }
  }

  @Test
  public void lookupAllRejectsNullFilter() {
    Vertx vertx = Vertx.vertx();
    try {
      VertxServiceDiscovery discovery = new VertxServiceDiscovery(vertx);
      try {
        discovery.lookupAll(null, null);
        fail("Expected InvalidFilterException");
      } catch (VertxServiceDiscovery.InvalidFilterException expected) {
        // Expected.
      }
    } finally {
      vertx.close();
    }
  }

  @Test
  public void lookupAllIncludingOutOfServiceRejectsNullFilter() {
    Vertx vertx = Vertx.vertx();
    try {
      VertxServiceDiscovery discovery = new VertxServiceDiscovery(vertx);
      try {
        discovery.lookupAllIncludeOutOfService(null, null);
        fail("Expected InvalidFilterException");
      } catch (VertxServiceDiscovery.InvalidFilterException expected) {
        // Expected.
      }
    } finally {
      vertx.close();
    }
  }
}
