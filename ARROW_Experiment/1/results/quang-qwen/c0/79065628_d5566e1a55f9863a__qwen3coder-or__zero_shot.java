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

    private BaseCouchbaseRepository<TestEntity> repository;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        repository = new BaseCouchbaseRepository<TestEntity>(new ObjectMapper()) {
            @Override
            public Single<AsyncBucket> asyncBucket() {
                return couchbaseAsyncBucketFactory.getAsyncBucketInstance();
            }
        };
    }

    @Test
    public void testConvertSuccess() throws Exception {
        String json = "{\"name\":\"test\"}";
        TestEntity entity = new TestEntity("test");

        Single<TestEntity> result = repository.convert(json);

        assertNotNull(result);
    }

    @Test
    public void testConvertFailure() throws Exception {
        String invalidJson = "{invalid json}";

        Single<TestEntity> result = repository.convert(invalidJson);

        assertNotNull(result);
    }

    @Test
    public void testAsyncBucket() throws Exception {
        when(couchbaseAsyncBucketFactory.getAsyncBucketInstance()).thenReturn(Single.just(asyncBucket));

        Single<AsyncBucket> result = repository.asyncBucket();

        assertSame(Single.just(asyncBucket), result);
    }

    @Test(expected = NullPointerException.class)
    public void testConstructorWithNullObjectMapper() {
        new BaseCouchbaseRepository<TestEntity>(null);
    }

    private static class TestEntity {
        private String name;

        public TestEntity() {
        }

        public TestEntity(String name) {
            this.name = name;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }
    }
}
