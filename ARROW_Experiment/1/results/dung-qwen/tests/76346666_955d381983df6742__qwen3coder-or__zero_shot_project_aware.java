package org.neo4j.helpers.json.document.impl;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.neo4j.graphdb.*;
import org.neo4j.helpers.json.document.DocumentRelationBuilder;
import org.neo4j.helpers.json.document.context.DocumentRelationContext;
import org.neo4j.logging.Log;

import java.util.*;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

public class DocumentRelationBuilderByKey_ArrowTest {

    private DocumentRelationBuilderByKey builder;
    private GraphDatabaseService db;
    private Log log;
    private DocumentRelationContext context;
    private Node parent;
    private Node child;

    @Before
    public void setUp() {
        builder = new DocumentRelationBuilderByKey();
        db = Mockito.mock(GraphDatabaseService.class);
        log = Mockito.mock(Log.class);
        context = Mockito.mock(DocumentRelationContext.class);
        parent = Mockito.mock(Node.class);
        child = Mockito.mock(Node.class);

        builder.setDb(db);
        builder.setLog(log);
    }

    @Test
    public void testBuildRelation_WithBlankDocumentKey_ReturnsNull() {
        // Given
        when(context.getDocumentKey()).thenReturn("");
        
        // When
        Relationship result = builder.buildRelation(parent, child, context);
        
        // Then
        assertNull(result);
    }

    @Test
    public void testBuildRelation_WithNullDocumentKey_ReturnsNull() {
        // Given
        when(context.getDocumentKey()).thenReturn(null);
        
        // When
        Relationship result = builder.buildRelation(parent, child, context);
        
        // Then
        assertNull(result);
    }

    @Test
    public void testBuildRelation_WhenNoExistingRelation_CreatesNewRelation() {
        // Given
        String documentKey = "TEST_RELATION";
        when(context.getDocumentKey()).thenReturn(documentKey);
        when(parent.getId()).thenReturn(1L);
        when(child.getId()).thenReturn(2L);
        
        Iterable<Relationship> existingRelationships = Collections.emptyList();
        when(child.getRelationships(Direction.INCOMING, RelationshipType.withName(documentKey))).thenReturn(existingRelationships);
        
        Relationship newRelation = Mockito.mock(Relationship.class);
        when(parent.createRelationshipTo(child, RelationshipType.withName(documentKey))).thenReturn(newRelation);
        when(log.isDebugEnabled()).thenReturn(true);
        
        // When
        Relationship result = builder.buildRelation(parent, child, context);
        
        // Then
        assertEquals(newRelation, result);
        verify(log).debug("Create new Relation " + newRelation);
    }

    @Test
    public void testBuildRelation_WhenExistingRelationExists_ReturnsExistingRelation() {
        // Given
        String documentKey = "TEST_RELATION";
        when(context.getDocumentKey()).thenReturn(documentKey);
        when(parent.getId()).thenReturn(1L);
        when(child.getId()).thenReturn(2L);
        
        Relationship existingRelation = Mockito.mock(Relationship.class);
        when(existingRelation.getStartNode().getId()).thenReturn(1L);
        Iterable<Relationship> existingRelationships = Arrays.asList(existingRelation);
        when(child.getRelationships(Direction.INCOMING, RelationshipType.withName(documentKey))).thenReturn(existingRelationships);
        when(log.isDebugEnabled()).thenReturn(true);
        
        // When
        Relationship result = builder.buildRelation(parent, child, context);
        
        // Then
        assertEquals(existingRelation, result);
        verify(log).debug("Update Relation " + existingRelation);
    }

    @Test
    public void testBuildRelation_WhenMultipleExistingRelationsButNoneMatch_ReturnsNewRelation() {
        // Given
        String documentKey = "TEST_RELATION";
        when(context.getDocumentKey()).thenReturn(documentKey);
        when(parent.getId()).thenReturn(1L);
        when(child.getId()).thenReturn(2L);
        
        Relationship unrelatedRelation = Mockito.mock(Relationship.class);
        when(unrelatedRelation.getStartNode().getId()).thenReturn(3L);
        Iterable<Relationship> existingRelationships = Arrays.asList(unrelatedRelation);
        when(child.getRelationships(Direction.INCOMING, RelationshipType.withName(documentKey))).thenReturn(existingRelationships);
        
        Relationship newRelation = Mockito.mock(Relationship.class);
        when(parent.createRelationshipTo(child, RelationshipType.withName(documentKey))).thenReturn(newRelation);
        when(log.isDebugEnabled()).thenReturn(true);
        
        // When
        Relationship result = builder.buildRelation(parent, child, context);
        
        // Then
        assertEquals(newRelation, result);
        verify(log).debug("Create new Relation " + newRelation);
    }

    @Test
    public void testDeleteRelations_WithBlankDocumentKey_ReturnsEmptySet() {
        // Given
        when(context.getDocumentKey()).thenReturn("");
        
        // When
        Set<Node> result = builder.deleteRelations(context);
        
        // Then
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    public void testDeleteRelations_WithNullDocumentKey_ReturnsEmptySet() {
        // Given
        when(context.getDocumentKey()).thenReturn(null);
        
        // When
        Set<Node> result = builder.deleteRelations(context);
        
        // Then
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    public void testDeleteRelations_WithValidDocumentKey_DeletesRelationsAndUpdatesOrphans() {
        // Given
        String documentKey = "TEST_RELATION";
        when(context.getDocumentKey()).thenReturn(documentKey);
        
        Result result = Mockito.mock(Result.class);
        when(db.execute("MATCH (p)-[r:" + documentKey + "]->(c) RETURN r")).thenReturn(result);
        
        Relationship rel1 = Mockito.mock(Relationship.class);
        Node parent1 = Mockito.mock(Node.class);
        Node child1 = Mockito.mock(Node.class);
        when(rel1.getStartNode()).thenReturn(parent1);
        when(rel1.getEndNode()).thenReturn(child1);
        
        Map<String, Object> row1 = new HashMap<>();
        row1.put("r", rel1);
        
        when(result.iterator()).thenReturn(Collections.singleton(row1).iterator());
        when(log.isDebugEnabled()).thenReturn(true);
        
        // When
        Set<Node> orphans = builder.deleteRelations(context);
        
        // Then
        verify(rel1).delete();
        verify(log).debug("Delete relation " + rel1);
        assertTrue(orphans.isEmpty()); // No relationships left after deletion
    }
}
