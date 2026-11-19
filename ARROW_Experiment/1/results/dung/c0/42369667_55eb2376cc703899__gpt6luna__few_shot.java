package org.everit.json.schema;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class StringSchema_ArrowTest {

    @Test
    public void defaultSchemaHasNoLengthOrPatternAndRequiresString() {
        StringSchema schema = new StringSchema();

        assertNull(schema.getMinLength());
        assertNull(schema.getMaxLength());
        assertNull(schema.getPattern());
        assertTrue(schema.requireString());
        assertSame(FormatValidator.NONE, schema.getFormatValidator());
    }

    @Test
    public void builderSetsLengthPatternAndRequiresString() {
        StringSchema schema = StringSchema.builder()
                .minLength(2)
                .maxLength(5)
                .pattern("a+")
                .requiresString(false)
                .build();

        assertEquals(Integer.valueOf(2), schema.getMinLength());
        assertEquals(Integer.valueOf(5), schema.getMaxLength());
        assertEquals(java.util.regex.Pattern.compile("a+"), schema.getPattern());
        assertFalse(schema.requireString());
    }

    @Test
    public void builderCanClearLengthAndPatternValues() {
        StringSchema schema = StringSchema.builder()
                .minLength(2)
                .maxLength(5)
                .pattern("a+")
                .minLength(null)
                .maxLength(null)
                .pattern((org.everit.json.schema.regexp.Regexp) null)
                .build();

        assertNull(schema.getMinLength());
        assertNull(schema.getMaxLength());
        assertNull(schema.getPattern());
    }

    @Test
    public void equalSchemasHaveEqualHashCodes() {
        StringSchema first = StringSchema.builder()
                .minLength(1)
                .maxLength(4)
                .pattern("x.*")
                .build();
        StringSchema second = StringSchema.builder()
                .minLength(1)
                .maxLength(4)
                .pattern("x.*")
                .build();

        assertEquals(first, second);
        assertEquals(first.hashCode(), second.hashCode());
    }

    @Test
    public void schemasWithDifferentCriteriaAreNotEqual() {
        StringSchema first = StringSchema.builder().minLength(1).build();
        StringSchema second = StringSchema.builder().minLength(2).build();

        assertNotEquals(first, second);
        assertFalse(first.equals(null));
    }

    @Test(expected = NullPointerException.class)
    public void formatValidatorCannotBeNull() {
        StringSchema.builder().formatValidator(null);
    }
}
