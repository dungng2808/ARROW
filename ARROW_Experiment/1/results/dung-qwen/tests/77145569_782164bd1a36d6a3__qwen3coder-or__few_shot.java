package com.sap.olingo.jpa.metadata.core.edm.mapper.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
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

    @Before
    public void setUp() throws Exception {
        nameBuilder = mock(JPAEdmNameBuilder.class);
        jpaAttribute = mock(PluralAttribute.class);
        schema = mock(IntermediateSchema.class);
        parent = mock(IntermediateStructuredType.class);
        annotatedElement = mock(AnnotatedElement.class);
        collectionTable = mock(CollectionTable.class);
        joinColumn = mock(JoinColumn.class);

        when(jpaAttribute.getJavaMember()).thenReturn(annotatedElement);
        when(annotatedElement.getAnnotation(CollectionTable.class)).thenReturn(collectionTable);
        when(collectionTable.name()).thenReturn("test_table");
        when(collectionTable.joinColumns()).thenReturn(new JoinColumn[]{joinColumn});
        when(joinColumn.referencedColumnName()).thenReturn("ref_column");
        when(joinColumn.name()).thenReturn("join_column");

        property = new IntermediateCollectionProperty(nameBuilder, jpaAttribute, schema, parent);
    }

    @Test
    public void testGetJoinTable() throws Exception {
        JPAJoinTable joinTable = property.getJoinTable();
        assertEquals("test_table", joinTable.getTableName());
    }

    @Test
    public void testGetSourceType() {
        assertEquals(parent, property.getSourceType());
    }

    @Test
    public void testIsCollection() {
        assertEquals(true, property.isCollection());
    }

    @Test
    public void testIsAssociation() {
        assertEquals(false, property.isAssociation());
    }

    @Test
    public void testIsComplex() throws Exception {
        Type<?> elementType = mock(Type.class);
        when(elementType.getPersistenceType()).thenReturn(javax.persistence.metamodel.Type.PersistenceType.EMBEDDABLE);
        when(((PluralAttribute<?, ?, ?>) jpaAttribute).getElementType()).thenReturn(elementType);
        
        assertEquals(true, property.isComplex());
    }

    @Test
    public void testIsEtag() {
        assertEquals(false, property.isEtag());
    }

    @Test
    public void testIsKey() {
        assertEquals(false, property.isKey());
    }

    @Test
    public void testIsSearchable() {
        assertEquals(false, property.isSearchable());
    }

    @Test
    public void testIsStream() {
        assertEquals(false, property.isStream());
    }

    @Test
    public void testGetTargetEntity() throws Exception {
        JPAStructuredType entityType = mock(JPAStructuredType.class);
        when(schema.getEntityType("", "", "test_table")).thenReturn(entityType);
        when(collectionTable.name()).thenReturn("test_table");
        
        JPAEntityType targetEntity = property.getTargetEntity();
        assertEquals(entityType, targetEntity);
    }

    @Test
    public void testGetTargetAttribute() throws Exception {
        JPAStructuredType entityType = mock(JPAStructuredType.class);
        JPAAttribute attribute = mock(JPAAttribute.class);
        when(schema.getEntityType("", "", "test_table")).thenReturn(entityType);
        when(collectionTable.name()).thenReturn("test_table");
        when(entityType.getAttributes()).thenReturn(new ArrayList<JPAAttribute>() {{
            add(attribute);
        }});
        
        JPAAttribute targetAttribute = property.getTargetAttribute();
        assertNull(targetAttribute);
    }

    @Test
    public void testGetPath() throws Exception {
        JPAAssociationPath path = property.getPath();
        assertEquals(JPAAssociationPath.class, path.getClass());
    }

    @Test
    public void testGetPartner() {
        assertNull(property.getPartner());
    }

    @Test
    public void testAsAssociation() throws Exception {
        JPAAssociationPath association = property.asAssociation();
        assertEquals(JPAAssociationPath.class, association.getClass());
    }

    @Test
    public void testGetDeafultValue() throws Exception {
        assertNull(property.getDeafultValue());
    }

    @Test
    public void testDetermineIsVersion() {
        property.determineIsVersion();
        assertEquals(false, property.isVersion);
    }

    @Test
    public void testDetermineStreamInfo() throws Exception {
        property.determineStreamInfo();
    }

    @Test
    public void testDetermineStructuredType() {
        property.determineStructuredType();
    }

    @Test
    public void testDetermineEntityType() {
        assertEquals(Object.class, property.determineEntityType());
    }

    @Test
    public void testCheckConsistancy() throws Exception {
        property.checkConsistancy();
    }

    @Test
    public void testLazyBuildEdmItem() throws Exception {
        property.lazyBuildEdmItem();
        assertEquals(true, property.edmProperty.isCollection());
    }

    @Test
    public void testDetermineType() throws Exception {
        Type<?> elementType = mock(Type.class);
        when(elementType.getPersistenceType()).thenReturn(javax.persistence.metamodel.Type.PersistenceType.BASIC);
        when(((PluralAttribute<?, ?, ?>) jpaAttribute).getElementType()).thenReturn(elementType);
        
        FullQualifiedName type = property.determineType();
        assertEquals(FullQualifiedName.class, type.getClass());
    }
}
