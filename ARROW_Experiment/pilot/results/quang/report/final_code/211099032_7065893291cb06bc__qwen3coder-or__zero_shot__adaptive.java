package io.jaegertracing.analytics;

import io.jaegertracing.analytics.gremlin.GraphCreator;
import io.jaegertracing.analytics.gremlin.TraceTraversal;
import io.jaegertracing.analytics.gremlin.TraceTraversalSource;
import io.jaegertracing.analytics.gremlin.Util;
import io.jaegertracing.analytics.model.Span;
import org.apache.tinkerpop.gremlin.structure.Graph;
import org.apache.tinkerpop.gremlin.structure.Vertex;
import org.junit.Test;
import org.mockito.Mockito;

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.*;

public class ServiceHeight_ArrowTest {

    @Test
    public void testCalculateEmptyGraph() {
        Graph graph = Mockito.mock(Graph.class);
        TraceTraversalSource traversalSource = Mockito.mock(TraceTraversalSource.class);
        TraceTraversal<Vertex, Vertex> leafs = Mockito.mock(TraceTraversal.class);
        
        when(graph.traversal(TraceTraversalSource.class)).thenReturn(traversalSource);
        when(traversalSource.leafSpans()).thenReturn(leafs);
        when(leafs.hasNext()).thenReturn(false);
        
        Map<String, Integer> result = ServiceHeight.calculate(graph);
        assertNotNull(result);
        assertEquals(0, result.size());
    }

    @Test
    public void testCalculateSingleNode() {
        Graph graph = Mockito.mock(Graph.class);
        TraceTraversalSource traversalSource = Mockito.mock(TraceTraversalSource.class);
        TraceTraversal<Vertex, Vertex> leafs = Mockito.mock(TraceTraversal.class);
        Vertex vertex = Mockito.mock(Vertex.class);
        Span span = new Span();
        span.serviceName = "service1";
        
        when(graph.traversal(TraceTraversalSource.class)).thenReturn(traversalSource);
        when(traversalSource.leafSpans()).thenReturn(leafs);
        when(leafs.hasNext()).thenReturn(true, false);
        when(leafs.next()).thenReturn(vertex);
        when(Util.parent(vertex)).thenReturn(null);
        when(GraphCreator.toSpan(vertex)).thenReturn(span);
        
        Map<String, Integer> result = ServiceHeight.calculate(graph);
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(Integer.valueOf(0), result.get("service1"));
    }

    @Test
    public void testCalculateTwoServicesInChain() {
        Graph graph = Mockito.mock(Graph.class);
        TraceTraversalSource traversalSource = Mockito.mock(TraceTraversalSource.class);
        TraceTraversal<Vertex, Vertex> leafs = Mockito.mock(TraceTraversal.class);
        Vertex leafVertex = Mockito.mock(Vertex.class);
        Vertex parentVertex = Mockito.mock(Vertex.class);
        Span leafSpan = new Span();
        leafSpan.serviceName = "service2";
        Span parentSpan = new Span();
        parentSpan.serviceName = "service1";
        
        when(graph.traversal(TraceTraversalSource.class)).thenReturn(traversalSource);
        when(traversalSource.leafSpans()).thenReturn(leafs);
        when(leafs.hasNext()).thenReturn(true, false);
        when(leafs.next()).thenReturn(leafVertex);
        when(Util.parent(leafVertex)).thenReturn(parentVertex);
        when(Util.parent(parentVertex)).thenReturn(null);
        when(GraphCreator.toSpan(leafVertex)).thenReturn(leafSpan);
        when(GraphCreator.toSpan(parentVertex)).thenReturn(parentSpan);
        
        Map<String, Integer> result = ServiceHeight.calculate(graph);
        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals(Integer.valueOf(0), result.get("service2"));
        assertEquals(Integer.valueOf(1), result.get("service1"));
    }

    @Test
    public void testCalculateMultipleServicesInChain() {
        Graph graph = Mockito.mock(Graph.class);
        TraceTraversalSource traversalSource = Mockito.mock(TraceTraversalSource.class);
        TraceTraversal<Vertex, Vertex> leafs = Mockito.mock(TraceTraversal.class);
        Vertex leafVertex = Mockito.mock(Vertex.class);
        Vertex parentVertex1 = Mockito.mock(Vertex.class);
        Vertex parentVertex2 = Mockito.mock(Vertex.class);
        Span leafSpan = new Span();
        leafSpan.serviceName = "service3";
        Span parentSpan1 = new Span();
        parentSpan1.serviceName = "service2";
        Span parentSpan2 = new Span();
        parentSpan2.serviceName = "service1";
        
        when(graph.traversal(TraceTraversalSource.class)).thenReturn(traversalSource);
        when(traversalSource.leafSpans()).thenReturn(leafs);
        when(leafs.hasNext()).thenReturn(true, false);
        when(leafs.next()).thenReturn(leafVertex);
        when(Util.parent(leafVertex)).thenReturn(parentVertex1);
        when(Util.parent(parentVertex1)).thenReturn(parentVertex2);
        when(Util.parent(parentVertex2)).thenReturn(null);
        when(GraphCreator.toSpan(leafVertex)).thenReturn(leafSpan);
        when(GraphCreator.toSpan(parentVertex1)).thenReturn(parentSpan1);
        when(GraphCreator.toSpan(parentVertex2)).thenReturn(parentSpan2);
        
        Map<String, Integer> result = ServiceHeight.calculate(graph);
        assertNotNull(result);
        assertEquals(3, result.size());
        assertEquals(Integer.valueOf(0), result.get("service3"));
        assertEquals(Integer.valueOf(1), result.get("service2"));
        assertEquals(Integer.valueOf(2), result.get("service1"));
    }

    @Test
    public void testCalculateSameServiceMultipleTimes() {
        Graph graph = Mockito.mock(Graph.class);
        TraceTraversalSource traversalSource = Mockito.mock(TraceTraversalSource.class);
        TraceTraversal<Vertex, Vertex> leafs = Mockito.mock(TraceTraversal.class);
        Vertex leafVertex = Mockito.mock(Vertex.class);
        Vertex parentVertex = Mockito.mock(Vertex.class);
        Span leafSpan = new Span();
        leafSpan.serviceName = "service1";
        Span parentSpan = new Span();
        parentSpan.serviceName = "service1";
        
        when(graph.traversal(TraceTraversalSource.class)).thenReturn(traversalSource);
        when(traversalSource.leafSpans()).thenReturn(leafs);
        when(leafs.hasNext()).thenReturn(true, false);
        when(leafs.next()).thenReturn(leafVertex);
        when(Util.parent(leafVertex)).thenReturn(parentVertex);
        when(Util.parent(parentVertex)).thenReturn(null);
        when(GraphCreator.toSpan(leafVertex)).thenReturn(leafSpan);
        when(GraphCreator.toSpan(parentVertex)).thenReturn(parentSpan);
        
        Map<String, Integer> result = ServiceHeight.calculate(graph);
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(Integer.valueOf(0), result.get("service1"));
    }
}
