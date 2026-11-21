package org.neo4j.helpers.json.document.impl;

import org.junit.Test;
import org.neo4j.graphdb.Direction;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Relationship;
import org.neo4j.graphdb.RelationshipType;
import org.neo4j.helpers.json.document.context.DocumentRelationContext;
import org.neo4j.logging.Log;

import static org.easymock.EasyMock.*;
import static org.junit.Assert.*;

public class DocumentRelationBuilderByKey_ArrowTest {

    @Test
    public void returnsNullWhenDocumentKeyIsNull() {
        DocumentRelationBuilderByKey builder = new DocumentRelationBuilderByKey();
        DocumentRelationContext context = createMock(DocumentRelationContext.class);
        expect(context.getDocumentKey()).andReturn(null);
        replay(context);

        assertNull(builder.buildRelation(null, null, context));
        verify(context);
    }

    @Test
    public void returnsNullWhenDocumentKeyIsBlank() {
        DocumentRelationBuilderByKey builder = new DocumentRelationBuilderByKey();
        DocumentRelationContext context = createMock(DocumentRelationContext.class);
        expect(context.getDocumentKey()).andReturn("   ");
        replay(context);

        assertNull(builder.buildRelation(null, null, context));
        verify(context);
    }

    @Test
    public void createsRelationshipWhenNoMatchingRelationshipExists() {
        DocumentRelationBuilderByKey builder = new DocumentRelationBuilderByKey();
        Node parent = createMock(Node.class);
        Node child = createMock(Node.class);
        Relationship relationship = createMock(Relationship.class);
        DocumentRelationContext context = createMock(DocumentRelationContext.class);
        RelationshipType type = RelationshipType.withName("key");

        expect(context.getDocumentKey()).andReturn("key");
        expect(child.getRelationships(Direction.INCOMING, type)).andReturn(
                java.util.Collections.<Relationship>emptyList());
        expect(parent.createRelationshipTo(child, type)).andReturn(relationship);
        replay(parent, child, relationship, context);

        assertSame(relationship, builder.buildRelation(parent, child, context));
        verify(parent, child, relationship, context);
    }

    @Test
    public void returnsExistingRelationshipBetweenParentAndChild() {
        DocumentRelationBuilderByKey builder = new DocumentRelationBuilderByKey();
        Node parent = createMock(Node.class);
        Node child = createMock(Node.class);
        Node otherParent = createMock(Node.class);
        Relationship existing = createMock(Relationship.class);
        DocumentRelationContext context = createMock(DocumentRelationContext.class);
        RelationshipType type = RelationshipType.withName("key");

        expect(context.getDocumentKey()).andReturn("key");
        expect(child.getRelationships(Direction.INCOMING, type)).andReturn(
                java.util.Collections.singletonList(existing));
        expect(existing.getStartNode()).andReturn(parent);
        expect(parent.getId()).andReturn(1L);
        expect(parent.getId()).andReturn(1L);
        replay(parent, child, otherParent, existing, context);

        assertSame(existing, builder.buildRelation(parent, child, context));
        verify(parent, child, existing, context);
    }

    @Test
    public void deleteRelationsReturnsEmptySetWhenDocumentKeyIsBlank() {
        DocumentRelationBuilderByKey builder = new DocumentRelationBuilderByKey();
        DocumentRelationContext context = createMock(DocumentRelationContext.class);
        expect(context.getDocumentKey()).andReturn("");
        replay(context);

        assertTrue(builder.deleteRelations(context).isEmpty());
        verify(context);
    }

    @Test
    public void deleteRelationsReturnsOrphanNodesAfterDeletingRelationships() {
        DocumentRelationBuilderByKey builder = new DocumentRelationBuilderByKey();
        GraphDatabaseService db = createMock(GraphDatabaseService.class);
        Log log = createMock(Log.class);
        DocumentRelationContext context = createMock(DocumentRelationContext.class);
        Node parent = createMock(Node.class);
        Node child = createMock(Node.class);
        Relationship relationship = createMock(Relationship.class);
        org.neo4j.graphdb.Result result = createMock(org.neo4j.graphdb.Result.class);

        expect(context.getDocumentKey()).andReturn("key");
        expect(db.execute("MATCH (p)-[r:key]->(c) RETURN r")).andReturn(result);
        expect(result.forEachRemaining(anyObject(java.util.function.Consumer.class))).andAnswer(
                new IAnswer<Void>() {
                    @Override
                    public Void answer() {
                        java.util.function.Consumer<java.util.Map<String, Object>> consumer =
                                (java.util.function.Consumer<java.util.Map<String, Object>>) getCurrentArguments()[0];
                        java.util.Map<String, Object> row = new java.util.HashMap<String, Object>();
                        row.put("r", relationship);
                        consumer.accept(row);
                        return null;
                    }
                });
        expect(relationship.getStartNode()).andReturn(parent);
        expect(relationship.getEndNode()).andReturn(child);
        relationship.delete();
        expect(log.isDebugEnabled()).andReturn(false);
        expect(parent.getRelationships()).andReturn(java.util.Collections.<Relationship>emptyList());
        expect(child.getRelationships()).andReturn(java.util.Collections.<Relationship>emptyList());
        replay(db, log, context, parent, child, relationship, result);

        builder.setDb(db);
        builder.setLog(log);
        java.util.Set<Node> orphans = builder.deleteRelations(context);

        assertEquals(2, orphans.size());
        assertTrue(orphans.contains(parent));
        assertTrue(orphans.contains(child));
        verify(db, log, context, parent, child, relationship, result);
    }
}
