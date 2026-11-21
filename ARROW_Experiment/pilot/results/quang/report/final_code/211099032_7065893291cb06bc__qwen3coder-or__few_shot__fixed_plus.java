package io.jaegertracing.analytics;

import io.jaegertracing.analytics.gremlin.GraphCreator;
import io.jaegertracing.analytics.gremlin.TraceTraversal;
import io.jaegertracing.analytics.gremlin.TraceTraversalSource;
import io.jaegertracing.analytics.gremlin.Util;
import io.jaegertracing.analytics.model.Span;
import org.apache.tinkerpop.gremlin.structure.Graph;
import org.apache.tinkerpop.gremlin.structure.Vertex;
import org.junit.Before;
import org.junit.Test;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;

public class ServiceHeight_ArrowTest {

    private Graph graph;
    private TraceTraversal<Vertex, Vertex> mockLeafsTraversal;
    private Vertex mockLeafVertex;
    private Vertex mockParentVertex;
    private Span mockLeafSpan;
    private Span mockParentSpan;

    @Before
    public void setUp() {
        // Create a proper mock Graph implementation
        graph = new org.apache.tinkerpop.gremlin.tinkergraph.TinkerGraph();
        
        // Create mock vertices and spans for testing
        mockLeafVertex = graph.addVertex("leaf");
        mockParentVertex = graph.addVertex("parent");
        
        mockLeafSpan = new Span();
        mockLeafSpan.serviceName = "service1";
        mockParentSpan = new Span();
        mockParentSpan.serviceName = "service2";
    }

    @Test
    public void testCalculateEmptyGraph() {
        // Test with empty graph
        Map<String, Integer> result = ServiceHeight.calculate(graph);
        assertEquals(0, result.size());
    }

    @Test
    public void testCalculateSingleNode() {
        // Add a single vertex to the graph
        Vertex vertex = graph.addVertex("single");
        Span span = new Span();
        span.serviceName = "testService";
        // Set up the vertex with the span data
        vertex.property("span", span);
        
        Map<String, Integer> result = ServiceHeight.calculate(graph);
        assertEquals(1, result.size());
        assertEquals(Integer.valueOf(0), result.get("testService"));
    }

    @Test
    public void testCalculateTwoNodesSameService() {
        // Create two nodes in the same service
        Vertex leaf = graph.addVertex("leaf");
        Vertex parent = graph.addVertex("parent");
        
        Span leafSpan = new Span();
        leafSpan.serviceName = "sameService";
        Span parentSpan = new Span();
        parentSpan.serviceName = "sameService";
        
        leaf.property("span", leafSpan);
        parent.property("span", parentSpan);
        
        // Set up parent relationship
        leaf.addEdge("parent", parent);
        
        Map<String, Integer> result = ServiceHeight.calculate(graph);
        assertEquals(1, result.size());
        assertEquals(Integer.valueOf(0), result.get("sameService"));
    }

    @Test
    public void testCalculateTwoNodesDifferentServices() {
        // Create two nodes in different services
        Vertex leaf = graph.addVertex("leaf");
        Vertex parent = graph.addVertex("parent");
        
        Span leafSpan = new Span();
        leafSpan.serviceName = "service1";
        Span parentSpan = new Span();
        parentSpan.serviceName = "service2";
        
        leaf.property("span", leafSpan);
        parent.property("span", parentSpan);
        
        // Set up parent relationship
        leaf.addEdge("parent", parent);
        
        Map<String, Integer> result = ServiceHeight.calculate(graph);
        assertEquals(2, result.size());
        assertEquals(Integer.valueOf(1), result.get("service2"));
        assertEquals(Integer.valueOf(0), result.get("service1"));
    }

    @Test
    public void testCalculateThreeNodesChain() {
        // Create three nodes in a chain with different services
        Vertex leaf = graph.addVertex("leaf");
        Vertex middle = graph.addVertex("middle");
        Vertex root = graph.addVertex("root");
        
        Span leafSpan = new Span();
        leafSpan.serviceName = "service1";
        Span middleSpan = new Span();
        middleSpan.serviceName = "service2";
        Span rootSpan = new Span();
        rootSpan.serviceName = "service3";
        
        leaf.property("span", leafSpan);
        middle.property("span", middleSpan);
        root.property("span", rootSpan);
        
        // Set up parent relationships
        leaf.addEdge("parent", middle);
        middle.addEdge("parent", root);
        
        Map<String, Integer> result = ServiceHeight.calculate(graph);
        assertEquals(3, result.size());
        assertEquals(Integer.valueOf(2), result.get("service3"));
        assertEquals(Integer.valueOf(1), result.get("service2"));
        assertEquals(Integer.valueOf(0), result.get("service1"));
    }

    @Test
    public void testCalculateMultiplePaths() {
        // Create multiple paths
        Vertex leaf1 = graph.addVertex("leaf1");
        Vertex leaf2 = graph.addVertex("leaf2");
        Vertex common = graph.addVertex("common");
        Vertex root = graph.addVertex("root");
        
        Span leaf1Span = new Span();
        leaf1Span.serviceName = "service1";
        Span leaf2Span = new Span();
        leaf2Span.serviceName = "service2";
        Span commonSpan = new Span();
        commonSpan.serviceName = "service3";
        Span rootSpan = new Span();
        rootSpan.serviceName = "service4";
        
        leaf1.property("span", leaf1Span);
        leaf2.property("span", leaf2Span);
        common.property("span", commonSpan);
        root.property("span", rootSpan);
        
        // Set up parent relationships
        leaf1.addEdge("parent", common);
        leaf2.addEdge("parent", common);
        common.addEdge("parent", root);
        
        Map<String, Integer> result = ServiceHeight.calculate(graph);
        assertEquals(4, result.size());
        assertEquals(Integer.valueOf(2), result.get("service4"));
        assertEquals(Integer.valueOf(1), result.get("service3"));
        assertEquals(Integer.valueOf(0), result.get("service2"));
        assertEquals(Integer.valueOf(0), result.get("service1"));
    }
}
