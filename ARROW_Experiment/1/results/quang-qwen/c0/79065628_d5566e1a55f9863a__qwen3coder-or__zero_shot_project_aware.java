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

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.*;

public class BaseCouchbaseRepository_ArrowTest {

    @Mock
    private ObjectMapper objectMapper;

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
        repository = new BaseCouchbaseRepository<TestEntity>(objectMapper);
        repository.couchbaseAsyncBucketFactory = couchbaseAsyncBucketFactory;
        repository.couchbaseProperties = couchbaseProperties;
    }

    @Test
    public void shouldCreateDefaultConstructor() {
        BaseCouchbaseRepository<TestEntity> repo = new BaseCouchbaseRepository<TestEntity>();
        assertThat(repo, notNullValue());
    }

    @Test(expected = NullPointerException.class)
    public void shouldThrowExceptionForNullObjectMapperInConstructor() {
        new BaseCouchbaseRepository<TestEntity>(null);
    }

    @Test
    public void shouldReturnAsyncBucketFromFactory() {
        when(couchbaseAsyncBucketFactory.getAsyncBucketInstance()).thenReturn(Single.just(asyncBucket));

        Single<AsyncBucket> result = repository.asyncBucket();

        assertThat(result, notNullValue());
        assertThat(result.toBlocking().value(), is(asyncBucket));
    }

    @Test
    public void shouldConvertJsonToObject() throws Exception {
        String json = "{\"name\":\"test\"}";
        TestEntity entity = new TestEntity("test");

        when(objectMapper.readValue(json, TestEntity.class)).thenReturn(entity);

        Single<TestEntity> result = repository.convert(json);

        assertThat(result, notNullValue());
        assertThat(result.toBlocking().value(), is(entity));
    }

    @Test
    public void shouldReturnErrorWhenConversionFails() throws Exception {
        String json = "{\"name\":\"test\"}";

        when(objectMapper.readValue(json, TestEntity.class)).thenThrow(new RuntimeException("Parse error"));

        Single<TestEntity> result = repository.convert(json);

        assertThat(result, notNullValue());
        assertThat(result.toBlocking().getThrowable(), instanceOf(RuntimeException.class));
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
