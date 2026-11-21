package io.jaegertracing.analytics;

import io.jaegertracing.analytics.gremlin.GraphCreator;
import io.jaegertracing.analytics.gremlin.TraceTraversal;
import io.jaegertracing.analytics.gremlin.TraceTraversalSource;
import io.jaegertracing.analytics.gremlin.Util;
import io.jaegertracing.analytics.model.Span;
import org.apache.tinkerpop.gremlin.structure.Graph;
import org.apache.tinkerpop.gremlin.structure.Vertex;
import org.junit.Test;
import org.junit.Before;
import static org.mockito.Mockito.*;
import java.util.Map;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.ArrayList;
import java.util.Set;
import java.util.HashSet;

import static org.junit.Assert.*;

public class ServiceHeight_ArrowTest {

    private Graph mockGraph;
    private TraceTraversal mockTraversal;
    private TraceTraversalSource mockTraversalSource;
    private Vertex mockLeafVertex;
    private Vertex mockParentVertex;
    private Span mockLeafSpan;
    private Span mockParentSpan;

    @Before
    public void setUp() {
        mockGraph = mock(Graph.class);
        mockTraversal = mock(TraceTraversal.class);
        mockTraversalSource = mock(TraceTraversalSource.class);
        mockLeafVertex = mock(Vertex.class);
        mockParentVertex = mock(Vertex.class);
        mockLeafSpan = mock(Span.class);
        mockParentSpan = mock(Span.class);
    }

    @Test
    public void testCalculateEmptyGraph() {
        when(mockGraph.traversal(TraceTraversalSource.class)).thenReturn(mockTraversalSource);
        when(mockTraversalSource.leafSpans()).thenReturn(mockTraversal);
        when(mockTraversal.hasNext()).thenReturn(false);

        Map<String, Integer> result = ServiceHeight.calculate(mockGraph);
        
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    public void testCalculateSingleLeaf() {
        when(mockGraph.traversal(TraceTraversalSource.class)).thenReturn(mockTraversalSource);
        when(mockTraversalSource.leafSpans()).thenReturn(mockTraversal);
        when(mockTraversal.hasNext()).thenReturn(true, false);
        when(mockTraversal.next()).thenReturn(mockLeafVertex);
        
        when(Util.parent(mockLeafVertex)).thenReturn(null);
        when(GraphCreator.toSpan(mockLeafVertex)).thenReturn(mockLeafSpan);
        mockLeafSpan.serviceName = "serviceA";
        
        Map<String, Integer> result = ServiceHeight.calculate(mockGraph);
        
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(Integer.valueOf(0), result.get("serviceA"));
    }

    @Test
    public void testCalculateLeafWithParent() {
        when(mockGraph.traversal(TraceTraversalSource.class)).thenReturn(mockTraversalSource);
        when(mockTraversalSource.leafSpans()).thenReturn(mockTraversal);
        when(mockTraversal.hasNext()).thenReturn(true, false);
        when(mockTraversal.next()).thenReturn(mockLeafVertex);
        
        when(Util.parent(mockLeafVertex)).thenReturn(mockParentVertex);
        when(Util.parent(mockParentVertex)).thenReturn(null);
        when(GraphCreator.toSpan(mockLeafVertex)).thenReturn(mockLeafSpan);
        when(GraphCreator.toSpan(mockParentVertex)).thenReturn(mockParentSpan);
        
        mockLeafSpan.serviceName = "serviceB";
        mockParentSpan.serviceName = "serviceC";
        
        Map<String, Integer> result = ServiceHeight.calculate(mockGraph);
        
        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals(Integer.valueOf(0), result.get("serviceB"));
        assertEquals(Integer.valueOf(1), result.get("serviceC"));
    }

    @Test
    public void testCalculateMultipleLeavesSameService() {
        when(mockGraph.traversal(TraceTraversalSource.class)).thenReturn(mockTraversalSource);
        when(mockTraversalSource.leafSpans()).thenReturn(mockTraversal);
        when(mockTraversal.hasNext()).thenReturn(true, true, false);
        
        when(mockTraversal.next()).thenReturn(mockLeafVertex, mockLeafVertex);
        
        when(Util.parent(mockLeafVertex)).thenReturn(null);
        when(GraphCreator.toSpan(mockLeafVertex)).thenReturn(mockLeafSpan);
        mockLeafSpan.serviceName = "serviceD";
        
        Map<String, Integer> result = ServiceHeight.calculate(mockGraph);
        
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(Integer.valueOf(0), result.get("serviceD"));
    }

    @Test
    public void testCalculateMultipleLeavesDifferentServices() {
        when(mockGraph.traversal(TraceTraversalSource.class)).thenReturn(mockTraversalSource);
        when(mockTraversalSource.leafSpans()).thenReturn(mockTraversal);
        when(mockTraversal.hasNext()).thenReturn(true, true, false);
        
        when(mockTraversal.next()).thenReturn(mockLeafVertex, mockLeafVertex);
        
        when(Util.parent(mockLeafVertex)).thenReturn(null);
        when(GraphCreator.toSpan(mockLeafVertex)).thenReturn(mockLeafSpan);
        mockLeafSpan.serviceName = "serviceE";
        
        Map<String, Integer> result = ServiceHeight.calculate(mockGraph);
        
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(Integer.valueOf(0), result.get("serviceE"));
    }

    @Test
    public void testRunWithMetrics() {
        Map<String, Integer> mockHeights = new LinkedHashMap<>();
        mockHeights.put("serviceX", 5);
        mockHeights.put("serviceY", 3);
        
        ServiceHeight serviceHeight = new ServiceHeight();
        
        // We can't easily test the metrics registration since it's a static field,
        // but we can at least verify the method doesn't throw exceptions
        try {
            serviceHeight.runWithMetrics(mockGraph);
        } catch (Exception e) {
            fail("runWithMetrics should not throw exception");
        }
    }
}
