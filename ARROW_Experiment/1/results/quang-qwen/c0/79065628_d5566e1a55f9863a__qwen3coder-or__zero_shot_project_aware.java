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

import static org.assertj.core.api.Assertions.assertThat;
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
    public void shouldCreateRepositoryWithObjectMapper() {
        ObjectMapper mapper = new ObjectMapper();
        BaseCouchbaseRepository<TestEntity> repo = new BaseCouchbaseRepository<TestEntity>(mapper);
        
        assertThat(repo).isNotNull();
    }
    
    @Test(expected = NullPointerException.class)
    public void shouldThrowExceptionWhenObjectMapperIsNull() {
        new BaseCouchbaseRepository<TestEntity>(null);
    }
    
    @Test
    public void shouldGetAsyncBucket() {
        when(couchbaseAsyncBucketFactory.getAsyncBucketInstance()).thenReturn(Single.just(asyncBucket));
        
        Single<AsyncBucket> result = repository.asyncBucket();
        
        assertThat(result).isNotNull();
        assertThat(result.toBlocking().value()).isEqualTo(asyncBucket);
    }
    
    @Test
    public void shouldConvertJsonToObject() throws Exception {
        String json = "{\"name\":\"test\",\"id\":1}";
        TestEntity expected = new TestEntity("test", 1);
        
        Single<TestEntity> result = repository.convert(json);
        
        assertThat(result).isNotNull();
        assertThat(result.toBlocking().value()).isEqualTo(expected);
    }
    
    @Test
    public void shouldReturnErrorWhenConversionFails() {
        String invalidJson = "{invalid json}";
        
        Single<TestEntity> result = repository.convert(invalidJson);
        
        assertThat(result).isNotNull();
        assertThat(result.toBlocking().getThrowable()).isNotNull();
    }
    
    private static class TestEntity {
        private String name;
        private int id;
        
        public TestEntity() {}
        
        public TestEntity(String name, int id) {
            this.name = name;
            this.id = id;
        }
        
        public String getName() {
            return name;
        }
        
        public void setName(String name) {
            this.name = name;
        }
        
        public int getId() {
            return id;
        }
        
        public void setId(int id) {
            this.id = id;
        }
        
        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            
            TestEntity that = (TestEntity) o;
            
            if (id != that.id) return false;
            return name != null ? name.equals(that.name) : that.name == null;
        }
        
        @Override
        public int hashCode() {
            int result = name != null ? name.hashCode() : 0;
            result = 31 * result + id;
            return result;
        }
    }
}
