package com.amazonaws.secretsmanager.sql;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

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
    public void testIsExceptionDueToAuthenticationErrorWithNullSqlState() {
        AWSSecretsManagerPostgreSQLDriver driver = new AWSSecretsManagerPostgreSQLDriver();
        SQLException sqlException = new SQLException("");
        assertFalse(driver.isExceptionDueToAuthenticationError(sqlException));
    }

    @Test
    public void testIsExceptionDueToAuthenticationErrorWithNonSQLException() {
        AWSSecretsManagerPostgreSQLDriver driver = new AWSSecretsManagerPostgreSQLDriver();
        RuntimeException runtimeException = new RuntimeException();
        assertFalse(driver.isExceptionDueToAuthenticationError(runtimeException));
    }

    @Test
    public void testConstructUrlFromEndpointPortDatabaseWithAllParameters() {
        AWSSecretsManagerPostgreSQLDriver driver = new AWSSecretsManagerPostgreSQLDriver();
        String url = driver.constructUrlFromEndpointPortDatabase("localhost", "5432", "mydb");
        assertEquals("jdbc:postgresql://localhost:5432/mydb", url);
    }

    @Test
    public void testConstructUrlFromEndpointPortDatabaseWithNoPort() {
        AWSSecretsManagerPostgreSQLDriver driver = new AWSSecretsManagerPostgreSQLDriver();
        String url = driver.constructUrlFromEndpointPortDatabase("localhost", null, "mydb");
        assertEquals("jdbc:postgresql://localhost/mydb", url);
    }

    @Test
    public void testConstructUrlFromEndpointPortDatabaseWithNoDbName() {
        AWSSecretsManagerPostgreSQLDriver driver = new AWSSecretsManagerPostgreSQLDriver();
        String url = driver.constructUrlFromEndpointPortDatabase("localhost", "5432", null);
        assertEquals("jdbc:postgresql://localhost:5432/", url);
    }

    @Test
    public void testConstructUrlFromEndpointPortDatabaseWithNoPortAndNoDbName() {
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
    public void testConstructorWithSecretCache() {
        SecretCache cache = new SecretCache();
        AWSSecretsManagerPostgreSQLDriver driver = new AWSSecretsManagerPostgreSQLDriver(cache);
        assertEquals("postgresql", driver.getPropertySubprefix());
    }

    @Test
    public void testConstructorWithAWSSecretsManagerClientBuilder() {
        AWSSecretsManagerClientBuilder builder = AWSSecretsManagerClientBuilder.defaultClient();
        AWSSecretsManagerPostgreSQLDriver driver = new AWSSecretsManagerPostgreSQLDriver(builder);
        assertEquals("postgresql", driver.getPropertySubprefix());
    }

    @Test
    public void testConstructorWithAWSSecretsManagerClient() {
        AWSSecretsManager client = AWSSecretsManagerClientBuilder.defaultClient();
        AWSSecretsManagerPostgreSQLDriver driver = new AWSSecretsManagerPostgreSQLDriver(client);
        assertEquals("postgresql", driver.getPropertySubprefix());
    }

    @Test
    public void testConstructorWithSecretCacheConfiguration() {
        SecretCacheConfiguration config = new SecretCacheConfiguration();
        AWSSecretsManagerPostgreSQLDriver driver = new AWSSecretsManagerPostgreSQLDriver(config);
        assertEquals("postgresql", driver.getPropertySubprefix());
    }
}
