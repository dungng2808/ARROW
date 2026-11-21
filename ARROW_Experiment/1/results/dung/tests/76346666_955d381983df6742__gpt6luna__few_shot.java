package org.neo4j.helpers.json.document.impl;

import org.junit.Test;
import org.neo4j.graphdb.Node;
import org.neo4j.helpers.json.document.context.DocumentRelationContext;

import static org.junit.Assert.assertNull;

public class DocumentRelationBuilderByKey_ArrowTest {

    @Test
    public void buildRelationReturnsNullForBlankDocumentKey() {
        DocumentRelationBuilderByKey builder = new DocumentRelationBuilderByKey();
        DocumentRelationContext context = new DocumentRelationContext();
        Node parent = null;
        Node child = null;

        assertNull(builder.buildRelation(parent, child, context));
    }
}
