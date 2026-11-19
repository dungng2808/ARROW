package com.amazonaws.secretsmanager.sql;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import java.sql.SQLException;

import org.junit.Test;

import com.amazonaws.secretsmanager.caching.SecretCache;
import com.amazonaws.secretsmanager.caching.SecretCacheConfiguration;
import com.amazonaws.services.secretsmanager.AWSSecretsManager;
import com.amazonaws.services.secretsmanager.AWSSecretsManagerClientBuilder;

public class AWSSecretsManagerPostgreSQLDriver_ArrowTest {

    @Test
    public void testGetPropertySubprefix() {
        AWSSecretsManagerPostgreSQLDriver driver = new AWSSecretsManagerPostgreSQLDriver();
        assertEquals("postgresql", driver.getPropertySubprefix());
    }

    @Test
    public void testIsExceptionDueToAuthenticationErrorWithSQLException() {
        AWSSecretsManagerPostgreSQLDriver driver = new AWSSecretsManagerPostgreSQLDriver();
        SQLException sqlException = mock(SQLException.class);
        when(sqlException.getSQLState()).thenReturn("28P01");
        assertTrue(driver.isExceptionDueToAuthenticationError(sqlException));
    }

    @Test
    public void testIsExceptionDueToAuthenticationErrorWithWrongSQLState() {
        AWSSecretsManagerPostgreSQLDriver driver = new AWSSecretsManagerPostgreSQLDriver();
        SQLException sqlException = mock(SQLException.class);
        when(sqlException.getSQLState()).thenReturn("28P02");
        assertFalse(driver.isExceptionDueToAuthenticationError(sqlException));
    }

    @Test
    public void testIsExceptionDueToAuthenticationErrorWithNonSQLException() {
        AWSSecretsManagerPostgreSQLDriver driver = new AWSSecretsManagerPostgreSQLDriver();
        Exception exception = new Exception();
        assertFalse(driver.isExceptionDueToAuthenticationError(exception));
    }

    @Test
    public void testConstructUrlFromEndpointPortDatabase() {
        AWSSecretsManagerPostgreSQLDriver driver = new AWSSecretsManagerPostgreSQLDriver();
        String url = driver.constructUrlFromEndpointPortDatabase("localhost", "5432", "mydb");
        assertEquals("jdbc:postgresql://localhost:5432/mydb", url);
    }

    @Test
    public void testConstructUrlFromEndpointPortDatabaseWithoutPort() {
        AWSSecretsManagerPostgreSQLDriver driver = new AWSSecretsManagerPostgreSQLDriver();
        String url = driver.constructUrlFromEndpointPortDatabase("localhost", null, "mydb");
        assertEquals("jdbc:postgresql://localhost/mydb", url);
    }

    @Test
    public void testConstructUrlFromEndpointPortDatabaseWithoutDbname() {
        AWSSecretsManagerPostgreSQLDriver driver = new AWSSecretsManagerPostgreSQLDriver();
        String url = driver.constructUrlFromEndpointPortDatabase("localhost", "5432", null);
        assertEquals("jdbc:postgresql://localhost:5432/", url);
    }

    @Test
    public void testConstructUrlFromEndpointPortDatabaseWithoutPortAndDbname() {
        AWSSecretsManagerPostgreSQLDriver driver = new AWSSecretsManagerPostgreSQLDriver();
        String url = driver.constructUrlFromEndpointPortDatabase("localhost", null, null);
        assertEquals("jdbc:postgresql://localhost/", url);
    }

    @Test
    public void testGetDefaultDriverClass() {
        AWSSecretsManagerPostgreSQLDriver driver = new AWSSecretsManagerPostgreSQLDriver();
        assertEquals("org.postgresql.Driver", driver.getDefaultDriverClass());
    }

    @Test
    public void testConstructorNoArgs() {
        AWSSecretsManagerPostgreSQLDriver driver = new AWSSecretsManagerPostgreSQLDriver();
        assertNotNull(driver);
    }

    @Test
    public void testConstructorWithSecretCache() {
        SecretCache cache = mock(SecretCache.class);
        AWSSecretsManagerPostgreSQLDriver driver = new AWSSecretsManagerPostgreSQLDriver(cache);
        assertNotNull(driver);
    }

    @Test
    public void testConstructorWithAWSSecretsManagerClientBuilder() {
        AWSSecretsManagerClientBuilder builder = mock(AWSSecretsManagerClientBuilder.class);
        AWSSecretsManagerPostgreSQLDriver driver = new AWSSecretsManagerPostgreSQLDriver(builder);
        assertNotNull(driver);
    }

    @Test
    public void testConstructorWithAWSSecretsManagerClient() {
        AWSSecretsManager client = mock(AWSSecretsManager.class);
        AWSSecretsManagerPostgreSQLDriver driver = new AWSSecretsManagerPostgreSQLDriver(client);
        assertNotNull(driver);
    }

    @Test
    public void testConstructorWithSecretCacheConfiguration() {
        SecretCacheConfiguration cacheConfig = mock(SecretCacheConfiguration.class);
        AWSSecretsManagerPostgreSQLDriver driver = new AWSSecretsManagerPostgreSQLDriver(cacheConfig);
        assertNotNull(driver);
    }
}
