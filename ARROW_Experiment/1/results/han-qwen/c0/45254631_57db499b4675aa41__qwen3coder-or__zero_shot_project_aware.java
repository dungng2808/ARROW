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
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

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
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

public class EntityFacade_ArrowTest {

    @Mock
    private EntityService entityService;

    @Mock
    private FilePathService filePathService;

    private EntityFacade entityFacade;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        Injector injector = Guice.createInjector(binder -> {
            binder.bind(EntityService.class).toInstance(entityService);
            binder.bind(FilePathService.class).toInstance(filePathService);
        });
        entityFacade = injector.getInstance(EntityFacade.class);
    }

    @Test
    public void testInitConfiguration() {
        entityFacade.initConfiguration();
        assertNull(entityFacade.getClass().getDeclaredField("entity").get(entityFacade));
    }

    @Test
    public void testReadEntityByDn() throws Exception {
        Attributes mockAttributes = mock(Attributes.class);
        when(entityService.getEntityByDn(anyString())).thenReturn(mockAttributes);

        entityFacade.readEntityByDn("cn=test,dc=example,dc=com");

        assertEquals(mockAttributes, entityFacade.getClass().getDeclaredField("entity").get(entityFacade));
    }

    @Test
    public void testEntityExists() throws Exception {
        Attributes mockAttributes = mock(Attributes.class);
        entityFacade.getClass().getDeclaredField("entity").set(entityFacade, mockAttributes);

        entityFacade.entityExists();
    }

    @Test(expected = AssertionError.class)
    public void testEntityExistsWhenEntityIsNull() throws Exception {
        entityFacade.getClass().getDeclaredField("entity").set(entityFacade, null);

        entityFacade.entityExists();
    }

    @Test
    public void testEntityDoesNotExist() throws Exception {
        entityFacade.getClass().getDeclaredField("entity").set(entityFacade, null);

        entityFacade.entityDoesNotExist();
    }

    @Test(expected = AssertionError.class)
    public void testEntityDoesNotExistWhenEntityIsNotNull() throws Exception {
        Attributes mockAttributes = mock(Attributes.class);
        entityFacade.getClass().getDeclaredField("entity").set(entityFacade, mockAttributes);

        entityFacade.entityDoesNotExist();
    }

    @Test
    public void testDeleteEntityByDn() {
        entityFacade.deleteEntityByDn("cn=test,dc=example,dc=com");

        verify(entityService).deleteEntityByDn("cn=test,dc=example,dc=com");
    }

    @Test(expected = CukesRuntimeException.class)
    public void testGetAttributeWhenEntityIsNull() throws Exception {
        entityFacade.getClass().getDeclaredField("entity").set(entityFacade, null);

        entityFacade.getAttribute("cn");
    }

    @Test
    public void testGetAttribute() throws Exception {
        Attributes mockAttributes = mock(Attributes.class);
        Attribute mockAttribute = mock(Attribute.class);
        when(mockAttributes.get("cn")).thenReturn(mockAttribute);
        entityFacade.getClass().getDeclaredField("entity").set(entityFacade, mockAttributes);

        Attribute result = entityFacade.getAttribute("cn");

        assertEquals(mockAttribute, result);
    }

    @Test(expected = CukesRuntimeException.class)
    public void testGetNotNullAttributeWhenEntityIsNull() throws Exception {
        entityFacade.getClass().getDeclaredField("entity").set(entityFacade, null);

        entityFacade.getNotNullAttribute("cn");
    }

    @Test(expected = AssertionError.class)
    public void testGetNotNullAttributeWhenAttributeIsNull() throws Exception {
        Attributes mockAttributes = mock(Attributes.class);
        when(mockAttributes.get("cn")).thenReturn(null);
        entityFacade.getClass().getDeclaredField("entity").set(entityFacade, mockAttributes);

        entityFacade.getNotNullAttribute("cn");
    }

    @Test
    public void testGetNotNullAttribute() throws Exception {
        Attributes mockAttributes = mock(Attributes.class);
        Attribute mockAttribute = mock(Attribute.class);
        when(mockAttributes.get("cn")).thenReturn(mockAttribute);
        entityFacade.getClass().getDeclaredField("entity").set(entityFacade, mockAttributes);

        Attribute result = entityFacade.getNotNullAttribute("cn");

        assertEquals(mockAttribute, result);
    }

    @Test
    public void testEntityHasAttributeWithValue() throws Exception {
        Attributes mockAttributes = mock(Attributes.class);
        Attribute mockAttribute = mock(Attribute.class);
        when(mockAttributes.get("cn")).thenReturn(mockAttribute);
        when(mockAttribute.size()).thenReturn(1);
        when(mockAttribute.get(0)).thenReturn("testValue");
        entityFacade.getClass().getDeclaredField("entity").set(entityFacade, mockAttributes);

        entityFacade.entityHasAttributeWithValue("cn", "testValue");
    }

    @Test(expected = AssertionError.class)
    public void testEntityHasAttributeWithValueWhenValueNotFound() throws Exception {
        Attributes mockAttributes = mock(Attributes.class);
        Attribute mockAttribute = mock(Attribute.class);
        when(mockAttributes.get("cn")).thenReturn(mockAttribute);
        when(mockAttribute.size()).thenReturn(1);
        when(mockAttribute.get(0)).thenReturn("otherValue");
        entityFacade.getClass().getDeclaredField("entity").set(entityFacade, mockAttributes);

        entityFacade.entityHasAttributeWithValue("cn", "testValue");
    }

    @Test(expected = CukesRuntimeException.class)
    public void testEntityHasAttributeWithValueWhenNamingExceptionOccurs() throws Exception {
        Attributes mockAttributes = mock(Attributes.class);
        Attribute mockAttribute = mock(Attribute.class);
        when(mockAttributes.get("cn")).thenReturn(mockAttribute);
        when(mockAttribute.size()).thenReturn(1);
        when(mockAttribute.get(0)).thenThrow(new NamingException());
        entityFacade.getClass().getDeclaredField("entity").set(entityFacade, mockAttributes);

        entityFacade.entityHasAttributeWithValue("cn", "testValue");
    }

    @Test
    public void testToStringWithByteArray() throws Exception {
        byte[] bytes = "test".getBytes("UTF-8");
        String result = entityFacade.getClass().getDeclaredMethod("toString", Object.class).invoke(entityFacade, bytes).toString();

        assertEquals("test", result);
    }

    @Test
    public void testToStringWithCharArray() throws Exception {
        char[] chars = {'t', 'e', 's', 't'};
        String result = entityFacade.getClass().getDeclaredMethod("toString", Object.class).invoke(entityFacade, chars).toString();

        assertEquals("test", result);
    }

    @Test
    public void testToStringWithArray() throws Exception {
        int[] array = {1, 2, 3};
        String result = entityFacade.getClass().getDeclaredMethod("toString", Object.class).invoke(entityFacade, array).toString();

        assertEquals("[1, 2, 3]", result);
    }

    @Test
    public void testToStringWithString() throws Exception {
        String result = entityFacade.getClass().getDeclaredMethod("toString", Object.class).invoke(entityFacade, "test").toString();

        assertEquals("test", result);
    }

    @Test
    public void testEntityHasAttributeWithValueOtherThat() throws Exception {
        Attributes mockAttributes = mock(Attributes.class);
        Attribute mockAttribute = mock(Attribute.class);
        when(mockAttributes.get("cn")).thenReturn(mockAttribute);
        when(mockAttribute.contains("testValue")).thenReturn(false);
        entityFacade.getClass().getDeclaredField("entity").set(entityFacade, mockAttributes);

        entityFacade.entityHasAttributeWithValueOtherThat("cn", "testValue");
    }

    @Test(expected = AssertionError.class)
    public void testEntityHasAttributeWithValueOtherThatWhenValueExists() throws Exception {
        Attributes mockAttributes = mock(Attributes.class);
        Attribute mockAttribute = mock(Attribute.class);
        when(mockAttributes.get("cn")).thenReturn(mockAttribute);
        when(mockAttribute.contains("testValue")).thenReturn(true);
        entityFacade.getClass().getDeclaredField("entity").set(entityFacade, mockAttributes);

        entityFacade.entityHasAttributeWithValueOtherThat("cn", "testValue");
    }

    @Test
    public void testEntityContainsAttribute() throws Exception {
        Attributes mockAttributes = mock(Attributes.class);
        Attribute mockAttribute = mock(Attribute.class);
        when(mockAttributes.get("cn")).thenReturn(mockAttribute);
        entityFacade.getClass().getDeclaredField("entity").set(entityFacade, mockAttributes);

        entityFacade.entityContainsAttribute("cn");
    }

    @Test(expected = AssertionError.class)
    public void testEntityContainsAttributeWhenAttributeIsNull() throws Exception {
        Attributes mockAttributes = mock(Attributes.class);
        when(mockAttributes.get("cn")).thenReturn(null);
        entityFacade.getClass().getDeclaredField("entity").set(entityFacade, mockAttributes);

        entityFacade.entityContainsAttribute("cn");
    }

    @Test
    public void testEntityDoesNotContainAttribute() throws Exception {
        Attributes mockAttributes = mock(Attributes.class);
        when(mockAttributes.get("cn")).thenReturn(null);
        entityFacade.getClass().getDeclaredField("entity").set(entityFacade, mockAttributes);

        entityFacade.entityDoesNotContainAttribute("cn");
    }

    @Test(expected = AssertionError.class)
    public void testEntityDoesNotContainAttributeWhenAttributeExists() throws Exception {
        Attributes mockAttributes = mock(Attributes.class);
        Attribute mockAttribute = mock(Attribute.class);
        when(mockAttributes.get("cn")).thenReturn(mockAttribute);
        entityFacade.getClass().getDeclaredField("entity").set(entityFacade, mockAttributes);

        entityFacade.entityDoesNotContainAttribute("cn");
    }

    @Test
    public void testEntityHasAttributeAsArrayOfSize() throws Exception {
        Attributes mockAttributes = mock(Attributes.class);
        Attribute mockAttribute = mock(Attribute.class);
        when(mockAttributes.get("cn")).thenReturn(mockAttribute);
        NamingEnumeration mockEnum = mock(NamingEnumeration.class);
        when(mockEnum.hasMore()).thenReturn(true, false);
        when(mockEnum.next()).thenReturn("value1");
        when(mockAttribute.getAll()).thenReturn(mockEnum);
        entityFacade.getClass().getDeclaredField("entity").set(entityFacade, mockAttributes);

        entityFacade.entityHasAttributeAsArrayOfSize("cn", "=", 1);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testEntityHasAttributeAsArrayOfSizeWithInvalidOperator() throws Exception {
        Attributes mockAttributes = mock(Attributes.class);
        Attribute mockAttribute = mock(Attribute.class);
        when(mockAttributes.get("cn")).thenReturn(mockAttribute);
        entityFacade.getClass().getDeclaredField("entity").set(entityFacade, mockAttributes);

        entityFacade.entityHasAttributeAsArrayOfSize("cn", "invalid", 1);
    }

    @Test(expected = CukesRuntimeException.class)
    public void testEntityHasAttributeAsArrayOfSizeWhenNamingExceptionOccurs() throws Exception {
        Attributes mockAttributes = mock(Attributes.class);
        Attribute mockAttribute = mock(Attribute.class);
        when(mockAttributes.get("cn")).thenReturn(mockAttribute);
        NamingEnumeration mockEnum = mock(NamingEnumeration.class);
        when(mockEnum.hasMore()).thenThrow(new NamingException());
        when(mockAttribute.getAll()).thenReturn(mockEnum);
        entityFacade.getClass().getDeclaredField("entity").set(entityFacade, mockAttributes);

        entityFacade.entityHasAttributeAsArrayOfSize("cn", "=", 1);
    }

    @Test
    public void testEntityHasAttributeWithValueMatchingPattern() throws Exception {
        Attributes mockAttributes = mock(Attributes.class);
        Attribute mockAttribute = mock(Attribute.class);
        when(mockAttributes.get("cn")).thenReturn(mockAttribute);
        NamingEnumeration mockEnum = mock(NamingEnumeration.class);
        when(mockEnum.hasMore()).thenReturn(true, false);
        when(mockEnum.next()).thenReturn("testValue");
        when(mockAttribute.getAll()).thenReturn(mockEnum);
        entityFacade.getClass().getDeclaredField("entity").set(entityFacade, mockAttributes);

        entityFacade.entityHasAttributeWithValueMatchingPattern("cn", "test.*");
    }

    @Test(expected = AssertionError.class)
    public void testEntityHasAttributeWithValueMatchingPatternWhenNoMatch() throws Exception {
        Attributes mockAttributes = mock(Attributes.class);
        Attribute mockAttribute = mock(Attribute.class);
        when(mockAttributes.get("cn")).thenReturn(mockAttribute);
        NamingEnumeration mockEnum = mock(NamingEnumeration.class);
        when(mockEnum.hasMore()).thenReturn(true, false);
        when(mockEnum.next()).thenReturn("otherValue");
        when(mockAttribute.getAll()).thenReturn(mockEnum);
        entityFacade.getClass().getDeclaredField("entity").set(entityFacade, mockAttributes);

        entityFacade.entityHasAttributeWithValueMatchingPattern("cn", "test.*");
    }

    @Test(expected = CukesRuntimeException.class)
    public void testEntityHasAttributeWithValueMatchingPatternWhenNamingExceptionOccurs() throws Exception {
        Attributes mockAttributes = mock(Attributes.class);
        Attribute mockAttribute = mock(Attribute.class);
        when(mockAttributes.get("cn")).thenReturn(mockAttribute);
        NamingEnumeration mockEnum = mock(NamingEnumeration.class);
        when(mockEnum.hasMore()).thenThrow(new NamingException());
        when(mockAttribute.getAll()).thenReturn(mockEnum);
        entityFacade.getClass().getDeclaredField("entity").set(entityFacade, mockAttributes);

        entityFacade.entityHasAttributeWithValueMatchingPattern("cn", "test.*");
    }

    @Test
    public void testEntityHasAttributeWithValueNotMatchingPattern() throws Exception {
        Attributes mockAttributes = mock(Attributes.class);
        Attribute mockAttribute = mock(Attribute.class);
        when(mockAttributes.get("cn")).thenReturn(mockAttribute);
        NamingEnumeration mockEnum = mock(NamingEnumeration.class);
        when(mockEnum.hasMore()).thenReturn(true, false);
        when(mockEnum.next()).thenReturn("otherValue");
        when(mockAttribute.getAll()).thenReturn(mockEnum);
        entityFacade.getClass().getDeclaredField("entity").set(entityFacade, mockAttributes);

        entityFacade.entityHasAttributeWithValueNotMatchingPattern("cn", "test.*");
    }

    @Test(expected = AssertionError.class)
    public void testEntityHasAttributeWithValueNotMatchingPatternWhenMatchExists() throws Exception {
        Attributes mockAttributes = mock(Attributes.class);
        Attribute mockAttribute = mock(Attribute.class);
        when(mockAttributes.get("cn")).thenReturn(mockAttribute);
        NamingEnumeration mockEnum = mock(NamingEnumeration.class);
        when(mockEnum.hasMore()).thenReturn(true, false);
        when(mockEnum.next()).thenReturn("testValue");
        when(mockAttribute.getAll()).thenReturn(mockEnum);
        entityFacade.getClass().getDeclaredField("entity").set(entityFacade, mockAttributes);

        entityFacade.entityHasAttributeWithValueNotMatchingPattern("cn", "test.*");
    }

    @Test(expected = CukesRuntimeException.class)
    public void testEntityHasAttributeWithValueNotMatchingPatternWhenNamingExceptionOccurs() throws Exception {
        Attributes mockAttributes = mock(Attributes.class);
        Attribute mockAttribute = mock(Attribute.class);
        when(mockAttributes.get("cn")).thenReturn(mockAttribute);
        NamingEnumeration mockEnum = mock(NamingEnumeration.class);
        when(mockEnum.hasMore()).thenThrow(new NamingException());
        when(mockAttribute.getAll()).thenReturn(mockEnum);
        entityFacade.getClass().getDeclaredField("entity").set(entityFacade, mockAttributes);

        entityFacade.entityHasAttributeWithValueNotMatchingPattern("cn", "test.*");
    }

    @Test
    public void testImportLdif() {
        String ldifContent = "dn: cn=test,dc=example,dc=com\n" +
                             "objectClass: top\n" +
                             "cn: test";

        entityFacade.importLdif(ldifContent);

        verify(entityService, times(1)).createEntity(anyString(), any(Attributes.class));
    }

    @Test(expected = CukesRuntimeException.class)
    public void testImportLdifWhenIOExceptionOccurs() {
        String ldifContent = "dn: cn=test,dc=example,dc=com\n" +
                             "objectClass: top\n" +
                             "cn: test";

        doThrow(new IOException()).when(entityService).createEntity(anyString(), any(Attributes.class));

        entityFacade.importLdif(ldifContent);
    }

    @Test
    public void testImportLdifFromFile() throws Exception {
        String ldifFile = "/path/to/file.ldif";
        String normalizedPath = "/normalized/path/to/file.ldif";
        FileInputStream mockStream = mock(FileInputStream.class);

        when(filePathService.normalize(ldifFile)).thenReturn(normalizedPath);
        when(new FileInputStream(normalizedPath)).thenReturn(mockStream);

        entityFacade.importLdifFromFile(ldifFile);

        verify(entityService, times(1)).createEntity(anyString(), any(Attributes.class));
    }

    @Test(expected = CukesRuntimeException.class)
    public void testImportLdifFromFileWhenFileNotFoundExceptionOccurs() throws Exception {
        String ldifFile = "/path/to/file.ldif";

        when(filePathService.normalize(ldifFile)).thenThrow(new FileNotFoundException());

        entityFacade.importLdifFromFile(ldifFile);
    }

    @Test
    public void testEntityMatchesLDIF() throws Exception {
        String ldifContent = "dn: cn=test,dc=example,dc=com\n" +
                             "objectClass: top\n" +
                             "cn: test";

        Attributes mockAttributes = mock(Attributes.class);
        Attribute mockAttribute = mock(Attribute.class);
        when(mockAttributes.get("cn")).thenReturn(mockAttribute);
        when(mockAttribute.getAll()).thenReturn(Collections.enumeration(Arrays.asList("test")));
        when(LDIFUtils.read(any(ByteArrayInputStream.class))).thenReturn(Collections.singletonMap("cn=test,dc=example,dc=com", mockAttributes));

        entityFacade.getClass().getDeclaredField("entity").set(entityFacade, mockAttributes);

        entityFacade.entityMatchesLDIF(ldifContent);
    }

    @Test(expected = CukesRuntimeException.class)
    public void testEntityMatchesLDIFWhenNamingExceptionOccurs() throws Exception {
        String ldifContent = "dn: cn=test,dc=example,dc=com\n" +
                             "objectClass: top\n" +
                             "cn: test";

        Attributes mockAttributes = mock(Attributes.class);
        Attribute mockAttribute = mock(Attribute.class);
        when(mockAttributes.get("cn")).thenReturn(mockAttribute);
        when(mockAttribute.getAll()).thenThrow(new NamingException());
        when(LDIFUtils.read(any(ByteArrayInputStream.class))).thenReturn(Collections.singletonMap("cn=test,dc=example,dc=com", mockAttributes));

        entityFacade.getClass().getDeclaredField("entity").set(entityFacade, mockAttributes);

        entityFacade.entityMatchesLDIF(ldifContent);
    }

    @Test
    public void testSearchByFilter() throws Exception {
        List<Attributes> mockResults = Collections.singletonList(mock(Attributes.class));
        when(entityService.searchByFilter(anyString(), anyString())).thenReturn(mockResults);

        entityFacade.searchByFilter("dc=example,dc=com", "(objectClass=*)");

        assertEquals(mockResults, entityFacade.getClass().getDeclaredField("searchResults").get(entityFacade));
    }

    @Test
    public void testSearchResultHasSize() throws Exception {
        List<Attributes> mockResults = Arrays.asList(mock(Attributes.class), mock(Attributes.class));
        entityFacade.getClass().getDeclaredField("searchResults").set(entityFacade, mockResults);

        entityFacade.searchResultHasSize("=", 2);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testSearchResultHasSizeWithInvalidOperator() throws Exception {
        List<Attributes> mockResults = Arrays.asList(mock(Attributes.class), mock(Attributes.class));
        entityFacade.getClass().getDeclaredField("searchResults").set(entityFacade, mockResults);

        entityFacade.searchResultHasSize("invalid", 2);
    }

    @Test
    public void testTakeEntityFromSearchResults() throws Exception {
        List<Attributes> mockResults = Arrays.asList(mock(Attributes.class), mock(Attributes.class));
        entityFacade.getClass().getDeclaredField("searchResults").set(entityFacade, mockResults);

        entityFacade.takeEntityFromSearchResults(1);

        assertEquals(mockResults.get(1), entityFacade.getClass().getDeclaredField("entity").get(entityFacade));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testTakeEntityFromSearchResultsWithInvalidIndex() throws Exception {
        List<Attributes> mockResults = Arrays.asList(mock(Attributes.class), mock(Attributes.class));
        entityFacade.getClass().getDeclaredField("searchResults").set(entityFacade, mockResults);

        entityFacade.takeEntityFromSearchResults(5);
    }
}
