package com.sap.olingo.jpa.metadata.core.edm.mapper.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.lang.reflect.AnnotatedElement;
import java.util.ArrayList;
import java.util.List;

import javax.persistence.CollectionTable;
import javax.persistence.JoinColumn;
import javax.persistence.metamodel.PluralAttribute;
import javax.persistence.metamodel.Type;

import org.apache.olingo.commons.api.edm.FullQualifiedName;
import org.junit.Before;
import org.junit.Test;

import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAAssociationPath;
import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAAttribute;
import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPACollectionAttribute;
import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAEdmNameBuilder;
import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAEntityType;
import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAJoinTable;
import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAOnConditionItem;
import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAPath;
import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAStructuredType;
import com.sap.olingo.jpa.metadata.core.edm.mapper.exception.ODataJPAModelException;

public class IntermediateCollectionProperty_ArrowTest {

    private IntermediateCollectionProperty property;
    private JPAEdmNameBuilder nameBuilder;
    private PluralAttribute<?, ?, ?> jpaAttribute;
    private IntermediateSchema schema;
    private IntermediateStructuredType<?> parent;
    private AnnotatedElement annotatedElement;
    private CollectionTable collectionTable;
    private JoinColumn joinColumn;
    private JPAEntityType entityType;
    private IntermediateJoinColumn joinColumnMock;
    private JPAPath pathMock;

    @Before
    public void setUp() throws Exception {
        nameBuilder = mock(JPAEdmNameBuilder.class);
        jpaAttribute = mock(PluralAttribute.class);
        schema = mock(IntermediateSchema.class);
        parent = mock(IntermediateStructuredType.class);
        annotatedElement = mock(AnnotatedElement.class);
        collectionTable = mock(CollectionTable.class);
        joinColumn = mock(JoinColumn.class);
        entityType = mock(JPAEntityType.class);
        joinColumnMock = mock(IntermediateJoinColumn.class);
        pathMock = mock(JPAPath.class);

        when(jpaAttribute.getJavaMember()).thenReturn(annotatedElement);
        when(annotatedElement.getAnnotation(CollectionTable.class)).thenReturn(collectionTable);
        when(collectionTable.name()).thenReturn("test_table");
        when(collectionTable.joinColumns()).thenReturn(new JoinColumn[]{joinColumn});
        when(joinColumn.referencedColumnName()).thenReturn("ref_column");
        when(joinColumn.name()).thenReturn("join_column");

        property = new IntermediateCollectionProperty(nameBuilder, jpaAttribute, schema, parent);
    }

    @Test
    public void testIsCollection() {
        assertTrue(property.isCollection());
    }

    @Test
    public void testIsComplex() throws Exception {
        Type<?> elementType = mock(Type.class);
        when(((PluralAttribute<?, ?, ?>) jpaAttribute).getElementType()).thenReturn(elementType);
        when(elementType.getPersistenceType()).thenReturn(javax.persistence.metamodel.Type.PersistenceType.EMBEDDABLE);
        assertTrue(property.isComplex());
    }

    @Test
    public void testIsComplexFalse() throws Exception {
        Type<?> elementType = mock(Type.class);
        when(((PluralAttribute<?, ?, ?>) jpaAttribute).getElementType()).thenReturn(elementType);
        when(elementType.getPersistenceType()).thenReturn(javax.persistence.metamodel.Type.PersistenceType.BASIC);
        assertFalse(property.isComplex());
    }

    @Test
    public void testIsEtag() {
        assertFalse(property.isEtag());
    }

    @Test
    public void testIsKey() {
        assertFalse(property.isKey());
    }

    @Test
    public void testIsSearchable() {
        assertFalse(property.isSearchable());
    }

    @Test
    public void testIsStream() {
        assertFalse(property.isStream());
    }

    @Test
    public void testGetJoinTable() throws Exception {
        JPAJoinTable joinTable = property.getJoinTable();
        assertNotNull(joinTable);
    }

    @Test
    public void testGetSourceType() {
        assertEquals(parent, property.getSourceType());
    }

    @Test
    public void testGetTargetEntity() throws Exception {
        when(schema.getEntityType("", "", "test_table")).thenReturn(entityType);
        JPAStructuredType targetEntity = property.getTargetEntity();
        assertEquals(entityType, targetEntity);
    }

    @Test
    public void testGetTargetAttribute() throws Exception {
        when(schema.getEntityType("", "", "test_table")).thenReturn(entityType);
        JPAAttribute targetAttribute = property.getTargetAttribute();
        assertNull(targetAttribute);
    }

    @Test
    public void testGetPath() throws Exception {
        JPAAssociationPath path = property.getPath();
        assertNotNull(path);
    }

    @Test
    public void testGetPartner() {
        assertNull(property.getPartner());
    }

    @Test
    public void testIsAssociation() {
        assertFalse(property.isAssociation());
    }

    @Test
    public void testGetDeafultValue() throws Exception {
        assertNull(property.getDeafultValue());
    }

    @Test
    public void testDetermineType() throws Exception {
        Type<?> elementType = mock(Type.class);
        when(((PluralAttribute<?, ?, ?>) jpaAttribute).getElementType()).thenReturn(elementType);
        when(elementType.getPersistenceType()).thenReturn(javax.persistence.metamodel.Type.PersistenceType.BASIC);
        FullQualifiedName type = property.determineType();
        assertNotNull(type);
    }

    @Test
    public void testDetermineStructuredType() throws Exception {
        Type<?> elementType = mock(Type.class);
        when(((PluralAttribute<?, ?, ?>) jpaAttribute).getElementType()).thenReturn(elementType);
        when(elementType.getPersistenceType()).thenReturn(javax.persistence.metamodel.Type.PersistenceType.EMBEDDABLE);
        property.determineStructuredType();
        // Just checking that it doesn't throw an exception
    }

    @Test
    public void testDetermineEntityType() throws Exception {
        Type<?> elementType = mock(Type.class);
        when(((PluralAttribute<?, ?, ?>) jpaAttribute).getElementType()).thenReturn(elementType);
        when(elementType.getJavaType()).thenReturn(String.class);
        Class<?> entityType = property.determineEntityType();
        assertEquals(String.class, entityType);
    }

    @Test
    public void testDetermineIsVersion() {
        property.determineIsVersion();
        // Just checking that it doesn't throw an exception
    }

    @Test
    public void testDetermineStreamInfo() throws Exception {
        property.determineStreamInfo();
        // Just checking that it doesn't throw an exception
    }

    @Test
    public void testCheckConsistancy() throws Exception {
        property.checkConsistancy();
        // Just checking that it doesn't throw an exception
    }

    @Test
    public void testLazyBuildEdmItem() throws Exception {
        property.lazyBuildEdmItem();
        // Just checking that it doesn't throw an exception
    }

    @Test
    public void testAsAssociation() throws Exception {
        JPAAssociationPath association = property.asAssociation();
        assertNotNull(association);
    }

    @Test
    public void testCopyConstructor() throws Exception {
        IntermediateProperty pathRoot = mock(IntermediateProperty.class);
        when(pathRoot.getExternalName()).thenReturn("root");
        IntermediateCollectionProperty original = mock(IntermediateCollectionProperty.class);
        when(original.nameBuilder).thenReturn(nameBuilder);
        when(original.jpaAttribute).thenReturn(jpaAttribute);
        when(original.schema).thenReturn(schema);
        when(original.path).thenReturn(pathMock);
        when(original.getExternalName()).thenReturn("external_name");
        when(pathMock.getPath()).thenReturn(new ArrayList<JPAElement>());
        when(pathMock.getAlias()).thenReturn("alias");

        IntermediateCollectionProperty copy = new IntermediateCollectionProperty(original, parent, pathRoot);
        assertNotNull(copy);
    }
}
