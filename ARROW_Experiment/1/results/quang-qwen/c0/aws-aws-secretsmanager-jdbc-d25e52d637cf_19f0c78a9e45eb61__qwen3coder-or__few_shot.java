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
        // The static block should register the driver
        // We can't directly test registration, but we can verify the class loads correctly
        assertNotNull(AWSSecretsManagerPostgreSQLDriver.class);
    }

    @Test
    public void testDefaultConstructor() {
        AWSSecretsManagerPostgreSQLDriver driver = new AWSSecretsManagerPostgreSQLDriver();
        assertNotNull(driver);
    }

    @Test
    public void testConstructorWithSecretCache() {
        SecretCache cache = new SecretCache();
        AWSSecretsManagerPostgreSQLDriver driver = new AWSSecretsManagerPostgreSQLDriver(cache);
        assertNotNull(driver);
    }

    @Test
    public void testConstructorWithClientBuilder() {
        AWSSecretsManagerClientBuilder builder = AWSSecretsManagerClientBuilder.defaultClient();
        AWSSecretsManagerPostgreSQLDriver driver = new AWSSecretsManagerPostgreSQLDriver(builder);
        assertNotNull(driver);
    }

    @Test
    public void testConstructorWithClient() {
        AWSSecretsManager client = AWSSecretsManagerClientBuilder.defaultClient();
        AWSSecretsManagerPostgreSQLDriver driver = new AWSSecretsManagerPostgreSQLDriver(client);
        assertNotNull(driver);
    }

    @Test
    public void testConstructorWithCacheConfiguration() {
        SecretCacheConfiguration cacheConfig = new SecretCacheConfiguration();
        AWSSecretsManagerPostgreSQLDriver driver = new AWSSecretsManagerPostgreSQLDriver(cacheConfig);
        assertNotNull(driver);
    }

    @Test
    public void testGetPropertySubprefix() {
        AWSSecretsManagerPostgreSQLDriver driver = new AWSSecretsManagerPostgreSQLDriver();
        assertEquals("postgresql", driver.getPropertySubprefix());
    }

    @Test
    public void testIsExceptionDueToAuthenticationErrorWithSQLException() {
        AWSSecretsManagerPostgreSQLDriver driver = new AWSSecretsManagerPostgreSQLDriver();
        
        // Test with access denied error code
        SQLException accessDeniedException = new SQLException("", "28P01");
        assertTrue(driver.isExceptionDueToAuthenticationError(accessDeniedException));
        
        // Test with different error code
        SQLException otherException = new SQLException("", "28P02");
        assertFalse(driver.isExceptionDueToAuthenticationError(otherException));
        
        // Test with null SQL state
        SQLException nullStateException = new SQLException("", "");
        assertFalse(driver.isExceptionDueToAuthenticationError(nullStateException));
    }

    @Test
    public void testIsExceptionDueToAuthenticationErrorWithNonSQLException() {
        AWSSecretsManagerPostgreSQLDriver driver = new AWSSecretsManagerPostgreSQLDriver();
        
        // Test with non-SQLException
        RuntimeException runtimeException = new RuntimeException();
        assertFalse(driver.isExceptionDueToAuthenticationError(runtimeException));
    }

    @Test
    public void testConstructUrlFromEndpointPortDatabase() {
        AWSSecretsManagerPostgreSQLDriver driver = new AWSSecretsManagerPostgreSQLDriver();
        
        // Test with all parameters
        String url = driver.constructUrlFromEndpointPortDatabase("localhost", "5432", "mydb");
        assertEquals("jdbc:postgresql://localhost:5432/mydb", url);
        
        // Test with no port
        url = driver.constructUrlFromEndpointPortDatabase("localhost", null, "mydb");
        assertEquals("jdbc:postgresql://localhost/mydb", url);
        
        // Test with no database
        url = driver.constructUrlFromEndpointPortDatabase("localhost", "5432", null);
        assertEquals("jdbc:postgresql://localhost:5432/", url);
        
        // Test with no port and no database
        url = driver.constructUrlFromEndpointPortDatabase("localhost", null, null);
        assertEquals("jdbc:postgresql://localhost/", url);
    }

    @Test
    public void testGetDefaultDriverClass() {
        AWSSecretsManagerPostgreSQLDriver driver = new AWSSecretsManagerPostgreSQLDriver();
        assertEquals("org.postgresql.Driver", driver.getDefaultDriverClass());
    }
}
