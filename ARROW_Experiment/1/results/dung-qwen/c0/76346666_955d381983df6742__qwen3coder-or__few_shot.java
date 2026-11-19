package org.neo4j.helpers.json.document.impl;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.neo4j.graphdb.*;
import org.neo4j.helpers.json.document.DocumentRelationBuilder;
import org.neo4j.helpers.json.document.context.DocumentRelationContext;
import org.neo4j.logging.Log;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

public class DocumentRelationBuilderByKey_ArrowTest {

    private DocumentRelationBuilder builder;

    @Mock
    private GraphDatabaseService db;

    @Mock
    private Log log;

    @Mock
    private Node parentNode;

    @Mock
    private Node childNode;

    @Mock
    private DocumentRelationContext context;

    @Mock
    private RelationshipType relationshipType;

    @Mock
    private Relationship relationship;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        builder = new DocumentRelationBuilderByKey();
        ((DocumentRelationBuilderByKey) builder).setDb(db);
        ((DocumentRelationBuilderByKey) builder).setLog(log);
    }

    @Test
    public void testBuildRelationWithBlankDocumentKey() {
        when(context.getDocumentKey()).thenReturn("");
        Relationship result = builder.buildRelation(parentNode, childNode, context);
        assertNull(result);
    }

    @Test
    public void testBuildRelationWhenNoExistingRelation() {
        when(context.getDocumentKey()).thenReturn("KNOWS");
        when(childNode.getRelationships(Direction.INCOMING, relationshipType)).thenReturn(Collections.emptyList());
        when(parentNode.createRelationshipTo(childNode, relationshipType)).thenReturn(relationship);
        when(log.isDebugEnabled()).thenReturn(true);

        Relationship result = builder.buildRelation(parentNode, childNode, context);

        assertNotNull(result);
        assertEquals(relationship, result);
    }

    @Test
    public void testBuildRelationWhenExistingRelationExists() {
        when(context.getDocumentKey()).thenReturn("KNOWS");
        when(childNode.getRelationships(Direction.INCOMING, relationshipType)).thenReturn(Collections.singletonList(relationship));
        when(relationship.getStartNode().getId()).thenReturn(1L);
        when(parentNode.getId()).thenReturn(1L);
        when(log.isDebugEnabled()).thenReturn(true);

        Relationship result = builder.buildRelation(parentNode, childNode, context);

        assertNotNull(result);
        assertEquals(relationship, result);
    }

    @Test
    public void testDeleteRelationsWithBlankDocumentKey() {
        when(context.getDocumentKey()).thenReturn("");
        Set<Node> result = builder.deleteRelations(context);
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    public void testDeleteRelationsWithValidDocumentKey() {
        when(context.getDocumentKey()).thenReturn("KNOWS");
        when(db.execute("MATCH (p)-[r:KNOWS]->(c) RETURN r")).thenReturn(createMockResult());

        Set<Node> result = builder.deleteRelations(context);

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    private Result createMockResult() {
        return new Result() {
            @Override
            public boolean hasNext() {
                return false;
            }

            @Override
            public Map<String, Object> next() {
                return null;
            }

            @Override
            public void forEachRemaining(Consumer<Map<String, Object>> action) {
                // Mock implementation
            }

            @Override
            public void close() {
                // Mock implementation
            }

            @Override
            public Set<String> getKeys() {
                return Collections.singleton("r");
            }

            @Override
            public boolean containsField(String field) {
                return "r".equals(field);
            }

            @Override
            public Object get(String field) {
                return relationship;
            }
        };
    }
}
