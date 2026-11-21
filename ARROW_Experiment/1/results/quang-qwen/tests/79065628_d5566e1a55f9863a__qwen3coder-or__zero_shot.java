package org.abhijitsarkar.spring.beer.repository;

import com.couchbase.client.java.AsyncBucket;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.abhijitsarkar.spring.beer.CouchbaseProperties;
import org.abhijitsarkar.spring.beer.factory.CouchbaseAsyncBucketFactory;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import rx.Single;

import java.io.IOException;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertSame;
import static org.mockito.Mockito.when;

public class BaseCouchbaseRepository_ArrowTest {

    @Mock
    private CouchbaseAsyncBucketFactory couchbaseAsyncBucketFactory;

    @Mock
    private CouchbaseProperties couchbaseProperties;

    @Mock
    private AsyncBucket asyncBucket;

    private ObjectMapper objectMapper;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        objectMapper = new ObjectMapper();
    }

    @Test
    public void testConstructorWithObjectMapper() {
        BaseCouchbaseRepository<String> repository = new BaseCouchbaseRepository<>(objectMapper);

        assertNotNull(repository);
        assertSame(objectMapper, repository.objectMapper);
    }

    @Test(expected = NullPointerException.class)
    public void testConstructorWithNullObjectMapper() {
        new BaseCouchbaseRepository<String>(null);
    }

    @Test
    public void testAsyncBucket() {
        BaseCouchbaseRepository<String> repository = new BaseCouchbaseRepository<>(objectMapper);
        repository.couchbaseAsyncBucketFactory = couchbaseAsyncBucketFactory;

        when(couchbaseAsyncBucketFactory.getAsyncBucketInstance()).thenReturn(Single.just(asyncBucket));

        Single<AsyncBucket> result = repository.asyncBucket();

        assertNotNull(result);
    }

    @Test
    public void testConvertValidJson() throws IOException {
        BaseCouchbaseRepository<TestEntity> repository = new BaseCouchbaseRepository<>(objectMapper);
        String json = "{\"name\":\"test\"}";

        Single<TestEntity> result = repository.convert(json);

        assertNotNull(result);
    }

    @Test
    public void testConvertInvalidJson() {
        BaseCouchbaseRepository<TestEntity> repository = new BaseCouchbaseRepository<>(objectMapper);
        String invalidJson = "{invalid json}";

        Single<TestEntity> result = repository.convert(invalidJson);

        assertNotNull(result);
    }

    private static class TestEntity {
        private String name;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }
    }
}
