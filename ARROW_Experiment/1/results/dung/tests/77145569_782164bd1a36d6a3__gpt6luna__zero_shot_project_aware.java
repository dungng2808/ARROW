package com.sap.olingo.jpa.metadata.core.edm.mapper.impl;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class IntermediateCollectionProperty_ArrowTest {

  @Test
  public void collectionFlagsHaveExpectedValues() throws Exception {
    IntermediateCollectionProperty property = new IntermediateCollectionProperty(null, null, null, null);

    assertTrue(property.isCollection());
    assertFalse(property.isAssociation());
    assertFalse(property.isEtag());
    assertFalse(property.isKey());
    assertFalse(property.isSearchable());
    assertFalse(property.isStream());
    assertNull(property.getPartner());
  }

  @Test
  public void defaultValueIsNull() throws Exception {
    IntermediateCollectionProperty property = new IntermediateCollectionProperty(null, null, null, null);

    assertNull(property.getDeafultValue());
  }

  @Test
  public void versionIsAlwaysFalse() throws Exception {
    IntermediateCollectionProperty property = new IntermediateCollectionProperty(null, null, null, null);

    property.determineIsVersion();

    assertFalse(property.isVersion);
  }
}
