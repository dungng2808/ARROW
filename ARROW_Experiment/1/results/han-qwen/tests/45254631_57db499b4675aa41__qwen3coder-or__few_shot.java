package lv.ctco.cukes.ldap.facade;

import com.google.inject.Guice;
import com.google.inject.Injector;
import cucumber.runtime.CucumberException;
import lv.ctco.cukes.core.CukesRuntimeException;
import lv.ctco.cukes.core.internal.resources.FilePathService;
import lv.ctco.cukes.ldap.internal.EntityService;
import lv.ctco.cukes.ldap.internal.ldif.LDIFUtils;
import org.junit.Before;
import org.junit.Test;

import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.Attribute;
import javax.naming.directory.Attributes;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
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
        
        // Inject dependencies manually since we're not using Guice fully
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
        assertNull(entityFacade.getClass().getDeclaredField("entity").get(entityFacade));
    }

    @Test
    public void testReadEntityByDn() throws Exception {
        Attributes mockAttributes = mock(Attributes.class);
        when(entityService.getEntityByDn("cn=test,dc=example,dc=com")).thenReturn(mockAttributes);
        
        entityFacade.readEntityByDn("cn=test,dc=example,dc=com");
        
        assertEquals(mockAttributes, entityFacade.getClass().getDeclaredField("entity").get(entityFacade));
    }

    @Test
    public void testEntityExists() throws Exception {
        Attributes mockAttributes = mock(Attributes.class);
        java.lang.reflect.Field field = EntityFacade.class.getDeclaredField("entity");
        field.setAccessible(true);
        field.set(entityFacade, mockAttributes);
        
        entityFacade.entityExists();
    }

    @Test(expected = AssertionError.class)
    public void testEntityExistsFailsWhenEntityIsNull() throws Exception {
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
        
        entityFacade.entityDoesNotExist();
    }

    @Test(expected = AssertionError.class)
    public void testEntityDoesNotExistFailsWhenEntityIsNotNull() throws Exception {
        Attributes mockAttributes = mock(Attributes.class);
        java.lang.reflect.Field field = EntityFacade.class.getDeclaredField("entity");
        field.setAccessible(true);
        field.set(entityFacade, mockAttributes);
        
        entityFacade.entityDoesNotExist();
    }

    @Test
    public void testDeleteEntityByDn() throws Exception {
        entityFacade.deleteEntityByDn("cn=test,dc=example,dc=com");
        verify(entityService).deleteEntityByDn("cn=test,dc=example,dc=com");
    }

    @Test(expected = CukesRuntimeException.class)
    public void testGetAttributeThrowsWhenEntityIsNull() {
        entityFacade.getAttribute("cn");
    }

    @Test
    public void testGetAttributeReturnsAttribute() throws Exception {
        Attributes mockAttributes = mock(Attributes.class);
        Attribute mockAttribute = mock(Attribute.class);
        when(entityService.getEntityByDn("cn=test,dc=example,dc=com")).thenReturn(mockAttributes);
        when(mockAttributes.get("cn")).thenReturn(mockAttribute);
        
        entityFacade.readEntityByDn("cn=test,dc=example,dc=com");
        Attribute result = entityFacade.getAttribute("cn");
        
        assertEquals(mockAttribute, result);
    }

    @Test(expected = CukesRuntimeException.class)
    public void testGetNotNullAttributeThrowsWhenEntityIsNull() {
        entityFacade.getNotNullAttribute("cn");
    }

    @Test(expected = AssertionError.class)
    public void testGetNotNullAttributeThrowsWhenAttributeIsNull() throws Exception {
        Attributes mockAttributes = mock(Attributes.class);
        when(entityService.getEntityByDn("cn=test,dc=example,dc=com")).thenReturn(mockAttributes);
        when(mockAttributes.get("cn")).thenReturn(null);
        
        entityFacade.readEntityByDn("cn=test,dc=example,dc=com");
        entityFacade.getNotNullAttribute("cn");
    }

    @Test
    public void testGetNotNullAttributeReturnsAttribute() throws Exception {
        Attributes mockAttributes = mock(Attributes.class);
        Attribute mockAttribute = mock(Attribute.class);
        when(entityService.getEntityByDn("cn=test,dc=example,dc=com")).thenReturn(mockAttributes);
        when(mockAttributes.get("cn")).thenReturn(mockAttribute);
        
        entityFacade.readEntityByDn("cn=test,dc=example,dc=com");
        Attribute result = entityFacade.getNotNullAttribute("cn");
        
        assertEquals(mockAttribute, result);
    }

    @Test
    public void testEntityHasAttributeWithValue() throws Exception {
        Attributes mockAttributes = mock(Attributes.class);
        Attribute mockAttribute = mock(Attribute.class);
        when(entityService.getEntityByDn("cn=test,dc=example,dc=com")).thenReturn(mockAttributes);
        when(mockAttributes.get("cn")).thenReturn(mockAttribute);
        when(mockAttribute.size()).thenReturn(1);
        when(mockAttribute.get(0)).thenReturn("testValue");
        
        entityFacade.readEntityByDn("cn=test,dc=example,dc=com");
        entityFacade.entityHasAttributeWithValue("cn", "testValue");
    }

    @Test(expected = AssertionError.class)
    public void testEntityHasAttributeWithValueFailsWhenValueNotPresent() throws Exception {
        Attributes mockAttributes = mock(Attributes.class);
        Attribute mockAttribute = mock(Attribute.class);
        when(entityService.getEntityByDn("cn=test,dc=example,dc=com")).thenReturn(mockAttributes);
        when(mockAttributes.get("cn")).thenReturn(mockAttribute);
        when(mockAttribute.size()).thenReturn(1);
        when(mockAttribute.get(0)).thenReturn("otherValue");
        
        entityFacade.readEntityByDn("cn=test,dc=example,dc=com");
        entityFacade.entityHasAttributeWithValue("cn", "testValue");
    }

    @Test(expected = CukesRuntimeException.class)
    public void testEntityHasAttributeWithValueFailsOnNamingException() throws Exception {
        Attributes mockAttributes = mock(Attributes.class);
        Attribute mockAttribute = mock(Attribute.class);
        when(entityService.getEntityByDn("cn=test,dc=example,dc=com")).thenReturn(mockAttributes);
        when(mockAttributes.get("cn")).thenReturn(mockAttribute);
        when(mockAttribute.size()).thenReturn(1);
        when(mockAttribute.get(0)).thenThrow(new NamingException());
        
        entityFacade.readEntityByDn("cn=test,dc=example,dc=com");
        entityFacade.entityHasAttributeWithValue("cn", "testValue");
    }

    @Test
    public void testEntityHasAttributeWithValueOtherThat() throws Exception {
        Attributes mockAttributes = mock(Attributes.class);
        Attribute mockAttribute = mock(Attribute.class);
        when(entityService.getEntityByDn("cn=test,dc=example,dc=com")).thenReturn(mockAttributes);
        when(mockAttributes.get("cn")).thenReturn(mockAttribute);
        when(mockAttribute.contains("testValue")).thenReturn(false);
        
        entityFacade.readEntityByDn("cn=test,dc=example,dc=com");
        entityFacade.entityHasAttributeWithValueOtherThat("cn", "testValue");
    }

    @Test(expected = AssertionError.class)
    public void testEntityHasAttributeWithValueOtherThatFailsWhenValueIsPresent() throws Exception {
        Attributes mockAttributes = mock(Attributes.class);
        Attribute mockAttribute = mock(Attribute.class);
        when(entityService.getEntityByDn("cn=test,dc=example,dc=com")).thenReturn(mockAttributes);
        when(mockAttributes.get("cn")).thenReturn(mockAttribute);
        when(mockAttribute.contains("testValue")).thenReturn(true);
        
        entityFacade.readEntityByDn("cn=test,dc=example,dc=com");
        entityFacade.entityHasAttributeWithValueOtherThat("cn", "testValue");
    }

    @Test
    public void testEntityContainsAttribute() throws Exception {
        Attributes mockAttributes = mock(Attributes.class);
        Attribute mockAttribute = mock(Attribute.class);
        when(entityService.getEntityByDn("cn=test,dc=example,dc=com")).thenReturn(mockAttributes);
        when(mockAttributes.get("cn")).thenReturn(mockAttribute);
        
        entityFacade.readEntityByDn("cn=test,dc=example,dc=com");
        entityFacade.entityContainsAttribute("cn");
    }

    @Test(expected = AssertionError.class)
    public void testEntityContainsAttributeFailsWhenAttributeIsNull() throws Exception {
        Attributes mockAttributes = mock(Attributes.class);
        when(entityService.getEntityByDn("cn=test,dc=example,dc=com")).thenReturn(mockAttributes);
        when(mockAttributes.get("cn")).thenReturn(null);
        
        entityFacade.readEntityByDn("cn=test,dc=example,dc=com");
        entityFacade.entityContainsAttribute("cn");
    }

    @Test
    public void testEntityDoesNotContainAttribute() throws Exception {
        Attributes mockAttributes = mock(Attributes.class);
        when(entityService.getEntityByDn("cn=test,dc=example,dc=com")).thenReturn(mockAttributes);
        when(mockAttributes.get("cn")).thenReturn(null);
        
        entityFacade.readEntityByDn("cn=test,dc=example,dc=com");
        entityFacade.entityDoesNotContainAttribute("cn");
    }

    @Test(expected = AssertionError.class)
    public void testEntityDoesNotContainAttributeFailsWhenAttributeIsNotNull() throws Exception {
        Attributes mockAttributes = mock(Attributes.class);
        Attribute mockAttribute = mock(Attribute.class);
        when(entityService.getEntityByDn("cn=test,dc=example,dc=com")).thenReturn(mockAttributes);
        when(mockAttributes.get("cn")).thenReturn(mockAttribute);
        
        entityFacade.readEntityByDn("cn=test,dc=example,dc=com");
        entityFacade.entityDoesNotContainAttribute("cn");
    }

    @Test
    public void testEntityHasAttributeAsArrayOfSize() throws Exception {
        Attributes mockAttributes = mock(Attributes.class);
        Attribute mockAttribute = mock(Attribute.class);
        when(entityService.getEntityByDn("cn=test,dc=example,dc=com")).thenReturn(mockAttributes);
        when(mockAttributes.get("cn")).thenReturn(mockAttribute);
        when(mockAttribute.getAll()).thenAnswer(invocation -> {
            NamingEnumeration<Object> mockEnum = mock(NamingEnumeration.class);
            when(mockEnum.hasMore()).thenReturn(true, false);
            when(mockEnum.next()).thenReturn("value1");
            return mockEnum;
        });
        
        entityFacade.readEntityByDn("cn=test,dc=example,dc=com");
        entityFacade.entityHasAttributeAsArrayOfSize("cn", "=", 1);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testEntityHasAttributeAsArrayOfSizeFailsOnUnknownOperator() throws Exception {
        Attributes mockAttributes = mock(Attributes.class);
        Attribute mockAttribute = mock(Attribute.class);
        when(entityService.getEntityByDn("cn=test,dc=example,dc=com")).thenReturn(mockAttributes);
        when(mockAttributes.get("cn")).thenReturn(mockAttribute);
        
        entityFacade.readEntityByDn("cn=test,dc=example,dc=com");
        entityFacade.entityHasAttributeAsArrayOfSize("cn", "unknown", 1);
    }

    @Test(expected = CukesRuntimeException.class)
    public void testEntityHasAttributeAsArrayOfSizeFailsOnNamingException() throws Exception {
        Attributes mockAttributes = mock(Attributes.class);
        Attribute mockAttribute = mock(Attribute.class);
        when(entityService.getEntityByDn("cn=test,dc=example,dc=com")).thenReturn(mockAttributes);
        when(mockAttributes.get("cn")).thenReturn(mockAttribute);
        when(mockAttribute.getAll()).thenThrow(new NamingException());
        
        entityFacade.readEntityByDn("cn=test,dc=example,dc=com");
        entityFacade.entityHasAttributeAsArrayOfSize("cn", "=", 1);
    }

    @Test
    public void testEntityHasAttributeWithValueMatchingPattern() throws Exception {
        Attributes mockAttributes = mock(Attributes.class);
        Attribute mockAttribute = mock(Attribute.class);
        when(entityService.getEntityByDn("cn=test,dc=example,dc=com")).thenReturn(mockAttributes);
        when(mockAttributes.get("cn")).thenReturn(mockAttribute);
        when(mockAttribute.getAll()).thenAnswer(invocation -> {
            NamingEnumeration<Object> mockEnum = mock(NamingEnumeration.class);
            when(mockEnum.hasMore()).thenReturn(true, false);
            when(mockEnum.next()).thenReturn("testValue");
            return mockEnum;
        });
        
        entityFacade.readEntityByDn("cn=test,dc=example,dc=com");
        entityFacade.entityHasAttributeWithValueMatchingPattern("cn", "test.*");
    }

    @Test(expected = AssertionError.class)
    public void testEntityHasAttributeWithValueMatchingPatternFailsWhenNoMatch() throws Exception {
        Attributes mockAttributes = mock(Attributes.class);
        Attribute mockAttribute = mock(Attribute.class);
        when(entityService.getEntityByDn("cn=test,dc=example,dc=com")).thenReturn(mockAttributes);
        when(mockAttributes.get("cn")).thenReturn(mockAttribute);
        when(mockAttribute.getAll()).thenAnswer(invocation -> {
            NamingEnumeration<Object> mockEnum = mock(NamingEnumeration.class);
            when(mockEnum.hasMore()).thenReturn(true, false);
            when(mockEnum.next()).thenReturn("otherValue");
            return mockEnum;
        });
        
        entityFacade.readEntityByDn("cn=test,dc=example,dc=com");
        entityFacade.entityHasAttributeWithValueMatchingPattern("cn", "test.*");
    }

    @Test(expected = CukesRuntimeException.class)
    public void testEntityHasAttributeWithValueMatchingPatternFailsOnNamingException() throws Exception {
        Attributes mockAttributes = mock(Attributes.class);
        Attribute mockAttribute = mock(Attribute.class);
        when(entityService.getEntityByDn("cn=test,dc=example,dc=com")).thenReturn(mockAttributes);
        when(mockAttributes.get("cn")).thenReturn(mockAttribute);
        when(mockAttribute.getAll()).thenThrow(new NamingException());
        
        entityFacade.readEntityByDn("cn=test,dc=example,dc=com");
        entityFacade.entityHasAttributeWithValueMatchingPattern("cn", "test.*");
    }

    @Test
    public void testEntityHasAttributeWithValueNotMatchingPattern() throws Exception {
        Attributes mockAttributes = mock(Attributes.class);
        Attribute mockAttribute = mock(Attribute.class);
        when(entityService.getEntityByDn("cn=test,dc=example,dc=com")).thenReturn(mockAttributes);
        when(mockAttributes.get("cn")).thenReturn(mockAttribute);
        when(mockAttribute.getAll()).thenAnswer(invocation -> {
            NamingEnumeration<Object> mockEnum = mock(NamingEnumeration.class);
            when(mockEnum.hasMore()).thenReturn(true, false);
            when(mockEnum.next()).thenReturn("otherValue");
            return mockEnum;
        });
        
        entityFacade.readEntityByDn("cn=test,dc=example,dc=com");
        entityFacade.entityHasAttributeWithValueNotMatchingPattern("cn", "test.*");
    }

    @Test(expected = AssertionError.class)
    public void testEntityHasAttributeWithValueNotMatchingPatternFailsWhenMatchFound() throws Exception {
        Attributes mockAttributes = mock(Attributes.class);
        Attribute mockAttribute = mock(Attribute.class);
        when(entityService.getEntityByDn("cn=test,dc=example,dc=com")).thenReturn(mockAttributes);
        when(mockAttributes.get("cn")).thenReturn(mockAttribute);
        when(mockAttribute.getAll()).thenAnswer(invocation -> {
            NamingEnumeration<Object> mockEnum = mock(NamingEnumeration.class);
            when(mockEnum.hasMore()).thenReturn(true, false);
            when(mockEnum.next()).thenReturn("testValue");
            return mockEnum;
        });
        
        entityFacade.readEntityByDn("cn=test,dc=example,dc=com");
        entityFacade.entityHasAttributeWithValueNotMatchingPattern("cn", "test.*");
    }

    @Test(expected = CukesRuntimeException.class)
    public void testEntityHasAttributeWithValueNotMatchingPatternFailsOnNamingException() throws Exception {
        Attributes mockAttributes = mock(Attributes.class);
        Attribute mockAttribute = mock(Attribute.class);
        when(entityService.getEntityByDn("cn=test,dc=example,dc=com")).thenReturn(mockAttributes);
        when(mockAttributes.get("cn")).thenReturn(mockAttribute);
        when(mockAttribute.getAll()).thenThrow(new NamingException());
        
        entityFacade.readEntityByDn("cn=test,dc=example,dc=com");
        entityFacade.entityHasAttributeWithValueNotMatchingPattern("cn", "test.*");
    }

    @Test
    public void testImportLdif() throws Exception {
        String ldifContent = "dn: cn=test,dc=example,dc=com\n" +
                            "objectClass: top\n" +
                            "cn: test\n";
        
        Map<String, Attributes> mockEntities = new HashMap<>();
        Attributes mockAttributes = mock(Attributes.class);
        mockEntities.put("cn=test,dc=example,dc=com", mockAttributes);
        
        when(LDIFUtils.read(any(InputStream.class))).thenReturn(mockEntities);
        
        entityFacade.importLdif(ldifContent);
        
        verify(entityService).createEntity(anyString(), any(Attributes.class));
    }

    @Test(expected = CukesRuntimeException.class)
    public void testImportLdifFailsOnIOException() throws Exception {
        String ldifContent = "dn: cn=test,dc=example,dc=com\n" +
                            "objectClass: top\n" +
                            "cn: test\n";
        
        doThrow(new IOException()).when(LDIFUtils.class);
        LDIFUtils.read(any(InputStream.class));
        
        entityFacade.importLdif(ldifContent);
    }

    @Test
    public void testImportLdifFromFile() throws Exception {
        String fileName = "test.ldif";
        String normalizedPath = "/path/to/test.ldif";
        InputStream mockInputStream = mock(InputStream.class);
        
        when(filePathService.normalize(fileName)).thenReturn(normalizedPath);
        when(new FileInputStream(normalizedPath)).thenReturn(mockInputStream);
        
        Map<String, Attributes> mockEntities = new HashMap<>();
        Attributes mockAttributes = mock(Attributes.class);
        mockEntities.put("cn=test,dc=example,dc=com", mockAttributes);
        
        when(LDIFUtils.read(mockInputStream)).thenReturn(mockEntities);
        
        entityFacade.importLdifFromFile(fileName);
        
        verify(entityService).createEntity(anyString(), any(Attributes.class));
    }

    @Test(expected = CukesRuntimeException.class)
    public void testImportLdifFromFileFailsOnFileNotFoundException() throws Exception {
        String fileName = "test.ldif";
        when(filePathService.normalize(fileName)).thenThrow(new FileNotFoundException());
        
        entityFacade.importLdifFromFile(fileName);
    }

    @Test
    public void testEntityMatchesLDIF() throws Exception {
        String ldifContent = "dn: cn=test,dc=example,dc=com\n" +
                            "objectClass: top\n" +
                            "cn: test\n";
        
        Map<String, Attributes> mockEntities = new HashMap<>();
        Attributes mockAttributes = mock(Attributes.class);
        mockEntities.put("cn=test,dc=example,dc=com", mockAttributes);
        
        when(LDIFUtils.read(any(InputStream.class))).thenReturn(mockEntities);
        
        entityFacade.entityMatchesLDIF(ldifContent);
    }

    @Test(expected = CukesRuntimeException.class)
    public void testEntityMatchesLDIFFailsOnIOException() throws Exception {
        String ldifContent = "dn: cn=test,dc=example,dc=com\n" +
                            "objectClass: top\n" +
                            "cn: test\n";
        
        doThrow(new IOException()).when(LDIFUtils.class);
        LDIFUtils.read(any(InputStream.class));
        
        entityFacade.entityMatchesLDIF(ldifContent);
    }

    @Test(expected = CukesRuntimeException.class)
    public void testEntityMatchesLDIFFailsOnNamingException() throws Exception {
        String ldifContent = "dn: cn=test,dc=example,dc=com\n" +
                            "objectClass: top\n" +
                            "cn: test\n";
        
        Map<String, Attributes> mockEntities = new HashMap<>();
        Attributes mockAttributes = mock(Attributes.class);
        mockEntities.put("cn=test,dc=example,dc=com", mockAttributes);
        
        when(LDIFUtils.read(any(InputStream.class))).thenReturn(mockEntities);
        doThrow(new NamingException()).when(mockAttributes).getAll();
        
        entityFacade.entityMatchesLDIF(ldifContent);
    }

    @Test
    public void testSearchByFilter() throws Exception {
        List<Attributes> mockResults = Arrays.asList(mock(Attributes.class));
        when(entityService.searchByFilter("dc=example,dc=com", "(objectClass=person)")).thenReturn(mockResults);
        
        entityFacade.searchByFilter("dc=example,dc=com", "(objectClass=person)");
        
        assertEquals(mockResults, entityFacade.getClass().getDeclaredField("searchResults").get(entityFacade));
    }

    @Test
    public void testSearchResultHasSize() throws Exception {
        List<Attributes> mockResults = Arrays.asList(mock(Attributes.class), mock(Attributes.class));
        java.lang.reflect.Field field = EntityFacade.class.getDeclaredField("searchResults");
        field.setAccessible(true);
        field.set(entityFacade, mockResults);
        
        entityFacade.searchResultHasSize("=", 2);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testSearchResultHasSizeFailsOnUnknownOperator() throws Exception {
        List<Attributes> mockResults = Arrays.asList(mock(Attributes.class));
        java.lang.reflect.Field field = EntityFacade.class.getDeclaredField("searchResults");
        field.setAccessible(true);
        field.set(entityFacade, mockResults);
        
        entityFacade.searchResultHasSize("unknown", 1);
    }

    @Test
    public void testTakeEntityFromSearchResults() throws Exception {
        List<Attributes> mockResults = Arrays.asList(mock(Attributes.class), mock(Attributes.class));
        java.lang.reflect.Field field = EntityFacade.class.getDeclaredField("searchResults");
        field.setAccessible(true);
        field.set(entityFacade, mockResults);
        
        entityFacade.takeEntityFromSearchResults(1);
        
        assertEquals(mockResults.get(1), entityFacade.getClass().getDeclaredField("entity").get(entityFacade));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testTakeEntityFromSearchResultsFailsOnInvalidIndex() throws Exception {
        List<Attributes> mockResults = Arrays.asList(mock(Attributes.class));
        java.lang.reflect.Field field = EntityFacade.class.getDeclaredField("searchResults");
        field.setAccessible(true);
        field.set(entityFacade, mockResults);
        
        entityFacade.takeEntityFromSearchResults(5);
    }
}
