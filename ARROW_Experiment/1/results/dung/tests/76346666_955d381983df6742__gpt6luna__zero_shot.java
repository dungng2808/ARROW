package org.neo4j.helpers.json.document.impl;

import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;

import org.junit.Test;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Relationship;
import org.neo4j.helpers.json.document.context.DocumentRelationContext;

public class DocumentRelationBuilderByKey_ArrowTest {

    @Test
    public void returnsNullWhenDocumentKeyIsBlank() {
        DocumentRelationBuilderByKey builder = new DocumentRelationBuilderByKey();
        DocumentRelationContext context = new DocumentRelationContext();
        context.setDocumentKey(" ");

        Relationship result = builder.buildRelation(null, null, context);

        assertNull(result);
    }
}
