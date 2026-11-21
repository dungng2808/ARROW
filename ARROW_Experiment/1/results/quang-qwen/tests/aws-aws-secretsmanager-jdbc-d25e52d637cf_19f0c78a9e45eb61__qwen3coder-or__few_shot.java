package com.amazonaws.secretsmanager.sql;

import com.amazonaws.secretsmanager.caching.SecretCache;
import com.amazonaws.secretsmanager.caching.SecretCacheConfiguration;
import com.amazonaws.services.secretsmanager.AWSSecretsManager;
import com.amazonaws.services.secretsmanager.AWSSecretsManagerClientBuilder;
import org.junit.Test;

import java.sql.SQLException;

import static org.junit.Assert.*;

public class AWSSecretsManagerPostgreSQLDriver_ArrowTest {

    @Test
    public void testStaticInitialization() {
        // Testing that the driver is registered in static block
        assertNotNull(AWSSecretsManagerDriver.getRegisteredDriver("postgresql"));
    }

    @Test
    public void testDefaultConstructor() {
        AWSSecretsManagerPostgreSQLDriver driver = new AWSSecretsManagerPostgreSQLDriver();
        assertEquals("postgresql", driver.getPropertySubprefix());
    }

    @Test
    public void testConstructorWithSecretCache() {
        SecretCache cache = new SecretCache();
        AWSSecretsManagerPostgreSQLDriver driver = new AWSSecretsManagerPostgreSQLDriver(cache);
        assertEquals("postgresql", driver.getPropertySubprefix());
    }

    @Test
    public void testConstructorWithClientBuilder() {
        AWSSecretsManagerClientBuilder builder = AWSSecretsManagerClientBuilder.standard();
        AWSSecretsManagerPostgreSQLDriver driver = new AWSSecretsManagerPostgreSQLDriver(builder);
        assertEquals("postgresql", driver.getPropertySubprefix());
    }

    @Test
    public void testConstructorWithClient() {
        AWSSecretsManager client = AWSSecretsManagerClientBuilder.standard().build();
        AWSSecretsManagerPostgreSQLDriver driver = new AWSSecretsManagerPostgreSQLDriver(client);
        assertEquals("postgresql", driver.getPropertySubprefix());
    }

    @Test
    public void testConstructorWithCacheConfiguration() {
        SecretCacheConfiguration cacheConfig = new SecretCacheConfiguration();
        AWSSecretsManagerPostgreSQLDriver driver = new AWSSecretsManagerPostgreSQLDriver(cacheConfig);
        assertEquals("postgresql", driver.getPropertySubprefix());
    }

    @Test
    public void testGetPropertySubprefix() {
        AWSSecretsManagerPostgreSQLDriver driver = new AWSSecretsManagerPostgreSQLDriver();
        assertEquals("postgresql", driver.getPropertySubprefix());
    }

    @Test
    public void testIsExceptionDueToAuthenticationErrorWithCorrectSqlState() {
        AWSSecretsManagerPostgreSQLDriver driver = new AWSSecretsManagerPostgreSQLDriver();
        SQLException sqlException = new SQLException("", "28P01");
        assertTrue(driver.isExceptionDueToAuthenticationError(sqlException));
    }

    @Test
    public void testIsExceptionDueToAuthenticationErrorWithWrongSqlState() {
        AWSSecretsManagerPostgreSQLDriver driver = new AWSSecretsManagerPostgreSQLDriver();
        SQLException sqlException = new SQLException("", "28P02");
        assertFalse(driver.isExceptionDueToAuthenticationError(sqlException));
    }

    @Test
    public void testIsExceptionDueToAuthenticationErrorWithNonSqlException() {
        AWSSecretsManagerPostgreSQLDriver driver = new AWSSecretsManagerPostgreSQLDriver();
        RuntimeException runtimeException = new RuntimeException();
        assertFalse(driver.isExceptionDueToAuthenticationError(runtimeException));
    }

    @Test
    public void testConstructUrlFromEndpointPortDatabase() {
        AWSSecretsManagerPostgreSQLDriver driver = new AWSSecretsManagerPostgreSQLDriver();
        String url = driver.constructUrlFromEndpointPortDatabase("localhost", "5432", "testdb");
        assertEquals("jdbc:postgresql://localhost:5432/testdb", url);
    }

    @Test
    public void testConstructUrlFromEndpointPortDatabaseWithoutPort() {
        AWSSecretsManagerPostgreSQLDriver driver = new AWSSecretsManagerPostgreSQLDriver();
        String url = driver.constructUrlFromEndpointPortDatabase("localhost", null, "testdb");
        assertEquals("jdbc:postgresql://localhost/testdb", url);
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
}
