package io.jaegertracing.analytics;

import io.jaegertracing.analytics.gremlin.GraphCreator;
import io.jaegertracing.analytics.gremlin.TraceTraversal;
import io.jaegertracing.analytics.gremlin.TraceTraversalSource;
import io.jaegertracing.analytics.gremlin.Util;
import io.jaegertracing.analytics.model.Span;
import org.apache.tinkerpop.gremlin.structure.Graph;
import org.apache.tinkerpop.gremlin.structure.Vertex;
import org.apache.tinkerpop.gremlin.structure.util.empty.EmptyGraph;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class ServiceHeight_ArrowTest {

    @Test
    public void testCalculateEmptyGraph() {
        Graph graph = EmptyGraph.instance();
        TraceTraversalSource traversalSource = graph.traversal(TraceTraversalSource.class);
        TraceTraversal<Vertex, Vertex> leafs = traversalSource.leafSpans();
        
        Map<String, Integer> result = ServiceHeight.calculate(graph);
        assertNotNull(result);
        assertEquals(0, result.size());
    }

    @Test
    public void testCalculateSingleNode() {
        Graph graph = EmptyGraph.instance();
        TraceTraversalSource traversalSource = graph.traversal(TraceTraversalSource.class);
        TraceTraversal<Vertex, Vertex> leafs = traversalSource.leafSpans();
        Vertex vertex = new MockVertex();
        Span span = new Span();
        span.serviceName = "service1";
        vertex.property("serviceName", "service1");
        
        Map<String, Integer> result = ServiceHeight.calculate(graph);
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(Integer.valueOf(0), result.get("service1"));
    }

    @Test
    public void testCalculateTwoServicesInChain() {
        Graph graph = EmptyGraph.instance();
        TraceTraversalSource traversalSource = graph.traversal(TraceTraversalSource.class);
        TraceTraversal<Vertex, Vertex> leafs = traversalSource.leafSpans();
        Vertex leafVertex = new MockVertex();
        Vertex parentVertex = new MockVertex();
        Span leafSpan = new Span();
        leafSpan.serviceName = "service2";
        leafVertex.property("serviceName", "service2");
        Span parentSpan = new Span();
        parentSpan.serviceName = "service1";
        parentVertex.property("serviceName", "service1");
        
        Map<String, Integer> result = ServiceHeight.calculate(graph);
        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals(Integer.valueOf(0), result.get("service2"));
        assertEquals(Integer.valueOf(1), result.get("service1"));
    }

    @Test
    public void testCalculateMultipleServicesInChain() {
        Graph graph = EmptyGraph.instance();
        TraceTraversalSource traversalSource = graph.traversal(TraceTraversalSource.class);
        TraceTraversal<Vertex, Vertex> leafs = traversalSource.leafSpans();
        Vertex leafVertex = new MockVertex();
        Vertex parentVertex1 = new MockVertex();
        Vertex parentVertex2 = new MockVertex();
        Span leafSpan = new Span();
        leafSpan.serviceName = "service3";
        leafVertex.property("serviceName", "service3");
        Span parentSpan1 = new Span();
        parentSpan1.serviceName = "service2";
        parentVertex1.property("serviceName", "service2");
        Span parentSpan2 = new Span();
        parentSpan2.serviceName = "service1";
        parentVertex2.property("serviceName", "service1");
        
        Map<String, Integer> result = ServiceHeight.calculate(graph);
        assertNotNull(result);
        assertEquals(3, result.size());
        assertEquals(Integer.valueOf(0), result.get("service3"));
        assertEquals(Integer.valueOf(1), result.get("service2"));
        assertEquals(Integer.valueOf(2), result.get("service1"));
    }

    @Test
    public void testCalculateSameServiceMultipleTimes() {
        Graph graph = EmptyGraph.instance();
        TraceTraversalSource traversalSource = graph.traversal(TraceTraversalSource.class);
        TraceTraversal<Vertex, Vertex> leafs = traversalSource.leafSpans();
        Vertex leafVertex = new MockVertex();
        Vertex parentVertex = new MockVertex();
        Span leafSpan = new Span();
        leafSpan.serviceName = "service1";
        leafVertex.property("serviceName", "service1");
        Span parentSpan = new Span();
        parentSpan.serviceName = "service1";
        parentVertex.property("serviceName", "service1");
        
        Map<String, Integer> result = ServiceHeight.calculate(graph);
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(Integer.valueOf(0), result.get("service1"));
    }
    
    // Mock classes to avoid dependency on Mockito
    private static class MockVertex implements Vertex {
        private Map<String, Object> properties = new HashMap<>();
        private String serviceName;

        @Override
        public Object id() {
            return null;
        }

        @Override
        public String label() {
            return null;
        }

        @Override
        public Map<String, Object> properties(String key) {
            return properties;
        }

        @Override
        public Map<String, Object> properties() {
            return properties;
        }

        @Override
        public <V> V property(String key) {
            return (V) properties.get(key);
        }

        @Override
        public <V> V property(String key, V value) {
            properties.put(key, value);
            return value;
        }

        @Override
        public void remove() {}

        @Override
        public Graph graph() {
            return null;
        }

        @Override
        public Object[] keys() {
            return properties.keySet().toArray();
        }

        @Override
        public Object[] values() {
            return properties.values().toArray();
        }

        @Override
        public <V> V property(String... key) {
            return null;
        }

        public void setServiceName(String serviceName) {
            this.serviceName = serviceName;
        }

        public String getServiceName() {
            return serviceName;
        }
    }
}
