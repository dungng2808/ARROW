package org.everit.json.schema;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class StringSchema_ArrowTest {

    @Test
    public void defaultSchemaHasNoLengthOrPatternConstraints() {
        StringSchema schema = new StringSchema();

        assertNull(schema.getMinLength());
        assertNull(schema.getMaxLength());
        assertNull(schema.getPattern());
        assertTrue(schema.requireString());
        assertEquals(FormatValidator.NONE, schema.getFormatValidator());
    }

    @Test
    public void builderStoresLengthConstraints() {
        StringSchema schema = StringSchema.builder()
                .minLength(2)
                .maxLength(5)
                .build();

        assertEquals(Integer.valueOf(2), schema.getMinLength());
        assertEquals(Integer.valueOf(5), schema.getMaxLength());
    }

    @Test
    public void builderStoresPattern() {
        StringSchema schema = StringSchema.builder()
                .pattern("a+b")
                .build();

        assertEquals("a+b", schema.getPattern().pattern());
    }

    @Test
    public void builderCanDisableStringRequirement() {
        StringSchema schema = StringSchema.builder()
                .requiresString(false)
                .build();

        assertFalse(schema.requireString());
    }

    @Test
    public void schemasWithSamePropertiesAreEqual() {
        StringSchema first = StringSchema.builder()
                .minLength(1)
                .maxLength(4)
                .pattern("x")
                .build();
        StringSchema second = StringSchema.builder()
                .minLength(1)
                .maxLength(4)
                .pattern("x")
                .build();

        assertEquals(first, second);
        assertEquals(first.hashCode(), second.hashCode());
    }

    @Test
    public void schemasWithDifferentLengthConstraintsAreNotEqual() {
        StringSchema first = StringSchema.builder().minLength(1).build();
        StringSchema second = StringSchema.builder().minLength(2).build();

        assertNotEquals(first, second);
    }
}
