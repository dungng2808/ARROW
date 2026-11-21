package lv.ctco.cukes.ldap.facade;

import org.junit.Test;

import javax.naming.directory.Attribute;
import javax.naming.directory.BasicAttribute;
import javax.naming.directory.BasicAttributes;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;

public class EntityFacade_ArrowTest {

    @Test
    public void initConfigurationClearsLoadedEntity() {
        EntityFacade facade = new EntityFacade();
        facade.initConfiguration();

        facade.entityDoesNotExist();
        assertNull(facade.getAttribute("cn"));
    }

    @Test
    public void getAttributeReturnsAttributeFromLoadedEntity() {
        EntityFacade facade = new EntityFacade();
        BasicAttributes attributes = new BasicAttributes();
        Attribute expected = new BasicAttribute("cn", "Alice");
        attributes.put(expected);
        facade.readEntityByDn("cn=Alice");

        facade.entity = attributes;
        assertSame(expected, facade.getAttribute("cn"));
    }

    @Test
    public void getNotNullAttributeReturnsPresentAttribute() {
        EntityFacade facade = new EntityFacade();
        BasicAttributes attributes = new BasicAttributes();
        Attribute expected = new BasicAttribute("cn", "Alice");
        attributes.put(expected);
        facade.entity = attributes;

        assertSame(expected, facade.getNotNullAttribute("cn"));
    }

    @Test
    public void entityHasAttributeWithValueAcceptsPresentValue() {
        EntityFacade facade = new EntityFacade();
        BasicAttributes attributes = new BasicAttributes();
        attributes.put(new BasicAttribute("cn", "Alice"));
        facade.entity = attributes;

        facade.entityHasAttributeWithValue("cn", "Alice");
    }

    @Test
    public void entityHasAttributeWithValueOtherThatAcceptsAbsentValue() {
        EntityFacade facade = new EntityFacade();
        BasicAttributes attributes = new BasicAttributes();
        attributes.put(new BasicAttribute("cn", "Alice"));
        facade.entity = attributes;

        facade.entityHasAttributeWithValueOtherThat("cn", "Bob");
    }

    @Test
    public void entityContainsAttributeAcceptsPresentAttribute() {
        EntityFacade facade = new EntityFacade();
        BasicAttributes attributes = new BasicAttributes();
        attributes.put(new BasicAttribute("cn", "Alice"));
        facade.entity = attributes;

        facade.entityContainsAttribute("cn");
    }

    @Test
    public void entityDoesNotContainAttributeAcceptsAbsentAttribute() {
        EntityFacade facade = new EntityFacade();
        facade.entity = new BasicAttributes();

        facade.entityDoesNotContainAttribute("cn");
    }

    @Test
    public void entityHasAttributeAsArrayOfSizeChecksEquality() {
        EntityFacade facade = new EntityFacade();
        BasicAttributes attributes = new BasicAttributes();
        BasicAttribute attribute = new BasicAttribute("cn");
        attribute.add("Alice");
        attribute.add("Bob");
        attributes.put(attribute);
        facade.entity = attributes;

        facade.entityHasAttributeAsArrayOfSize("cn", "=", 2);
    }

    @Test
    public void entityHasAttributeWithValueMatchingPatternAcceptsMatchingValue() {
        EntityFacade facade = new EntityFacade();
        BasicAttributes attributes = new BasicAttributes();
        attributes.put(new BasicAttribute("cn", "Alice Smith"));
        facade.entity = attributes;

        facade.entityHasAttributeWithValueMatchingPattern("cn", "Alice");
    }

    @Test
    public void entityHasAttributeWithValueNotMatchingPatternAcceptsNonmatchingValue() {
        EntityFacade facade = new EntityFacade();
        BasicAttributes attributes = new BasicAttributes();
        attributes.put(new BasicAttribute("cn", "Alice"));
        facade.entity = attributes;

        facade.entityHasAttributeWithValueNotMatchingPattern("cn", "Bob");
    }

    @Test
    public void searchResultHasSizeChecksEmptyResults() {
        EntityFacade facade = new EntityFacade();

        facade.searchResultHasSize("=", 0);
    }

    @Test
    public void takeEntityFromSearchResultsRejectsInvalidIndex() {
        EntityFacade facade = new EntityFacade();

        try {
            facade.takeEntityFromSearchResults(0);
        } catch (IllegalArgumentException expected) {
            assertEquals("Cannot extract entity from search results set by index 0. Total result set size is 0",
                expected.getMessage());
            return;
        }
        throw new AssertionError("Expected IllegalArgumentException");
    }
}
