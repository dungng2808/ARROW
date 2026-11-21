package lv.ctco.cukes.ldap.facade;

import lv.ctco.cukes.core.CukesRuntimeException;
import lv.ctco.cukes.ldap.internal.EntityService;
import org.junit.Test;

import javax.naming.directory.BasicAttribute;
import javax.naming.directory.BasicAttributes;
import javax.naming.directory.Attributes;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class EntityFacade_ArrowTest {

    @Test
    public void initConfigurationClearsLoadedEntity() {
        EntityFacade facade = new EntityFacade();
        facade.initConfiguration();

        facade.entityDoesNotExist();
    }

    @Test
    public void readEntityByDnLoadsEntity() {
        EntityFacade facade = new EntityFacade();
        EntityService service = mock(EntityService.class);
        Attributes attributes = new BasicAttributes();
        when(service.getEntityByDn("cn=test")).thenReturn(attributes);
        facade.entityService = service;

        facade.readEntityByDn("cn=test");

        facade.entityExists();
        assertNull(facade.getAttribute("missing"));
    }

    @Test
    public void getAttributeReturnsRequestedAttribute() {
        EntityFacade facade = new EntityFacade();
        EntityService service = mock(EntityService.class);
        Attributes attributes = new BasicAttributes();
        BasicAttribute attribute = new BasicAttribute("cn", "Alice");
        attributes.put(attribute);
        when(service.getEntityByDn("cn=alice")).thenReturn(attributes);
        facade.entityService = service;
        facade.readEntityByDn("cn=alice");

        assertEquals(attribute, facade.getAttribute("cn"));
    }

    @Test(expected = CukesRuntimeException.class)
    public void getAttributeFailsWhenEntityWasNotLoaded() {
        new EntityFacade().getAttribute("cn");
    }

    @Test
    public void getNotNullAttributeReturnsPresentAttribute() {
        EntityFacade facade = new EntityFacade();
        EntityService service = mock(EntityService.class);
        Attributes attributes = new BasicAttributes();
        BasicAttribute attribute = new BasicAttribute("cn", "Alice");
        attributes.put(attribute);
        when(service.getEntityByDn("cn=alice")).thenReturn(attributes);
        facade.entityService = service;
        facade.readEntityByDn("cn=alice");

        assertEquals(attribute, facade.getNotNullAttribute("cn"));
    }

    @Test(expected = CukesRuntimeException.class)
    public void getNotNullAttributeFailsWhenEntityWasNotLoaded() {
        new EntityFacade().getNotNullAttribute("cn");
    }

    @Test
    public void entityHasAttributeWithValueAcceptsPresentValue() {
        EntityFacade facade = facadeWithAttribute("cn", "Alice");
        facade.entityHasAttributeWithValue("cn", "Alice");
    }

    @Test
    public void entityHasAttributeWithValueOtherThatAcceptsAbsentValue() {
        EntityFacade facade = facadeWithAttribute("cn", "Alice");
        facade.entityHasAttributeWithValueOtherThat("cn", "Bob");
    }

    @Test
    public void entityContainsAttributeAcceptsPresentAttribute() {
        EntityFacade facade = facadeWithAttribute("cn", "Alice");
        facade.entityContainsAttribute("cn");
    }

    @Test
    public void entityDoesNotContainAttributeAcceptsAbsentAttribute() {
        EntityFacade facade = facadeWithAttribute("cn", "Alice");
        facade.entityDoesNotContainAttribute("sn");
    }

    @Test
    public void entityHasAttributeAsArrayOfSizeSupportsEquality() {
        EntityFacade facade = facadeWithAttribute("cn", "Alice", "Bob");
        facade.entityHasAttributeAsArrayOfSize("cn", "=", 2);
    }

    @Test
    public void entityHasAttributeAsArrayOfSizeSupportsGreaterThan() {
        EntityFacade facade = facadeWithAttribute("cn", "Alice", "Bob");
        facade.entityHasAttributeAsArrayOfSize("cn", ">", 1);
    }

    @Test
    public void entityHasAttributeAsArrayOfSizeSupportsGreaterThanOrEqual() {
        EntityFacade facade = facadeWithAttribute("cn", "Alice", "Bob");
        facade.entityHasAttributeAsArrayOfSize("cn", ">=", 2);
    }

    @Test
    public void entityHasAttributeAsArrayOfSizeSupportsLessThan() {
        EntityFacade facade = facadeWithAttribute("cn", "Alice", "Bob");
        facade.entityHasAttributeAsArrayOfSize("cn", "<", 3);
    }

    @Test
    public void entityHasAttributeAsArrayOfSizeSupportsLessThanOrEqual() {
        EntityFacade facade = facadeWithAttribute("cn", "Alice", "Bob");
        facade.entityHasAttributeAsArrayOfSize("cn", "<=", 2);
    }

    @Test
    public void entityHasAttributeAsArrayOfSizeSupportsNotEqual() {
        EntityFacade facade = facadeWithAttribute("cn", "Alice", "Bob");
        facade.entityHasAttributeAsArrayOfSize("cn", "<>", 1);
    }

    @Test(expected = IllegalArgumentException.class)
    public void entityHasAttributeAsArrayOfSizeRejectsUnknownOperator() {
        EntityFacade facade = facadeWithAttribute("cn", "Alice");
        facade.entityHasAttributeAsArrayOfSize("cn", "?", 1);
    }

    @Test
    public void entityHasAttributeWithValueMatchingPatternAcceptsMatch() {
        EntityFacade facade = facadeWithAttribute("cn", "Alice");
        facade.entityHasAttributeWithValueMatchingPattern("cn", "lic");
    }

    @Test
    public void entityHasAttributeWithValueNotMatchingPatternAcceptsNonMatch() {
        EntityFacade facade = facadeWithAttribute("cn", "Alice");
        facade.entityHasAttributeWithValueNotMatchingPattern("cn", "Bob");
    }

    @Test
    public void searchResultHasSizeChecksEmptyResults() {
        EntityFacade facade = new EntityFacade();
        facade.searchResultHasSize("=", 0);
    }

    @Test
    public void searchResultHasSizeSupportsNotEqual() {
        EntityFacade facade = new EntityFacade();
        facade.searchResultHasSize("<>", 1);
    }

    @Test(expected = IllegalArgumentException.class)
    public void searchResultHasSizeRejectsUnknownOperator() {
        new EntityFacade().searchResultHasSize("?", 0);
    }

    @Test(expected = IllegalArgumentException.class)
    public void takeEntityFromSearchResultsRejectsNegativeIndex() {
        new EntityFacade().takeEntityFromSearchResults(-1);
    }

    @Test(expected = IllegalArgumentException.class)
    public void takeEntityFromSearchResultsRejectsIndexPastEnd() {
        new EntityFacade().takeEntityFromSearchResults(0);
    }

    private EntityFacade facadeWithAttribute(String name, Object... values) {
        EntityFacade facade = new EntityFacade();
        EntityService service = mock(EntityService.class);
        Attributes attributes = new BasicAttributes();
        BasicAttribute attribute = new BasicAttribute(name);
        for (Object value : values) {
            attribute.add(value);
        }
        attributes.put(attribute);
        when(service.getEntityByDn("cn=test")).thenReturn(attributes);
        facade.entityService = service;
        facade.readEntityByDn("cn=test");
        return facade;
    }
}
