package org.dominokit.domino.service.discovery;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertThrows;

import io.vertx.core.Vertx;
import io.vertx.servicediscovery.ServiceDiscovery;
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
  public void discoveryAccessorsCreateWrappers() {
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
      assertThrows(
          VertxServiceDiscovery.InvalidFilterException.class,
          () -> discovery.lookup(null, result -> {}));
    } finally {
      vertx.close();
    }
  }

  @Test
  public void lookupIncludeOutOfServiceRejectsNullFilter() {
    Vertx vertx = Vertx.vertx();
    try {
      VertxServiceDiscovery discovery = new VertxServiceDiscovery(vertx);
      assertThrows(
          VertxServiceDiscovery.InvalidFilterException.class,
          () -> discovery.lookupIncludeOutOfService(null, result -> {}));
    } finally {
      vertx.close();
    }
  }

  @Test
  public void lookupAllRejectsNullFilter() {
    Vertx vertx = Vertx.vertx();
    try {
      VertxServiceDiscovery discovery = new VertxServiceDiscovery(vertx);
      assertThrows(
          VertxServiceDiscovery.InvalidFilterException.class,
          () -> discovery.lookupAll(null, result -> {}));
    } finally {
      vertx.close();
    }
  }

  @Test
  public void lookupAllIncludeOutOfServiceRejectsNullFilter() {
    Vertx vertx = Vertx.vertx();
    try {
      VertxServiceDiscovery discovery = new VertxServiceDiscovery(vertx);
      assertThrows(
          VertxServiceDiscovery.InvalidFilterException.class,
          () -> discovery.lookupAllIncludeOutOfService(null, result -> {}));
    } finally {
      vertx.close();
    }
  }
}
