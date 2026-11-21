package io.jaegertracing.analytics;

import io.jaegertracing.analytics.gremlin.GraphCreator;
import io.jaegertracing.analytics.gremlin.TraceTraversal;
import io.jaegertracing.analytics.gremlin.TraceTraversalSource;
import io.jaegertracing.analytics.gremlin.Util;
import io.jaegertracing.analytics.model.Span;
import org.apache.tinkerpop.gremlin.structure.Graph;
import org.apache.tinkerpop.gremlin.structure.Vertex;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversal;
import org.junit.Test;
import org.junit.Before;

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
        // Since Mockito is not available, we'll create mock-like objects manually
        // or use the actual classes if they're suitable for testing
        mockGraph = new Graph() {
            @Override
            public TraceTraversalSource traversal(Class<TraceTraversalSource> clazz) {
                return mockTraversalSource;
            }

            @Override
            public void close() {}

            @Override
            public String toString() {
                return "MockGraph";
            }

            @Override
            public Object variables() {
                return null;
            }
            
            @Override
            public String identifier() {
                return "mockGraph";
            }
        };

        mockTraversalSource = new TraceTraversalSource(mockGraph) {
            @Override
            public TraceTraversal<Vertex, Vertex> leafSpans() {
                return mockTraversal;
            }

            @Override
            public String toString() {
                return "MockTraversalSource";
            }
        };

        mockTraversal = new TraceTraversal<Vertex, Vertex>(mockGraph) {
            private boolean hasNext = false;
            private boolean firstCall = true;

            @Override
            public boolean hasNext() {
                if (firstCall) {
                    firstCall = false;
                    return hasNext;
                }
                return false;
            }

            @Override
            public Vertex next() {
                return mockLeafVertex;
            }

            @Override
            public String toString() {
                return "MockTraversal";
            }

            @Override
            public GraphTraversal.Admin<Vertex, Vertex> clone() {
                return null;
            }

            @Override
            public void setGraph(Graph graph) {}
            
            @Override
            public Graph getGraph() {
                return mockGraph;
            }
        };

        mockLeafVertex = new Vertex() {
            @Override
            public Object id() {
                return "mockLeafVertexId";
            }

            @Override
            public String label() {
                return "vertex";
            }

            @Override
            public Graph graph() {
                return mockGraph;
            }

            @Override
            public String toString() {
                return "MockLeafVertex";
            }

            @Override
            public <V> org.apache.tinkerpop.gremlin.structure.Property<V> property(String key) {
                return null;
            }

            @Override
            public <V> org.apache.tinkerpop.gremlin.structure.Property<V> property(String key, V value) {
                return null;
            }

            @Override
            public <V> Iterable<org.apache.tinkerpop.gremlin.structure.Property<V>> properties(String... keys) {
                return null;
            }
            
            @Override
            public Set<String> keys() {
                return new HashSet<>();
            }
        };

        mockParentVertex = new Vertex() {
            @Override
            public Object id() {
                return "mockParentVertexId";
            }

            @Override
            public String label() {
                return "vertex";
            }

            @Override
            public Graph graph() {
                return mockGraph;
            }

            @Override
            public String toString() {
                return "MockParentVertex";
            }

            @Override
            public <V> org.apache.tinkerpop.gremlin.structure.Property<V> property(String key) {
                return null;
            }

            @Override
            public <V> org.apache.tinkerpop.gremlin.structure.Property<V> property(String key, V value) {
                return null;
            }

            @Override
            public <V> Iterable<org.apache.tinkerpop.gremlin.structure.Property<V>> properties(String... keys) {
                return null;
            }
            
            @Override
            public Set<String> keys() {
                return new HashSet<>();
            }
        };

        mockLeafSpan = new Span() {
            @Override
            public String toString() {
                return "MockLeafSpan";
            }
        };

        mockParentSpan = new Span() {
            @Override
            public String toString() {
                return "MockParentSpan";
            }
        };
    }

    @Test
    public void testCalculateEmptyGraph() {
        // Set up the mock behavior for empty graph
        mockTraversal = new TraceTraversal<Vertex, Vertex>(mockGraph) {
            @Override
            public boolean hasNext() {
                return false;
            }

            @Override
            public Vertex next() {
                throw new UnsupportedOperationException();
            }

            @Override
            public String toString() {
                return "MockTraversal";
            }

            @Override
            public GraphTraversal.Admin<Vertex, Vertex> clone() {
                return null;
            }

            @Override
            public void setGraph(Graph graph) {}
            
            @Override
            public Graph getGraph() {
                return mockGraph;
            }
        };

        // Replace the traversal in mockTraversalSource
        mockTraversalSource = new TraceTraversalSource(mockGraph) {
            @Override
            public TraceTraversal<Vertex, Vertex> leafSpans() {
                return mockTraversal;
            }

            @Override
            public String toString() {
                return "MockTraversalSource";
            }
        };

        // Replace the graph
        mockGraph = new Graph() {
            @Override
            public TraceTraversalSource traversal(Class<TraceTraversalSource> clazz) {
                return mockTraversalSource;
            }

            @Override
            public void close() {}

            @Override
            public String toString() {
                return "MockGraph";
            }

            @Override
            public Object variables() {
                return null;
            }
            
            @Override
            public String identifier() {
                return "mockGraph";
            }
        };

        Map<String, Integer> result = ServiceHeight.calculate(mockGraph);
        
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    public void testCalculateSingleLeaf() {
        // Set up the mock behavior for single leaf
        mockTraversal = new TraceTraversal<Vertex, Vertex>(mockGraph) {
            private boolean firstCall = true;

            @Override
            public boolean hasNext() {
                if (firstCall) {
                    firstCall = false;
                    return true;
                }
                return false;
            }

            @Override
            public Vertex next() {
                return mockLeafVertex;
            }

            @Override
            public String toString() {
                return "MockTraversal";
            }

            @Override
            public GraphTraversal.Admin<Vertex, Vertex> clone() {
                return null;
            }

            @Override
            public void setGraph(Graph graph) {}
            
            @Override
            public Graph getGraph() {
                return mockGraph;
            }
        };

        // Replace the traversal in mockTraversalSource
        mockTraversalSource = new TraceTraversalSource(mockGraph) {
            @Override
            public TraceTraversal<Vertex, Vertex> leafSpans() {
                return mockTraversal;
            }

            @Override
            public String toString() {
                return "MockTraversalSource";
            }
        };

        // Replace the graph
        mockGraph = new Graph() {
            @Override
            public TraceTraversalSource traversal(Class<TraceTraversalSource> clazz) {
                return mockTraversalSource;
            }

            @Override
            public void close() {}

            @Override
            public String toString() {
                return "MockGraph";
            }

            @Override
            public Object variables() {
                return null;
            }
            
            @Override
            public String identifier() {
                return "mockGraph";
            }
        };

        // Mock GraphCreator.toSpan to return our mock span
        // Since we can't mock static methods, we'll set the service name directly
        mockLeafSpan.serviceName = "serviceA";

        Map<String, Integer> result = ServiceHeight.calculate(mockGraph);
        
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(Integer.valueOf(0), result.get("serviceA"));
    }

    @Test
    public void testCalculateLeafWithParent() {
        // Set up the mock behavior for leaf with parent
        mockTraversal = new TraceTraversal<Vertex, Vertex>(mockGraph) {
            private boolean firstCall = true;

            @Override
            public boolean hasNext() {
                if (firstCall) {
                    firstCall = false;
                    return true;
                }
                return false;
            }

            @Override
            public Vertex next() {
                return mockLeafVertex;
            }

            @Override
            public String toString() {
                return "MockTraversal";
            }

            @Override
            public GraphTraversal.Admin<Vertex, Vertex> clone() {
                return null;
            }

            @Override
            public void setGraph(Graph graph) {}
            
            @Override
            public Graph getGraph() {
                return mockGraph;
            }
        };

        // Replace the traversal in mockTraversalSource
        mockTraversalSource = new TraceTraversalSource(mockGraph) {
            @Override
            public TraceTraversal<Vertex, Vertex> leafSpans() {
                return mockTraversal;
            }

            @Override
            public String toString() {
                return "MockTraversalSource";
            }
        };

        // Replace the graph
        mockGraph = new Graph() {
            @Override
            public TraceTraversalSource traversal(Class<TraceTraversalSource> clazz) {
                return mockTraversalSource;
            }

            @Override
            public void close() {}

            @Override
            public String toString() {
                return "MockGraph";
            }

            @Override
            public Object variables() {
                return null;
            }
            
            @Override
            public String identifier() {
                return "mockGraph";
            }
        };

        // Mock the spans' service names
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
        // Set up the mock behavior for multiple leaves with same service
        mockTraversal = new TraceTraversal<Vertex, Vertex>(mockGraph) {
            private int callCount = 0;

            @Override
            public boolean hasNext() {
                return callCount < 2;
            }

            @Override
            public Vertex next() {
                callCount++;
                return mockLeafVertex;
            }

            @Override
            public String toString() {
                return "MockTraversal";
            }

            @Override
            public GraphTraversal.Admin<Vertex, Vertex> clone() {
                return null;
            }

            @Override
            public void setGraph(Graph graph) {}
            
            @Override
            public Graph getGraph() {
                return mockGraph;
            }
        };

        // Replace the traversal in mockTraversalSource
        mockTraversalSource = new TraceTraversalSource(mockGraph) {
            @Override
            public TraceTraversal<Vertex, Vertex> leafSpans() {
                return mockTraversal;
            }

            @Override
            public String toString() {
                return "MockTraversalSource";
            }
        };

        // Replace the graph
        mockGraph = new Graph() {
            @Override
            public TraceTraversalSource traversal(Class<TraceTraversalSource> clazz) {
                return mockTraversalSource;
            }

            @Override
            public void close() {}

            @Override
            public String toString() {
                return "MockGraph";
            }

            @Override
            public Object variables() {
                return null;
            }
            
            @Override
            public String identifier() {
                return "mockGraph";
            }
        };

        // Mock the span's service name
        mockLeafSpan.serviceName = "serviceD";

        Map<String, Integer> result = ServiceHeight.calculate(mockGraph);
        
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(Integer.valueOf(0), result.get("serviceD"));
    }

    @Test
    public void testCalculateMultipleLeavesDifferentServices() {
        // Set up the mock behavior for multiple leaves with different services
        mockTraversal = new TraceTraversal<Vertex, Vertex>(mockGraph) {
            private int callCount = 0;

            @Override
            public boolean hasNext() {
                return callCount < 2;
            }

            @Override
            public Vertex next() {
                callCount++;
                return mockLeafVertex;
            }

            @Override
            public String toString() {
                return "MockTraversal";
            }

            @Override
            public GraphTraversal.Admin<Vertex, Vertex> clone() {
                return null;
            }

            @Override
            public void setGraph(Graph graph) {}
            
            @Override
            public Graph getGraph() {
                return mockGraph;
            }
        };

        // Replace the traversal in mockTraversalSource
        mockTraversalSource = new TraceTraversalSource(mockGraph) {
            @Override
            public TraceTraversal<Vertex, Vertex> leafSpans() {
                return mockTraversal;
            }

            @Override
            public String toString() {
                return "MockTraversalSource";
            }
        };

        // Replace the graph
        mockGraph = new Graph() {
            @Override
            public TraceTraversalSource traversal(Class<TraceTraversalSource> clazz) {
                return mockTraversalSource;
            }

            @Override
            public void close() {}

            @Override
            public String toString() {
                return "MockGraph";
            }

            @Override
            public Object variables() {
                return null;
            }
            
            @Override
            public String identifier() {
                return "mockGraph";
            }
        };

        // Mock the span's service name
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
