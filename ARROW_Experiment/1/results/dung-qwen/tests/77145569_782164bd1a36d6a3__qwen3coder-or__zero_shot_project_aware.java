package com.sap.olingo.jpa.metadata.core.edm.mapper.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

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

  private IntermediateCollectionProperty cut;
  private JPAEdmNameBuilder nameBuilder;
  private PluralAttribute<?, ?, ?> jpaAttribute;
  private IntermediateSchema schema;
  private IntermediateStructuredType<?> parent;
  private IntermediateProperty pathRoot;
  private AnnotatedElement annotatedElement;
  private CollectionTable collectionTable;
  private JoinColumn joinColumn;
  private JPAEntityType entityType;
  private IntermediateJoinColumn intermediateJoinColumn;
  private List<JoinColumn> joinColumnsList;

  @BeforeEach
  public void setup() throws ODataJPAModelException {
    nameBuilder = mock(JPAEdmNameBuilder.class);
    jpaAttribute = mock(PluralAttribute.class);
    schema = mock(IntermediateSchema.class);
    parent = mock(IntermediateStructuredType.class);
    pathRoot = mock(IntermediateProperty.class);
    annotatedElement = mock(AnnotatedElement.class);
    collectionTable = mock(CollectionTable.class);
    joinColumn = mock(JoinColumn.class);
    entityType = mock(JPAEntityType.class);
    intermediateJoinColumn = mock(IntermediateJoinColumn.class);
    joinColumnsList = new ArrayList<>();

    when(jpaAttribute.getJavaMember()).thenReturn(annotatedElement);
    when(annotatedElement.getAnnotation(CollectionTable.class)).thenReturn(collectionTable);
    when(collectionTable.name()).thenReturn("test_table");
    when(collectionTable.joinColumns()).thenReturn(joinColumnsList.toArray(new JoinColumn[0]));
    when(schema.getEntityType("", "", "test_table")).thenReturn(entityType);
    when(pathRoot.getExternalName()).thenReturn("root");
    when(parent.getPath("test_prop")).thenReturn(mock(JPAPath.class));
  }

  @Test
  public void testConstructorWithOriginalAndParent() throws ODataJPAModelException {
    IntermediateCollectionProperty original = mock(IntermediateCollectionProperty.class);
    when(original.nameBuilder).thenReturn(nameBuilder);
    when(original.jpaAttribute).thenReturn(jpaAttribute);
    when(original.schema).thenReturn(schema);
    when(original.getExternalName()).thenReturn("test_prop");
    when(original.path).thenReturn(null);

    cut = new IntermediateCollectionProperty(original, parent, pathRoot);

    assertEquals("root" + JPAPath.PATH_SEPERATOR + "test_prop", cut.getExternalName());
  }

  @Test
  public void testConstructorWithOriginalAndParentWithPath() throws ODataJPAModelException {
    IntermediateCollectionProperty original = mock(IntermediateCollectionProperty.class);
    when(original.nameBuilder).thenReturn(nameBuilder);
    when(original.jpaAttribute).thenReturn(jpaAttribute);
    when(original.schema).thenReturn(schema);
    when(original.getExternalName()).thenReturn("test_prop");
    
    JPAPath path = mock(JPAPath.class);
    when(original.path).thenReturn(path);
    when(path.getPath()).thenReturn(new ArrayList<>());

    cut = new IntermediateCollectionProperty(original, parent, pathRoot);

    assertEquals("root" + JPAPath.PATH_SEPERATOR + "test_prop", cut.getExternalName());
  }

  @Test
  public void testConstructorWithParameters() throws ODataJPAModelException {
    cut = new IntermediateCollectionProperty(nameBuilder, jpaAttribute, schema, parent);

    assertEquals(parent, cut.getSourceType());
  }

  @Test
  public void testAsAssociation() throws ODataJPAModelException {
    cut = new IntermediateCollectionProperty(nameBuilder, jpaAttribute, schema, parent);
    JPAAssociationPath result = cut.asAssociation();

    assertSame(result, cut.asAssociation());
  }

  @Test
  public void testGetPartner() {
    cut = new IntermediateCollectionProperty(nameBuilder, jpaAttribute, schema, parent);
    assertNull(cut.getPartner());
  }

  @Test
  public void testGetPath() throws ODataJPAModelException {
    cut = new IntermediateCollectionProperty(nameBuilder, jpaAttribute, schema, parent);
    JPAAssociationPath result = cut.getPath();

    assertSame(result, cut.asAssociation());
  }

  @Test
  public void testGetTargetAttribute() throws ODataJPAModelException {
    cut = new IntermediateCollectionProperty(nameBuilder, jpaAttribute, schema, parent);
    when(cut.isComplex()).thenReturn(false);
    when(cut.getTargetEntity()).thenReturn(mock(JPAStructuredType.class));

    JPAAttribute result = cut.getTargetAttribute();

    assertNull(result);
  }

  @Test
  public void testGetTargetEntity() throws ODataJPAModelException {
    cut = new IntermediateCollectionProperty(nameBuilder, jpaAttribute, schema, parent);
    when(cut.getJoinTable()).thenReturn(mock(JPAJoinTable.class));
    when(((JPAJoinTable) cut.getJoinTable()).getEntityType()).thenReturn(entityType);

    JPAStructuredType result = cut.getTargetEntity();

    assertSame(entityType, result);
  }

  @Test
  public void testIsAssociation() {
    cut = new IntermediateCollectionProperty(nameBuilder, jpaAttribute, schema, parent);
    assertFalse(cut.isAssociation());
  }

  @Test
  public void testIsCollection() {
    cut = new IntermediateCollectionProperty(nameBuilder, jpaAttribute, schema, parent);
    assertTrue(cut.isCollection());
  }

  @Test
  public void testIsComplex() throws ODataJPAModelException {
    cut = new IntermediateCollectionProperty(nameBuilder, jpaAttribute, schema, parent);
    Type<?> elementType = mock(Type.class);
    when(elementType.getPersistenceType()).thenReturn(javax.persistence.metamodel.Type.PersistenceType.EMBEDDABLE);
    when(((PluralAttribute<?, ?, ?>) jpaAttribute).getElementType()).thenReturn(elementType);

    assertTrue(cut.isComplex());
  }

  @Test
  public void testIsEtag() {
    cut = new IntermediateCollectionProperty(nameBuilder, jpaAttribute, schema, parent);
    assertFalse(cut.isEtag());
  }

  @Test
  public void testIsKey() {
    cut = new IntermediateCollectionProperty(nameBuilder, jpaAttribute, schema, parent);
    assertFalse(cut.isKey());
  }

  @Test
  public void testIsSearchable() {
    cut = new IntermediateCollectionProperty(nameBuilder, jpaAttribute, schema, parent);
    assertFalse(cut.isSearchable());
  }

  @Test
  public void testLazyBuildEdmItem() throws ODataJPAModelException {
    cut = new IntermediateCollectionProperty(nameBuilder, jpaAttribute, schema, parent);
    when(cut.isComplex()).thenReturn(true);
    when(schema.getComplexType("test_type")).thenReturn(null);
    when(cut.getExternalName()).thenReturn("test_prop");
    when(cut.getSourceType()).thenReturn(parent);
    when(parent.getInternalName()).thenReturn("parent_type");

    try {
      cut.lazyBuildEdmItem();
    } catch (ODataJPAModelException e) {
      assertEquals("INVALID_COLLECTION_TYPE", e.getMessageKeys().toString());
    }
  }

  @Test
  public void testCheckConsistancy() throws ODataJPAModelException {
    cut = new IntermediateCollectionProperty(nameBuilder, jpaAttribute, schema, parent);
    when(cut.hasProtection()).thenReturn(false);
    when(cut.isComplex()).thenReturn(false);
    when(cut.getStructuredType()).thenReturn(mock(JPAStructuredType.class));
    when(cut.getStructuredType().getProtections()).thenReturn(new ArrayList<>());

    cut.checkConsistancy();
  }

  @Test
  public void testCheckConsistancyWithProtection() {
    cut = new IntermediateCollectionProperty(nameBuilder, jpaAttribute, schema, parent);
    when(cut.hasProtection()).thenReturn(true);

    try {
      cut.checkConsistancy();
    } catch (ODataJPAModelException e) {
      assertEquals("NOT_SUPPORTED_PROTECTED_COLLECTION", e.getMessageKeys().toString());
    }
  }

  @Test
  public void testDetermineEntityType() throws ODataJPAModelException {
    cut = new IntermediateCollectionProperty(nameBuilder, jpaAttribute, schema, parent);
    Type<?> elementType = mock(Type.class);
    when(elementType.getJavaType()).thenReturn(String.class);
    when(((PluralAttribute<?, ?, ?>) jpaAttribute).getElementType()).thenReturn(elementType);

    Class<?> result = cut.determineEntityType();

    assertEquals(String.class, result);
  }

  @Test
  public void testDetermineIsVersion() {
    cut = new IntermediateCollectionProperty(nameBuilder, jpaAttribute, schema, parent);
    cut.determineIsVersion();
    assertFalse(cut.isVersion);
  }

  @Test
  public void testDetermineStreamInfo() throws ODataJPAModelException {
    cut = new IntermediateCollectionProperty(nameBuilder, jpaAttribute, schema, parent);
    cut.determineStreamInfo();
  }

  @Test
  public void testDetermineStructuredType() throws ODataJPAModelException {
    cut = new IntermediateCollectionProperty(nameBuilder, jpaAttribute, schema, parent);
    Type<?> elementType = mock(Type.class);
    when(elementType.getPersistenceType()).thenReturn(javax.persistence.metamodel.Type.PersistenceType.EMBEDDABLE);
    when(((PluralAttribute<?, ?, ?>) jpaAttribute).getElementType()).thenReturn(elementType);
    when(schema.getStructuredType(jpaAttribute)).thenReturn(mock(JPAStructuredType.class));

    cut.determineStructuredType();

    assertEquals(JPAStructuredType.class, cut.type.getClass());
  }

  @Test
  public void testDetermineType() throws ODataJPAModelException {
    cut = new IntermediateCollectionProperty(nameBuilder, jpaAttribute, schema, parent);
    Type<?> elementType = mock(Type.class);
    when(elementType.getPersistenceType()).thenReturn(javax.persistence.metamodel.Type.PersistenceType.BASIC);
    when(((PluralAttribute<?, ?, ?>) jpaAttribute).getElementType()).thenReturn(elementType);

    FullQualifiedName result = cut.determineType();

    assertEquals(FullQualifiedName.class, result.getClass());
  }

  @Test
  public void testGetDeafultValue() throws ODataJPAModelException {
    cut = new IntermediateCollectionProperty(nameBuilder, jpaAttribute, schema, parent);
    assertNull(cut.getDeafultValue());
  }

  @Test
  public void testGetJoinTable() throws ODataJPAModelException {
    cut = new IntermediateCollectionProperty(nameBuilder, jpaAttribute, schema, parent);
    JPAJoinTable result = cut.getJoinTable();

    assertSame(result, cut.getJoinTable());
  }

  @Test
  public void testGetSourceType() {
    cut = new IntermediateCollectionProperty(nameBuilder, jpaAttribute, schema, parent);
    assertSame(parent, cut.getSourceType());
  }

  @Test
  public void testIsStream() {
    cut = new IntermediateCollectionProperty(nameBuilder, jpaAttribute, schema, parent);
    assertFalse(cut.isStream());
  }

  @Test
  public void testGetRowType() throws ODataJPAModelException {
    cut = new IntermediateCollectionProperty(nameBuilder, jpaAttribute, schema, parent);
    Type<?> elementType = mock(Type.class);
    when(((PluralAttribute<?, ?, ?>) jpaAttribute).getElementType()).thenReturn(elementType);

    Type<?> result = cut.getRowType();

    assertSame(elementType, result);
  }

  @Test
  public void testGetJoinTableReturnsNullWhenNoAnnotation() throws ODataJPAModelException {
    when(annotatedElement.getAnnotation(CollectionTable.class)).thenReturn(null);
    cut = new IntermediateCollectionProperty(nameBuilder, jpaAttribute, schema, parent);
    JPAJoinTable result = cut.getJoinTable();

    assertNull(result);
  }

  @Test
  public void testGetJoinTableReturnsNewInstanceWhenNotCached() throws ODataJPAModelException {
    cut = new IntermediateCollectionProperty(nameBuilder, jpaAttribute, schema, parent);
    when(cut.getJoinTable()).thenReturn(null);
    JPAJoinTable result = cut.getJoinTable();

    assertNotNull(result);
  }

  @Test
  public void testGetJoinTableReturnsCachedInstance() throws ODataJPAModelException {
    cut = new IntermediateCollectionProperty(nameBuilder, jpaAttribute, schema, parent);
    JPAJoinTable cached = mock(JPAJoinTable.class);
    cut.joinTable = cached;
    JPAJoinTable result = cut.getJoinTable();

    assertSame(cached, result);
  }

  @Test
  public void testGetInversJoinColumns() throws ODataJPAModelException {
    cut = new IntermediateCollectionProperty(nameBuilder, jpaAttribute, schema, parent);
    JPAJoinTable joinTable = mock(JPAJoinTable.class);
    when(cut.getJoinTable()).thenReturn(joinTable);
    when(joinTable.getInversJoinColumns()).thenReturn(new ArrayList<>());

    List<JPAOnConditionItem> result = cut.getJoinTable().getInversJoinColumns();

    assertEquals(0, result.size());
  }

  @Test
  public void testGetJoinColumns() throws ODataJPAModelException {
    cut = new IntermediateCollectionProperty(nameBuilder, jpaAttribute, schema, parent);
    JPAJoinTable joinTable = mock(JPAJoinTable.class);
    when(cut.getJoinTable()).thenReturn(joinTable);
    when(joinTable.getJoinColumns()).thenReturn(new ArrayList<>());

    List<JPAOnConditionItem> result = cut.getJoinTable().getJoinColumns();

    assertEquals(0, result.size());
  }

  @Test
  public void testGetTableName() throws ODataJPAModelException {
    cut = new IntermediateCollectionProperty(nameBuilder, jpaAttribute, schema, parent);
    JPAJoinTable joinTable = mock(JPAJoinTable.class);
    when(cut.getJoinTable()).thenReturn(joinTable);
    when(joinTable.getTableName()).thenReturn("test_table");

    String result = cut.getJoinTable().getTableName();

    assertEquals("test_table", result);
  }

  @Test
  public void testGetLeftJoinColumns() throws ODataJPAModelException {
    cut = new IntermediateCollectionProperty(nameBuilder, jpaAttribute, schema, parent);
    JPAJoinTable joinTable = mock(JPAJoinTable.class);
    when(cut.getJoinTable()).thenReturn(joinTable);
    when(joinTable.getLeftJoinColumns()).thenReturn(new ArrayList<>());

    List<IntermediateJoinColumn> result = cut.getJoinTable().getLeftJoinColumns();

    assertEquals(0, result.size());
  }

  @Test
  public void testGetAlias() throws ODataJPAModelException {
    cut = new IntermediateCollectionProperty(nameBuilder, jpaAttribute, schema, parent);
    JPAJoinTable joinTable = mock(JPAJoinTable.class);
    when(cut.getJoinTable()).thenReturn(joinTable);
    when(joinTable.getAlias("test_field")).thenReturn("alias");

    String result = cut.getJoinTable().getAlias("test_field");

    assertEquals("alias", result);
  }

  @Test
  public void testGetEntityType() throws ODataJPAModelException {
    cut = new IntermediateCollectionProperty(nameBuilder, jpaAttribute, schema, parent);
    JPAJoinTable joinTable = mock(JPAJoinTable.class);
    when(cut.getJoinTable()).thenReturn(joinTable);
    when(joinTable.getEntityType()).thenReturn(entityType);

    JPAEntityType result = cut.getJoinTable().getEntityType();

    assertSame(entityType, result);
  }

  @Test
  public void testGetInverseAlias() throws ODataJPAModelException {
    cut = new IntermediateCollectionProperty(nameBuilder, jpaAttribute, schema, parent);
    JPAJoinTable joinTable = mock(JPAJoinTable.class);
    when(cut.getJoinTable()).thenReturn(joinTable);
    when(joinTable.getInverseAlias("test_field")).thenReturn(null);

    String result = cut.getJoinTable().getInverseAlias("test_field");

    assertNull(result);
  }
}
