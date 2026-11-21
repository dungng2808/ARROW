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
import org.mockito.Mockito;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.*;

public class ServiceHeight_ArrowTest {

    private Graph graph;
    private TraceTraversal<Vertex, Vertex> mockLeafsTraversal;
    private Vertex mockLeafVertex;
    private Vertex mockParentVertex;
    private Span mockLeafSpan;
    private Span mockParentSpan;

    @Before
    public void setUp() {
        graph = Mockito.mock(Graph.class);
        mockLeafsTraversal = Mockito.mock(TraceTraversal.class);
        mockLeafVertex = Mockito.mock(Vertex.class);
        mockParentVertex = Mockito.mock(Vertex.class);
        mockLeafSpan = Mockito.mock(Span.class);
        mockParentSpan = Mockito.mock(Span.class);
    }

    @Test
    public void testCalculateEmptyGraph() {
        when(graph.traversal(TraceTraversalSource.class)).thenReturn(mockLeafsTraversal);
        when(mockLeafsTraversal.hasNext()).thenReturn(false);

        Map<String, Integer> result = ServiceHeight.calculate(graph);
        
        assertEquals(0, result.size());
    }

    @Test
    public void testCalculateSingleNode() {
        when(graph.traversal(TraceTraversalSource.class)).thenReturn(mockLeafsTraversal);
        when(mockLeafsTraversal.hasNext()).thenReturn(true, false);
        when(mockLeafsTraversal.next()).thenReturn(mockLeafVertex);
        
        when(Util.parent(mockLeafVertex)).thenReturn(null);
        when(GraphCreator.toSpan(mockLeafVertex)).thenReturn(mockLeafSpan);
        mockLeafSpan.serviceName = "service1";

        Map<String, Integer> result = ServiceHeight.calculate(graph);
        
        assertEquals(1, result.size());
        assertEquals(Integer.valueOf(0), result.get("service1"));
    }

    @Test
    public void testCalculateTwoNodesSameService() {
        when(graph.traversal(TraceTraversalSource.class)).thenReturn(mockLeafsTraversal);
        when(mockLeafsTraversal.hasNext()).thenReturn(true, false);
        when(mockLeafsTraversal.next()).thenReturn(mockLeafVertex);
        
        when(Util.parent(mockLeafVertex)).thenReturn(mockParentVertex);
        when(GraphCreator.toSpan(mockLeafVertex)).thenReturn(mockLeafSpan);
        when(GraphCreator.toSpan(mockParentVertex)).thenReturn(mockParentSpan);
        
        mockLeafSpan.serviceName = "service1";
        mockParentSpan.serviceName = "service1";

        Map<String, Integer> result = ServiceHeight.calculate(graph);
        
        assertEquals(1, result.size());
        assertEquals(Integer.valueOf(0), result.get("service1"));
    }

    @Test
    public void testCalculateTwoNodesDifferentServices() {
        when(graph.traversal(TraceTraversalSource.class)).thenReturn(mockLeafsTraversal);
        when(mockLeafsTraversal.hasNext()).thenReturn(true, false);
        when(mockLeafsTraversal.next()).thenReturn(mockLeafVertex);
        
        when(Util.parent(mockLeafVertex)).thenReturn(mockParentVertex);
        when(GraphCreator.toSpan(mockLeafVertex)).thenReturn(mockLeafSpan);
        when(GraphCreator.toSpan(mockParentVertex)).thenReturn(mockParentSpan);
        
        mockLeafSpan.serviceName = "service1";
        mockParentSpan.serviceName = "service2";

        Map<String, Integer> result = ServiceHeight.calculate(graph);
        
        assertEquals(2, result.size());
        assertEquals(Integer.valueOf(0), result.get("service1"));
        assertEquals(Integer.valueOf(1), result.get("service2"));
    }

    @Test
    public void testCalculateThreeNodesChain() {
        when(graph.traversal(TraceTraversalSource.class)).thenReturn(mockLeafsTraversal);
        when(mockLeafsTraversal.hasNext()).thenReturn(true, false);
        when(mockLeafsTraversal.next()).thenReturn(mockLeafVertex);
        
        when(Util.parent(mockLeafVertex)).thenReturn(mockParentVertex);
        when(Util.parent(mockParentVertex)).thenReturn(null);
        when(GraphCreator.toSpan(mockLeafVertex)).thenReturn(mockLeafSpan);
        when(GraphCreator.toSpan(mockParentVertex)).thenReturn(mockParentSpan);
        
        mockLeafSpan.serviceName = "service1";
        mockParentSpan.serviceName = "service2";

        Map<String, Integer> result = ServiceHeight.calculate(graph);
        
        assertEquals(2, result.size());
        assertEquals(Integer.valueOf(0), result.get("service1"));
        assertEquals(Integer.valueOf(1), result.get("service2"));
    }

    @Test
    public void testCalculateMultiplePaths() {
        when(graph.traversal(TraceTraversalSource.class)).thenReturn(mockLeafsTraversal);
        when(mockLeafsTraversal.hasNext()).thenReturn(true, true, false);
        when(mockLeafsTraversal.next()).thenReturn(mockLeafVertex, mockLeafVertex);
        
        when(Util.parent(mockLeafVertex)).thenReturn(mockParentVertex);
        when(Util.parent(mockParentVertex)).thenReturn(null);
        when(GraphCreator.toSpan(mockLeafVertex)).thenReturn(mockLeafSpan);
        when(GraphCreator.toSpan(mockParentVertex)).thenReturn(mockParentSpan);
        
        mockLeafSpan.serviceName = "service1";
        mockParentSpan.serviceName = "service2";

        Map<String, Integer> result = ServiceHeight.calculate(graph);
        
        assertEquals(2, result.size());
        assertEquals(Integer.valueOf(0), result.get("service1"));
        assertEquals(Integer.valueOf(1), result.get("service2"));
    }
}
