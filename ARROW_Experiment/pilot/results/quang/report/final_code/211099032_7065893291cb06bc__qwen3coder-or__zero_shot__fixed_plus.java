package io.jaegertracing.analytics;

import io.jaegertracing.analytics.gremlin.GraphCreator;
import io.jaegertracing.analytics.gremlin.TraceTraversal;
import io.jaegertracing.analytics.gremlin.TraceTraversalSource;
import io.jaegertracing.analytics.gremlin.Util;
import io.jaegertracing.analytics.model.Span;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversal;
import org.apache.tinkerpop.gremlin.structure.*;
import org.apache.tinkerpop.gremlin.structure.util.empty.EmptyGraph;
import org.junit.Test;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class ServiceHeight_ArrowTest {

    @Test
    public void testCalculateEmptyGraph() {
        Graph graph = new MockGraph();
        TraceTraversalSource traversalSource = graph.traversal(TraceTraversalSource.class);
        TraceTraversal<Vertex, Vertex> leafs = traversalSource.leafSpans();
        
        Map<String, Integer> result = ServiceHeight.calculate(graph);
        assertNotNull(result);
        assertEquals(0, result.size());
    }

    @Test
    public void testCalculateSingleNode() {
        Graph graph = new MockGraph();
        TraceTraversalSource traversalSource = graph.traversal(TraceTraversalSource.class);
        TraceTraversal<Vertex, Vertex> leafs = traversalSource.leafSpans();
        Vertex vertex = new MockVertex("service1");
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
        TraceTraversalSource traversalSource = graph.traversal(TraceTraversalSource.class);
        TraceTraversal<Vertex, Vertex> leafs = traversalSource.leafSpans();
        Vertex leafVertex = new MockVertex("service2");
        Vertex parentVertex = new MockVertex("service1");
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
        TraceTraversalSource traversalSource = graph.traversal(TraceTraversalSource.class);
        TraceTraversal<Vertex, Vertex> leafs = traversalSource.leafSpans();
        Vertex leafVertex = new MockVertex("service3");
        Vertex parentVertex1 = new MockVertex("service2");
        Vertex parentVertex2 = new MockVertex("service1");
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
        TraceTraversalSource traversalSource = graph.traversal(TraceTraversalSource.class);
        TraceTraversal<Vertex, Vertex> leafs = traversalSource.leafSpans();
        Vertex leafVertex = new MockVertex("service1");
        Vertex parentVertex = new MockVertex("service1");
        Span leafSpan = new Span();
        leafSpan.serviceName = "service1";
        Span parentSpan = new Span();
        parentSpan.serviceName = "service1";
        
        Map<String, Integer> result = ServiceHeight.calculate(graph);
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(Integer.valueOf(0), result.get("service1"));
    }
    
    // Mock classes to avoid dependency on Mockito
    private static class MockGraph implements Graph {
        @Override
        public <C extends GraphComputer> C compute(Class<C> graphComputerClass) {
            return null;
        }

        @Override
        public GraphComputer compute() {
            return null;
        }

        @Override
        public <T extends TraversalSource> T traversal(Class<T> traversalSourceClass) {
            return (T) new MockTraceTraversalSource();
        }

        @Override
        public void close() {}

        @Override
        public Variables variables() {
            return null;
        }

        @Override
        public Configuration configuration() {
            return null;
        }

        @Override
        public Transaction tx() {
            return null;
        }

        @Override
        public Edge addEdge(Object o, Vertex vertex, Vertex vertex1, String s, Object... objects) {
            return null;
        }

        @Override
        public Vertex addVertex(Object... objects) {
            return null;
        }

        @Override
        public <E extends Element> E addElement(ElementType elementType, Object o, String s, Object... objects) {
            return null;
        }

        @Override
        public void removeEdge(Edge edge) {}

        @Override
        public void removeVertex(Vertex vertex) {}

        @Override
        public <E extends Element> E removeElement(ElementType elementType, Object o) {
            return null;
        }

        @Override
        public Iterator<Edge> edges(Object... objects) {
            return null;
        }

        @Override
        public Iterator<Vertex> vertices(Object... objects) {
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

        public void setHasNext(boolean hasNext) {
            this.hasNext = hasNext;
        }

        public void setNextVertex(Vertex vertex) {
            this.nextVertex = vertex;
        }

        @Override
        public GraphTraversal.Admin<Vertex, Vertex> asAdmin() {
            return null;
        }

        @Override
        public TraceTraversal<Vertex, Vertex> clone() {
            return null;
        }

        @Override
        public void setGraph(Graph graph) {}
    }

    private static class MockVertex implements Vertex {
        private String serviceName;
        
        public MockVertex() {
            this.serviceName = "";
        }
        
        public MockVertex(String serviceName) {
            this.serviceName = serviceName;
        }

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
            return new HashMap<>();
        }

        @Override
        public <V> V property(String key) {
            return null;
        }

        @Override
        public <V> V property(String key, V value) {
            return null;
        }

        @Override
        public void remove() {}

        @Override
        public Graph graph() {
            return null;
        }

        @Override
        public boolean equals(Object o) {
            return super.equals(o);
        }

        @Override
        public int hashCode() {
            return super.hashCode();
        }

        @Override
        public <V> Property<V> property(VertexProperty.Cardinality cardinality, String key, V value, Object... objects) {
            return null;
        }

        @Override
        public Iterator<VertexProperty<Object>> properties(String... propertyKeys) {
            return null;
        }

        @Override
        public Iterator<VertexProperty<Object>> properties() {
            return null;
        }

        @Override
        public Iterator<Edge> edges(Direction direction, String... strings) {
            return null;
        }

        @Override
        public Iterator<Edge> edges(Direction direction) {
            return null;
        }

        @Override
        public Iterator<Edge> edges(String... strings) {
            return null;
        }

        @Override
        public Iterator<Edge> edges() {
            return null;
        }

        @Override
        public Iterator<Vertex> vertices(Direction direction, String... strings) {
            return null;
        }

        @Override
        public Iterator<Vertex> vertices(Direction direction) {
            return null;
        }

        @Override
        public Iterator<Vertex> vertices(String... strings) {
            return null;
        }

        @Override
        public Iterator<Vertex> vertices() {
            return null;
        }
    }
}
