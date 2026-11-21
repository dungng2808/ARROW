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

    private static class InvalidJsonRepository extends BaseCouchbaseRepository<SimpleValue> {
        InvalidJsonRepository() {
            super();
        }
    }

    public static class SimpleValue {
        public String name;

        public SimpleValue() {
        }
    }

    @Test
    public void convertReadsJsonAsGenericType() {
        StringRepository repository = new StringRepository();

        Single<String> result = repository.convert("\"beer\"");

        assertEquals("beer", result.toBlocking().value());
    }

    @Test
    public void convertReturnsErrorForInvalidJson() {
        InvalidJsonRepository repository = new InvalidJsonRepository();

        Single<SimpleValue> result = repository.convert("{");

        try {
            result.toBlocking().value();
            fail("Expected invalid JSON to produce an error");
        } catch (RuntimeException exception) {
            assertNotNull(exception.getCause());
        }
    }

    @Test(expected = NullPointerException.class)
    public void constructorRejectsNullObjectMapper() {
        new BaseCouchbaseRepository<Object>((ObjectMapper) null);
    }
}
