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
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
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
    public void testConvertSuccess() throws ExecutionException, InterruptedException {
        String json = "{\"id\":\"testId\",\"name\":\"testName\"}";
        TestEntity expected = new TestEntity("testId", "testName");

        Single<TestEntity> result = repository.convert(json);

        TestEntity actual = result.toBlocking().toFuture().get();
        assertNotNull(actual);
        assertEquals(expected.getId(), actual.getId());
        assertEquals(expected.getName(), actual.getName());
    }

    @Test
    public void testConvertFailure() {
        String invalidJson = "{invalid json}";

        Single<TestEntity> result = repository.convert(invalidJson);

        try {
            result.toBlocking().toFuture().get();
        } catch (ExecutionException e) {
            assertNotNull(e.getCause());
            assertEquals(IOException.class, e.getCause().getClass());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    @Test
    public void testAsyncBucket() {
        when(couchbaseAsyncBucketFactory.getAsyncBucketInstance()).thenReturn(Single.just(asyncBucket));

        Single<AsyncBucket> result = repository.asyncBucket();

        assertEquals(asyncBucket, result.toBlocking().toFuture().get());
    }

    @Test(expected = NullPointerException.class)
    public void testConstructorWithNullObjectMapper() {
        new BaseCouchbaseRepository<TestEntity>(null);
    }

    private static class TestEntity {
        private String id;
        private String name;

        public TestEntity() {
        }

        public TestEntity(String id, String name) {
            this.id = id;
            this.name = name;
        }

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }
    }
}
