package com.sap.olingo.jpa.metadata.core.edm.mapper.impl;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class IntermediateCollectionProperty_ArrowTest {

  @Test
  public void collectionFlags() {
    IntermediateCollectionProperty property = new IntermediateCollectionProperty(null, null, null, null);

    assertTrue(property.isCollection());
    assertFalse(property.isAssociation());
    assertFalse(property.isEtag());
    assertFalse(property.isKey());
    assertFalse(property.isSearchable());
    assertFalse(property.isStream());
  }

  @Test
  public void partnerIsNull() {
    IntermediateCollectionProperty property = new IntermediateCollectionProperty(null, null, null, null);

    assertNull(property.getPartner());
  }

  @Test
  public void defaultValueIsNull() throws Exception {
    IntermediateCollectionProperty property = new IntermediateCollectionProperty(null, null, null, null);

    assertNull(property.getDeafultValue());
  }

  @Test
  public void versionIsAlwaysFalse() {
    IntermediateCollectionProperty property = new IntermediateCollectionProperty(null, null, null, null);

    property.determineIsVersion();

    assertFalse(property.isVersion);
  }
}
