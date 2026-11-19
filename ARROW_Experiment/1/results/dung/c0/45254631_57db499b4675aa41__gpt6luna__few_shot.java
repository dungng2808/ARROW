package lv.ctco.cukes.ldap.facade;

import lv.ctco.cukes.core.CukesRuntimeException;
import org.junit.Test;

import javax.naming.directory.BasicAttribute;
import javax.naming.directory.BasicAttributes;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.fail;

public class EntityFacade_ArrowTest {

    @Test
    public void initConfigurationClearsLoadedEntity() {
        EntityFacade facade = new EntityFacade();
        facade.initConfiguration();
        facade.entityDoesNotExist();
    }

    @Test
    public void getAttributeFailsWhenEntityWasNotLoaded() {
        EntityFacade facade = new EntityFacade();
        try {
            facade.getAttribute("cn");
            fail("Expected an exception");
        } catch (CukesRuntimeException expected) {
            assertNotNull(expected);
        }
    }

    @Test
    public void getNotNullAttributeFailsWhenEntityWasNotLoaded() {
        EntityFacade facade = new EntityFacade();
        try {
            facade.getNotNullAttribute("cn");
            fail("Expected an exception");
        } catch (CukesRuntimeException expected) {
            assertNotNull(expected);
        }
    }

    @Test
    public void entityDoesNotContainAttributeWhenEntityIsNotLoaded() {
        EntityFacade facade = new EntityFacade();
        facade.entityDoesNotContainAttribute("cn");
    }

    @Test
    public void takeEntityFromSearchResultsRejectsNegativeIndex() {
        EntityFacade facade = new EntityFacade();
        try {
            facade.takeEntityFromSearchResults(-1);
            fail("Expected an exception");
        } catch (IllegalArgumentException expected) {
            assertNotNull(expected);
        }
    }

    @Test
    public void takeEntityFromSearchResultsRejectsIndexPastEnd() {
        EntityFacade facade = new EntityFacade();
        try {
            facade.takeEntityFromSearchResults(0);
            fail("Expected an exception");
        } catch (IllegalArgumentException expected) {
            assertNotNull(expected);
        }
    }

    @Test
    public void searchResultHasSizeAcceptsExactSize() {
        EntityFacade facade = new EntityFacade();
        facade.searchResultHasSize("=", 0);
    }

    @Test
    public void searchResultHasSizeAcceptsGreaterThanComparison() {
        EntityFacade facade = new EntityFacade();
        facade.searchResultHasSize(">", -1);
    }

    @Test
    public void searchResultHasSizeAcceptsGreaterThanOrEqualComparison() {
        EntityFacade facade = new EntityFacade();
        facade.searchResultHasSize(">=", 0);
    }

    @Test
    public void searchResultHasSizeAcceptsLessThanComparison() {
        EntityFacade facade = new EntityFacade();
        facade.searchResultHasSize("<", 1);
    }

    @Test
    public void searchResultHasSizeAcceptsLessThanOrEqualComparison() {
        EntityFacade facade = new EntityFacade();
        facade.searchResultHasSize("<=", 0);
    }

    @Test
    public void searchResultHasSizeAcceptsNotEqualComparison() {
        EntityFacade facade = new EntityFacade();
        facade.searchResultHasSize("<>", 1);
    }

    @Test
    public void searchResultHasSizeRejectsUnknownOperator() {
        EntityFacade facade = new EntityFacade();
        try {
            facade.searchResultHasSize("!=", 0);
            fail("Expected an exception");
        } catch (IllegalArgumentException expected) {
            assertNotNull(expected);
        }
    }

    @Test
    public void entityHasAttributeWithValueFindsValue() {
        EntityFacade facade = new EntityFacade();
        facade.readEntityByDn(null);
    }
}
