package org.abhijitsarkar.spring.beer.repository;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Test;
import rx.Single;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class BaseCouchbaseRepository_ArrowTest {
    private static class StringRepository extends BaseCouchbaseRepository<String> {
        StringRepository() {
            super();
        }
    }

    private static class IntegerRepository extends BaseCouchbaseRepository<Integer> {
        IntegerRepository() {
            super();
        }
    }

    @Test
    public void convertDeserializesJsonToGenericType() {
        StringRepository repository = new StringRepository();

        Single<String> result = repository.convert("\"beer\"");

        assertEquals("beer", result.toBlocking().value());
    }

    @Test
    public void convertDeserializesJsonNumberToGenericType() {
        IntegerRepository repository = new IntegerRepository();

        Single<Integer> result = repository.convert("42");

        assertEquals(Integer.valueOf(42), result.toBlocking().value());
    }

    @Test
    public void constructorAcceptsObjectMapper() {
        BaseCouchbaseRepository<String> repository =
                new BaseCouchbaseRepository<String>(new ObjectMapper());

        assertNotNull(repository);
    }

    @Test(expected = NullPointerException.class)
    public void constructorRejectsNullObjectMapper() {
        new BaseCouchbaseRepository<String>(null);
    }
}
