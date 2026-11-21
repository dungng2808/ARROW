package io.jaegertracing.analytics;

import io.jaegertracing.analytics.gremlin.GraphCreator;
import io.jaegertracing.analytics.gremlin.TraceTraversal;
import io.jaegertracing.analytics.gremlin.TraceTraversalSource;
import io.jaegertracing.analytics.gremlin.Util;
import io.jaegertracing.analytics.model.Span;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversal;
import org.apache.tinkerpop.gremlin.structure.Graph;
import org.apache.tinkerpop.gremlin.structure.Vertex;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class ServiceHeight_ArrowTest {

    @Test
    public void testCalculateEmptyGraph() {
        Graph graph = new MockGraph();
        TraceTraversalSource traversalSource = new MockTraceTraversalSource();
        TraceTraversal<Vertex, Vertex> leafs = new MockTraceTraversal();
        
        Map<String, Integer> result = ServiceHeight.calculate(graph);
        assertNotNull(result);
        assertEquals(0, result.size());
    }

    @Test
    public void testCalculateSingleNode() {
        Graph graph = new MockGraph();
        TraceTraversalSource traversalSource = new MockTraceTraversalSource();
        TraceTraversal<Vertex, Vertex> leafs = new MockTraceTraversal();
        Vertex vertex = new MockVertex();
        Span span = new Span();
        span.serviceName = "service1";
        
        Map<String, Integer> result = ServiceHeight.calculate(graph);
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(Integer.valueOf(0), result.get("service1"));
    }

    @Test
    public void testCalculateTwoServicesInChain() {
        Graph graph = new MockGraph();
        TraceTraversalSource traversalSource = new MockTraceTraversalSource();
        TraceTraversal<Vertex, Vertex> leafs = new MockTraceTraversal();
        Vertex leafVertex = new MockVertex();
        Vertex parentVertex = new MockVertex();
        Span leafSpan = new Span();
        leafSpan.serviceName = "service2";
        Span parentSpan = new Span();
        parentSpan.serviceName = "service1";
        
        Map<String, Integer> result = ServiceHeight.calculate(graph);
        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals(Integer.valueOf(0), result.get("service2"));
        assertEquals(Integer.valueOf(1), result.get("service1"));
    }

    @Test
    public void testCalculateMultipleServicesInChain() {
        Graph graph = new MockGraph();
        TraceTraversalSource traversalSource = new MockTraceTraversalSource();
        TraceTraversal<Vertex, Vertex> leafs = new MockTraceTraversal();
        Vertex leafVertex = new MockVertex();
        Vertex parentVertex1 = new MockVertex();
        Vertex parentVertex2 = new MockVertex();
        Span leafSpan = new Span();
        leafSpan.serviceName = "service3";
        Span parentSpan1 = new Span();
        parentSpan1.serviceName = "service2";
        Span parentSpan2 = new Span();
        parentSpan2.serviceName = "service1";
        
        Map<String, Integer> result = ServiceHeight.calculate(graph);
        assertNotNull(result);
        assertEquals(3, result.size());
        assertEquals(Integer.valueOf(0), result.get("service3"));
        assertEquals(Integer.valueOf(1), result.get("service2"));
        assertEquals(Integer.valueOf(2), result.get("service1"));
    }

    @Test
    public void testCalculateSameServiceMultipleTimes() {
        Graph graph = new MockGraph();
        TraceTraversalSource traversalSource = new MockTraceTraversalSource();
        TraceTraversal<Vertex, Vertex> leafs = new MockTraceTraversal();
        Vertex leafVertex = new MockVertex();
        Vertex parentVertex = new MockVertex();
        Span leafSpan = new Span();
        leafSpan.serviceName = "service1";
        Span parentSpan = new Span();
        parentSpan.serviceName = "service1";
        
        Map<String, Integer> result = ServiceHeight.calculate(graph);
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(Integer.valueOf(0), result.get("service1"));
    }
    
    // Mock classes to avoid Mockito dependency
    private static class MockGraph implements Graph {
        @Override
        public TraceTraversalSource traversal(Class<TraceTraversalSource> clazz) {
            return new MockTraceTraversalSource();
        }
        
        @Override
        public void close() {}
        
        @Override
        public boolean isClosed() { return false; }
        
        @Override
        public String toString() { return "MockGraph"; }
        
        @Override
        public GraphTraversal.Admin<Vertex, Vertex> addV() {
            return null;
        }
        
        @Override
        public GraphTraversal.Admin<Vertex, Vertex> addV(Object... v) {
            return null;
        }
        
        @Override
        public GraphTraversal.Admin<Vertex, Vertex> v(Object... v) {
            return null;
        }
        
        @Override
        public GraphTraversal.Admin<Vertex, Vertex> e(Object... e) {
            return null;
        }
        
        @Override
        public Variables variables() {
            return null;
        }
    }
    
    private static class MockTraceTraversalSource implements TraceTraversalSource {
        @Override
        public TraceTraversal<Vertex, Vertex> leafSpans() {
            return new MockTraceTraversal();
        }
    }
    
    private static class MockTraceTraversal implements TraceTraversal<Vertex, Vertex> {
        private boolean hasNext = false;
        private Vertex nextVertex = null;
        
        @Override
        public boolean hasNext() {
            return hasNext;
        }
        
        @Override
        public Vertex next() {
            hasNext = false;
            return nextVertex;
        }
        
        // Set up test data
        public void setHasNext(boolean hasNext) {
            this.hasNext = hasNext;
        }
        
        public void setNextVertex(Vertex vertex) {
            this.nextVertex = vertex;
            this.hasNext = true;
        }
        
        @Override
        public GraphTraversal.Admin<Vertex, Vertex> clone() {
            return null;
        }
    }
    
    private static class MockVertex implements Vertex {
        @Override
        public Object id() { return null; }
        
        @Override
        public String label() { return null; }
        
        @Override
        public Map<String, Object> properties() { return new HashMap<>(); }
        
        @Override
        public <V> V property(String key) { return null; }
        
        @Override
        public <V> V property(String key, V value) { return null; }
        
        @Override
        public void remove() {}
        
        @Override
        public Graph graph() { return null; }
        
        @Override
        public Object[] keys() { return new Object[0]; }
        
        @Override
        public Object[] values() { return new Object[0]; }
        
        @Override
        public String toString() { return "MockVertex"; }
        
        @Override
        public <V> org.apache.tinkerpop.gremlin.structure.Property<V> property(String key, V value) {
            return null;
        }
        
        @Override
        public <V> org.apache.tinkerpop.gremlin.structure.Property<V> property(String key) {
            return null;
        }
        
        @Override
        public Iterable<org.apache.tinkerpop.gremlin.structure.Property<Object>> properties(String... keys) {
            return null;
        }
        
        @Override
        public Iterator<Vertex> vertices(org.apache.tinkerpop.gremlin.structure.Direction direction, String... edgeLabels) {
            return null;
        }
        
        @Override
        public Iterator<Vertex> vertices(org.apache.tinkerpop.gremlin.structure.Direction direction, org.apache.tinkerpop.gremlin.structure.ElementFilter... filters) {
            return null;
        }
        
        @Override
        public Iterator<Edge> edges(org.apache.tinkerpop.gremlin.structure.Direction direction, String... edgeLabels) {
            return null;
        }
        
        @Override
        public Iterator<Edge> edges(org.apache.tinkerpop.gremlin.structure.Direction direction, org.apache.tinkerpop.gremlin.structure.ElementFilter... filters) {
            return null;
        }
    }
}
