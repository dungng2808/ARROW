package org.abhijitsarkar.spring.beer.repository;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Test;
import rx.Single;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

public class BaseCouchbaseRepository_ArrowTest {
    private static class StringRepository extends BaseCouchbaseRepository<String> {
        StringRepository() {
            super();
        }
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

    @Test
    public void convertReturnsDeserializedString() {
        StringRepository repository = new StringRepository();

        Single<String> result = repository.convert("\"beer\"");

        assertEquals("beer", result.toBlocking().value());
    }

    @Test
    public void convertReturnsErrorForInvalidJson() {
        StringRepository repository = new StringRepository();

        Single<String> result = repository.convert("{");

        try {
            result.toBlocking().value();
            fail("Expected JSON parsing to fail.");
        } catch (RuntimeException exception) {
            assertNotNull(exception.getCause());
        }
    }
}
