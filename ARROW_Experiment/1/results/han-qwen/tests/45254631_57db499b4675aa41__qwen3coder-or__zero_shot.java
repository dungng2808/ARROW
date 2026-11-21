package lv.ctco.cukes.ldap.facade;

import com.google.inject.Guice;
import com.google.inject.Injector;
import cucumber.runtime.CucumberException;
import lv.ctco.cukes.core.CukesRuntimeException;
import lv.ctco.cukes.core.internal.resources.FilePathService;
import lv.ctco.cukes.ldap.internal.EntityService;
import lv.ctco.cukes.ldap.internal.ldif.LDIFUtils;
import org.hamcrest.Matcher;
import org.junit.Before;
import org.junit.Test;

import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.Attribute;
import javax.naming.directory.Attributes;
import java.io.ByteArrayInputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.*;

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

public class EntityFacade_ArrowTest {

    private EntityFacade entityFacade;
    private EntityService entityService;
    private FilePathService filePathService;

    @Before
    public void setUp() {
        Injector injector = Guice.createInjector();
        entityFacade = injector.getInstance(EntityFacade.class);
        entityService = mock(EntityService.class);
        filePathService = mock(FilePathService.class);

        // Inject dependencies manually since we're not using full DI
        try {
            java.lang.reflect.Field entityServiceField = EntityFacade.class.getDeclaredField("entityService");
            entityServiceField.setAccessible(true);
            entityServiceField.set(entityFacade, entityService);

            java.lang.reflect.Field filePathServiceField = EntityFacade.class.getDeclaredField("filePathService");
            filePathServiceField.setAccessible(true);
            filePathServiceField.set(entityFacade, filePathService);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    public void testInitConfiguration() {
        entityFacade.initConfiguration();
        assertNull("Entity should be null after initialization", entityFacade.getClass().getDeclaredField("entity").get(entityFacade));
    }

    @Test
    public void testReadEntityByDn() throws Exception {
        String dn = "cn=test,dc=example,dc=com";
        Attributes mockAttributes = mock(Attributes.class);
        when(entityService.getEntityByDn(dn)).thenReturn(mockAttributes);

        entityFacade.readEntityByDn(dn);

        assertEquals("Entity should be set to the returned attributes", mockAttributes, entityFacade.getClass().getDeclaredField("entity").get(entityFacade));
    }

    @Test
    public void testEntityExists() throws Exception {
        Attributes mockAttributes = mock(Attributes.class);
        java.lang.reflect.Field field = EntityFacade.class.getDeclaredField("entity");
        field.setAccessible(true);
        field.set(entityFacade, mockAttributes);

        entityFacade.entityExists(); // Should not throw
    }

    @Test(expected = AssertionError.class)
    public void testEntityExistsWhenEntityIsNull() throws Exception {
        java.lang.reflect.Field field = EntityFacade.class.getDeclaredField("entity");
        field.setAccessible(true);
        field.set(entityFacade, null);

        entityFacade.entityExists();
    }

    @Test
    public void testEntityDoesNotExist() throws Exception {
        java.lang.reflect.Field field = EntityFacade.class.getDeclaredField("entity");
        field.setAccessible(true);
        field.set(entityFacade, null);

        entityFacade.entityDoesNotExist(); // Should not throw
    }

    @Test(expected = AssertionError.class)
    public void testEntityDoesNotExistWhenEntityIsNotNull() throws Exception {
        Attributes mockAttributes = mock(Attributes.class);
        java.lang.reflect.Field field = EntityFacade.class.getDeclaredField("entity");
        field.setAccessible(true);
        field.set(entityFacade, mockAttributes);

        entityFacade.entityDoesNotExist();
    }

    @Test
    public void testDeleteEntityByDn() throws Exception {
        String dn = "cn=test,dc=example,dc=com";

        entityFacade.deleteEntityByDn(dn);

        verify(entityService).deleteEntityByDn(dn);
    }

    @Test(expected = CukesRuntimeException.class)
    public void testGetAttributeWhenEntityIsNull() throws Exception {
        java.lang.reflect.Field field = EntityFacade.class.getDeclaredField("entity");
        field.setAccessible(true);
        field.set(entityFacade, null);

        entityFacade.getAttribute("testAttribute");
    }

    @Test
    public void testGetAttributeWhenEntityIsNotNull() throws Exception {
        String attributeName = "testAttribute";
        Attribute mockAttribute = mock(Attribute.class);
        Attributes mockAttributes = mock(Attributes.class);
        when(mockAttributes.get(attributeName)).thenReturn(mockAttribute);

        java.lang.reflect.Field field = EntityFacade.class.getDeclaredField("entity");
        field.setAccessible(true);
        field.set(entityFacade, mockAttributes);

        Attribute result = entityFacade.getAttribute(attributeName);

        assertEquals("Should return the correct attribute", mockAttribute, result);
    }

    @Test(expected = CukesRuntimeException.class)
    public void testGetNotNullAttributeWhenEntityIsNull() throws Exception {
        java.lang.reflect.Field field = EntityFacade.class.getDeclaredField("entity");
        field.setAccessible(true);
        field.set(entityFacade, null);

        entityFacade.getNotNullAttribute("testAttribute");
    }

    @Test(expected = AssertionError.class)
    public void testGetNotNullAttributeWhenAttributeIsNull() throws Exception {
        String attributeName = "testAttribute";
        Attributes mockAttributes = mock(Attributes.class);
        when(mockAttributes.get(attributeName)).thenReturn(null);

        java.lang.reflect.Field field = EntityFacade.class.getDeclaredField("entity");
        field.setAccessible(true);
        field.set(entityFacade, mockAttributes);

        entityFacade.getNotNullAttribute(attributeName);
    }

    @Test
    public void testGetNotNullAttributeWhenAttributeIsNotNull() throws Exception {
        String attributeName = "testAttribute";
        Attribute mockAttribute = mock(Attribute.class);
        Attributes mockAttributes = mock(Attributes.class);
        when(mockAttributes.get(attributeName)).thenReturn(mockAttribute);

        java.lang.reflect.Field field = EntityFacade.class.getDeclaredField("entity");
        field.setAccessible(true);
        field.set(entityFacade, mockAttributes);

        Attribute result = entityFacade.getNotNullAttribute(attributeName);

        assertEquals("Should return the correct attribute", mockAttribute, result);
    }

    @Test
    public void testEntityHasAttributeWithValue() throws Exception {
        String attributeName = "testAttribute";
        String expectedValue = "expectedValue";
        Attribute mockAttribute = mock(Attribute.class);
        Attributes mockAttributes = mock(Attributes.class);
        when(mockAttributes.get(attributeName)).thenReturn(mockAttribute);
        when(mockAttribute.size()).thenReturn(1);
        when(mockAttribute.get(0)).thenReturn(expectedValue);

        java.lang.reflect.Field field = EntityFacade.class.getDeclaredField("entity");
        field.setAccessible(true);
        field.set(entityFacade, mockAttributes);

        entityFacade.entityHasAttributeWithValue(attributeName, expectedValue); // Should not throw
    }

    @Test(expected = AssertionError.class)
    public void testEntityHasAttributeWithValueWhenValueNotPresent() throws Exception {
        String attributeName = "testAttribute";
        String expectedValue = "expectedValue";
        Attribute mockAttribute = mock(Attribute.class);
        Attributes mockAttributes = mock(Attributes.class);
        when(mockAttributes.get(attributeName)).thenReturn(mockAttribute);
        when(mockAttribute.size()).thenReturn(1);
        when(mockAttribute.get(0)).thenReturn("differentValue");

        java.lang.reflect.Field field = EntityFacade.class.getDeclaredField("entity");
        field.setAccessible(true);
        field.set(entityFacade, mockAttributes);

        entityFacade.entityHasAttributeWithValue(attributeName, expectedValue);
    }

    @Test(expected = CukesRuntimeException.class)
    public void testEntityHasAttributeWithValueWhenNamingExceptionOccurs() throws Exception {
        String attributeName = "testAttribute";
        String expectedValue = "expectedValue";
        Attribute mockAttribute = mock(Attribute.class);
        Attributes mockAttributes = mock(Attributes.class);
        when(mockAttributes.get(attributeName)).thenReturn(mockAttribute);
        when(mockAttribute.size()).thenReturn(1);
        when(mockAttribute.get(0)).thenThrow(new NamingException());

        java.lang.reflect.Field field = EntityFacade.class.getDeclaredField("entity");
        field.setAccessible(true);
        field.set(entityFacade, mockAttributes);

        entityFacade.entityHasAttributeWithValue(attributeName, expectedValue);
    }

    @Test
    public void testToStringWithByteArray() throws Exception {
        byte[] bytes = "test".getBytes("UTF-8");
        String result = entityFacade.getClass().getDeclaredMethod("toString", Object.class).invoke(entityFacade, bytes).toString();

        assertEquals("Should convert byte array to UTF-8 string", "test", result);
    }

    @Test
    public void testToStringWithCharArray() throws Exception {
        char[] chars = {'t', 'e', 's', 't'};
        String result = entityFacade.getClass().getDeclaredMethod("toString", Object.class).invoke(entityFacade, chars).toString();

        assertEquals("Should convert char array to string", "test", result);
    }

    @Test
    public void testToStringWithObjectArray() throws Exception {
        Object[] array = {"a", "b"};
        String result = entityFacade.getClass().getDeclaredMethod("toString", Object.class).invoke(entityFacade, array).toString();

        assertTrue("Should contain array elements", result.contains("a"));
        assertTrue("Should contain array elements", result.contains("b"));
    }

    @Test
    public void testToStringWithString() throws Exception {
        String input = "test";
        String result = entityFacade.getClass().getDeclaredMethod("toString", Object.class).invoke(entityFacade, input).toString();

        assertEquals("Should return string representation", "test", result);
    }

    @Test
    public void testEntityHasAttributeWithValueOtherThat() throws Exception {
        String attributeName = "testAttribute";
        String value = "testValue";
        Attribute mockAttribute = mock(Attribute.class);
        Attributes mockAttributes = mock(Attributes.class);
        when(mockAttributes.get(attributeName)).thenReturn(mockAttribute);
        when(mockAttribute.contains(value)).thenReturn(false);

        java.lang.reflect.Field field = EntityFacade.class.getDeclaredField("entity");
        field.setAccessible(true);
        field.set(entityFacade, mockAttributes);

        entityFacade.entityHasAttributeWithValueOtherThat(attributeName, value); // Should not throw
    }

    @Test(expected = AssertionError.class)
    public void testEntityHasAttributeWithValueOtherThatWhenValueIsPresent() throws Exception {
        String attributeName = "testAttribute";
        String value = "testValue";
        Attribute mockAttribute = mock(Attribute.class);
        Attributes mockAttributes = mock(Attributes.class);
        when(mockAttributes.get(attributeName)).thenReturn(mockAttribute);
        when(mockAttribute.contains(value)).thenReturn(true);

        java.lang.reflect.Field field = EntityFacade.class.getDeclaredField("entity");
        field.setAccessible(true);
        field.set(entityFacade, mockAttributes);

        entityFacade.entityHasAttributeWithValueOtherThat(attributeName, value);
    }

    @Test
    public void testEntityContainsAttribute() throws Exception {
        String attributeName = "testAttribute";
        Attribute mockAttribute = mock(Attribute.class);
        Attributes mockAttributes = mock(Attributes.class);
        when(mockAttributes.get(attributeName)).thenReturn(mockAttribute);

        java.lang.reflect.Field field = EntityFacade.class.getDeclaredField("entity");
        field.setAccessible(true);
        field.set(entityFacade, mockAttributes);

        entityFacade.entityContainsAttribute(attributeName); // Should not throw
    }

    @Test(expected = AssertionError.class)
    public void testEntityContainsAttributeWhenAttributeIsNull() throws Exception {
        String attributeName = "testAttribute";
        Attributes mockAttributes = mock(Attributes.class);
        when(mockAttributes.get(attributeName)).thenReturn(null);

        java.lang.reflect.Field field = EntityFacade.class.getDeclaredField("entity");
        field.setAccessible(true);
        field.set(entityFacade, mockAttributes);

        entityFacade.entityContainsAttribute(attributeName);
    }

    @Test
    public void testEntityDoesNotContainAttribute() throws Exception {
        String attributeName = "testAttribute";
        Attributes mockAttributes = mock(Attributes.class);
        when(mockAttributes.get(attributeName)).thenReturn(null);

        java.lang.reflect.Field field = EntityFacade.class.getDeclaredField("entity");
        field.setAccessible(true);
        field.set(entityFacade, mockAttributes);

        entityFacade.entityDoesNotContainAttribute(attributeName); // Should not throw
    }

    @Test(expected = AssertionError.class)
    public void testEntityDoesNotContainAttributeWhenAttributeIsNotNull() throws Exception {
        String attributeName = "testAttribute";
        Attribute mockAttribute = mock(Attribute.class);
        Attributes mockAttributes = mock(Attributes.class);
        when(mockAttributes.get(attributeName)).thenReturn(mockAttribute);

        java.lang.reflect.Field field = EntityFacade.class.getDeclaredField("entity");
        field.setAccessible(true);
        field.set(entityFacade, mockAttributes);

        entityFacade.entityDoesNotContainAttribute(attributeName);
    }

    @Test
    public void testEntityHasAttributeAsArrayOfSize() throws Exception {
        String attributeName = "testAttribute";
        String operator = "=";
        int size = 2;
        Attribute mockAttribute = mock(Attribute.class);
        Attributes mockAttributes = mock(Attributes.class);
        when(mockAttributes.get(attributeName)).thenReturn(mockAttribute);
        NamingEnumeration mockEnum = mock(NamingEnumeration.class);
        when(mockAttribute.getAll()).thenReturn(mockEnum);
        when(mockEnum.hasMore()).thenReturn(true, true, false);

        java.lang.reflect.Field field = EntityFacade.class.getDeclaredField("entity");
        field.setAccessible(true);
        field.set(entityFacade, mockAttributes);

        entityFacade.entityHasAttributeAsArrayOfSize(attributeName, operator, size); // Should not throw
    }

    @Test(expected = IllegalArgumentException.class)
    public void testEntityHasAttributeAsArrayOfSizeWithInvalidOperator() throws Exception {
        String attributeName = "testAttribute";
        String operator = "invalid";
        int size = 2;
        Attribute mockAttribute = mock(Attribute.class);
        Attributes mockAttributes = mock(Attributes.class);
        when(mockAttributes.get(attributeName)).thenReturn(mockAttribute);

        java.lang.reflect.Field field = EntityFacade.class.getDeclaredField("entity");
        field.setAccessible(true);
        field.set(entityFacade, mockAttributes);

        entityFacade.entityHasAttributeAsArrayOfSize(attributeName, operator, size);
    }

    @Test(expected = CukesRuntimeException.class)
    public void testEntityHasAttributeAsArrayOfSizeWithNamingException() throws Exception {
        String attributeName = "testAttribute";
        String operator = "=";
        int size = 2;
        Attribute mockAttribute = mock(Attribute.class);
        Attributes mockAttributes = mock(Attributes.class);
        when(mockAttributes.get(attributeName)).thenReturn(mockAttribute);
        NamingEnumeration mockEnum = mock(NamingEnumeration.class);
        when(mockAttribute.getAll()).thenReturn(mockEnum);
        when(mockEnum.hasMore()).thenThrow(new NamingException());

        java.lang.reflect.Field field = EntityFacade.class.getDeclaredField("entity");
        field.setAccessible(true);
        field.set(entityFacade, mockAttributes);

        entityFacade.entityHasAttributeAsArrayOfSize(attributeName, operator, size);
    }

    @Test
    public void testEntityHasAttributeWithValueMatchingPattern() throws Exception {
        String attributeName = "testAttribute";
        String pattern = "test.*";
        Attribute mockAttribute = mock(Attribute.class);
        Attributes mockAttributes = mock(Attributes.class);
        when(mockAttributes.get(attributeName)).thenReturn(mockAttribute);
        NamingEnumeration mockEnum = mock(NamingEnumeration.class);
        when(mockAttribute.getAll()).thenReturn(mockEnum);
        when(mockEnum.hasMore()).thenReturn(true, false);
        when(mockEnum.next()).thenReturn("testValue");

        java.lang.reflect.Field field = EntityFacade.class.getDeclaredField("entity");
        field.setAccessible(true);
        field.set(entityFacade, mockAttributes);

        entityFacade.entityHasAttributeWithValueMatchingPattern(attributeName, pattern); // Should not throw
    }

    @Test(expected = AssertionError.class)
    public void testEntityHasAttributeWithValueMatchingPatternWhenNoMatch() throws Exception {
        String attributeName = "testAttribute";
        String pattern = "test.*";
        Attribute mockAttribute = mock(Attribute.class);
        Attributes mockAttributes = mock(Attributes.class);
        when(mockAttributes.get(attributeName)).thenReturn(mockAttribute);
        NamingEnumeration mockEnum = mock(NamingEnumeration.class);
        when(mockAttribute.getAll()).thenReturn(mockEnum);
        when(mockEnum.hasMore()).thenReturn(true, false);
        when(mockEnum.next()).thenReturn("otherValue");

        java.lang.reflect.Field field = EntityFacade.class.getDeclaredField("entity");
        field.setAccessible(true);
        field.set(entityFacade, mockAttributes);

        entityFacade.entityHasAttributeWithValueMatchingPattern(attributeName, pattern);
    }

    @Test(expected = CukesRuntimeException.class)
    public void testEntityHasAttributeWithValueMatchingPatternWithNamingException() throws Exception {
        String attributeName = "testAttribute";
        String pattern = "test.*";
        Attribute mockAttribute = mock(Attribute.class);
        Attributes mockAttributes = mock(Attributes.class);
        when(mockAttributes.get(attributeName)).thenReturn(mockAttribute);
        NamingEnumeration mockEnum = mock(NamingEnumeration.class);
        when(mockAttribute.getAll()).thenReturn(mockEnum);
        when(mockEnum.hasMore()).thenThrow(new NamingException());

        java.lang.reflect.Field field = EntityFacade.class.getDeclaredField("entity");
        field.setAccessible(true);
        field.set(entityFacade, mockAttributes);

        entityFacade.entityHasAttributeWithValueMatchingPattern(attributeName, pattern);
    }

    @Test
    public void testEntityHasAttributeWithValueNotMatchingPattern() throws Exception {
        String attributeName = "testAttribute";
        String pattern = "test.*";
        Attribute mockAttribute = mock(Attribute.class);
        Attributes mockAttributes = mock(Attributes.class);
        when(mockAttributes.get(attributeName)).thenReturn(mockAttribute);
        NamingEnumeration mockEnum = mock(NamingEnumeration.class);
        when(mockAttribute.getAll()).thenReturn(mockEnum);
        when(mockEnum.hasMore()).thenReturn(true, false);
        when(mockEnum.next()).thenReturn("otherValue");

        java.lang.reflect.Field field = EntityFacade.class.getDeclaredField("entity");
        field.setAccessible(true);
        field.set(entityFacade, mockAttributes);

        entityFacade.entityHasAttributeWithValueNotMatchingPattern(attributeName, pattern); // Should not throw
    }

    @Test(expected = AssertionError.class)
    public void testEntityHasAttributeWithValueNotMatchingPatternWhenMatchFound() throws Exception {
        String attributeName = "testAttribute";
        String pattern = "test.*";
        Attribute mockAttribute = mock(Attribute.class);
        Attributes mockAttributes = mock(Attributes.class);
        when(mockAttributes.get(attributeName)).thenReturn(mockAttribute);
        NamingEnumeration mockEnum = mock(NamingEnumeration.class);
        when(mockAttribute.getAll()).thenReturn(mockEnum);
        when(mockEnum.hasMore()).thenReturn(true, false);
        when(mockEnum.next()).thenReturn("testValue");

        java.lang.reflect.Field field = EntityFacade.class.getDeclaredField("entity");
        field.setAccessible(true);
        field.set(entityFacade, mockAttributes);

        entityFacade.entityHasAttributeWithValueNotMatchingPattern(attributeName, pattern);
    }

    @Test(expected = CukesRuntimeException.class)
    public void testEntityHasAttributeWithValueNotMatchingPatternWithNamingException() throws Exception {
        String attributeName = "testAttribute";
        String pattern = "test.*";
        Attribute mockAttribute = mock(Attribute.class);
        Attributes mockAttributes = mock(Attributes.class);
        when(mockAttributes.get(attributeName)).thenReturn(mockAttribute);
        NamingEnumeration mockEnum = mock(NamingEnumeration.class);
        when(mockAttribute.getAll()).thenReturn(mockEnum);
        when(mockEnum.hasMore()).thenThrow(new NamingException());

        java.lang.reflect.Field field = EntityFacade.class.getDeclaredField("entity");
        field.setAccessible(true);
        field.set(entityFacade, mockAttributes);

        entityFacade.entityHasAttributeWithValueNotMatchingPattern(attributeName, pattern);
    }

    @Test
    public void testImportLdif() throws Exception {
        String ldif = "dn: cn=test,dc=example,dc=com\n" +
                      "objectClass: top\n" +
                      "cn: test\n";

        entityFacade.importLdif(ldif); // Should not throw

        verify(entityService, times(1)).createEntity(anyString(), any(Attributes.class));
    }

    @Test(expected = CukesRuntimeException.class)
    public void testImportLdifWithIOException() throws Exception {
        String ldif = "dn: cn=test,dc=example,dc=com\n" +
                      "objectClass: top\n" +
                      "cn: test\n";

        doThrow(new IOException()).when(entityService).createEntity(anyString(), any(Attributes.class));

        entityFacade.importLdif(ldif);
    }

    @Test
    public void testImportLdifFromFile() throws Exception {
        String ldifFile = "/path/to/file.ldif";
        String normalizedPath = "/normalized/path/to/file.ldif";
        FileInputStream mockStream = mock(FileInputStream.class);

        when(filePathService.normalize(ldifFile)).thenReturn(normalizedPath);
        when(new FileInputStream(normalizedPath)).thenReturn(mockStream);

        entityFacade.importLdifFromFile(ldifFile); // Should not throw

        verify(entityService, times(1)).createEntity(anyString(), any(Attributes.class));
    }

    @Test(expected = CukesRuntimeException.class)
    public void testImportLdifFromFileWithFileNotFoundException() throws Exception {
        String ldifFile = "/path/to/file.ldif";

        when(filePathService.normalize(ldifFile)).thenThrow(new FileNotFoundException());

        entityFacade.importLdifFromFile(ldifFile);
    }

    @Test
    public void testEntityMatchesLDIF() throws Exception {
        String ldif = "dn: cn=test,dc=example,dc=com\n" +
                      "objectClass: top\n" +
                      "cn: test\n";

        Map<String, Attributes> mockEntities = new HashMap<>();
        Attributes mockAttributes = mock(Attributes.class);
        mockEntities.put("cn=test,dc=example,dc=com", mockAttributes);
        when(LDIFUtils.read(any(ByteArrayInputStream.class))).thenReturn(mockEntities);

        entityFacade.entityMatchesLDIF(ldif); // Should not throw
    }

    @Test(expected = CukesRuntimeException.class)
    public void testEntityMatchesLDIFWithNamingException() throws Exception {
        String ldif = "dn: cn=test,dc=example,dc=com\n" +
                      "objectClass: top\n" +
                      "cn: test\n";

        when(LDIFUtils.read(any(ByteArrayInputStream.class))).thenThrow(new NamingException());

        entityFacade.entityMatchesLDIF(ldif);
    }

    @Test(expected = CukesRuntimeException.class)
    public void testEntityMatchesLDIFWithIOException() throws Exception {
        String ldif = "dn: cn=test,dc=example,dc=com\n" +
                      "objectClass: top\n" +
                      "cn: test\n";

        when(LDIFUtils.read(any(ByteArrayInputStream.class))).thenThrow(new IOException());

        entityFacade.entityMatchesLDIF(ldif);
    }

    @Test
    public void testSearchByFilter() throws Exception {
        String dn = "dc=example,dc=com";
        String filter = "(objectClass=top)";
        List<Attributes> mockResults = Arrays.asList(mock(Attributes.class));

        when(entityService.searchByFilter(dn, filter)).thenReturn(mockResults);

        entityFacade.searchByFilter(dn, filter);

        assertEquals("Search results should be cleared and populated", mockResults, entityFacade.getClass().getDeclaredField("searchResults").get(entityFacade));
    }

    @Test
    public void testSearchResultHasSize() throws Exception {
        List<Attributes> mockResults = Arrays.asList(mock(Attributes.class), mock(Attributes.class));
        java.lang.reflect.Field field = EntityFacade.class.getDeclaredField("searchResults");
        field.setAccessible(true);
        field.set(entityFacade, mockResults);

        entityFacade.searchResultHasSize("=", 2); // Should not throw
    }

    @Test(expected = IllegalArgumentException.class)
    public void testSearchResultHasSizeWithInvalidOperator() throws Exception {
        List<Attributes> mockResults = Arrays.asList(mock(Attributes.class), mock(Attributes.class));
        java.lang.reflect.Field field = EntityFacade.class.getDeclaredField("searchResults");
        field.setAccessible(true);
        field.set(entityFacade, mockResults);

        entityFacade.searchResultHasSize("invalid", 2);
    }

    @Test
    public void testTakeEntityFromSearchResults() throws Exception {
        List<Attributes> mockResults = Arrays.asList(mock(Attributes.class), mock(Attributes.class));
        java.lang.reflect.Field field = EntityFacade.class.getDeclaredField("searchResults");
        field.setAccessible(true);
        field.set(entityFacade, mockResults);

        entityFacade.takeEntityFromSearchResults(1);

        assertEquals("Entity should be set to the selected result", mockResults.get(1), entityFacade.getClass().getDeclaredField("entity").get(entityFacade));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testTakeEntityFromSearchResultsWithInvalidIndex() throws Exception {
        List<Attributes> mockResults = Arrays.asList(mock(Attributes.class), mock(Attributes.class));
        java.lang.reflect.Field field = EntityFacade.class.getDeclaredField("searchResults");
        field.setAccessible(true);
        field.set(entityFacade, mockResults);

        entityFacade.takeEntityFromSearchResults(5);
    }
}
