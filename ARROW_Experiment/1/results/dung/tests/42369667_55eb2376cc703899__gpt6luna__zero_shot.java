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
    public void defaultConstructorUsesDefaultValues() {
        StringSchema schema = new StringSchema();

        assertNull(schema.getMinLength());
        assertNull(schema.getMaxLength());
        assertNull(schema.getPattern());
        assertTrue(schema.requireString());
        assertSame(FormatValidator.NONE, schema.getFormatValidator());
    }

    @Test
    public void builderStoresLengthPatternAndStringRequirement() {
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
    public void builderAcceptsNullPattern() {
        StringSchema schema = StringSchema.builder().pattern((org.everit.json.schema.regexp.Regexp) null).build();

        assertNull(schema.getPattern());
    }

    @Test
    public void builderAcceptsNullLengths() {
        StringSchema schema = StringSchema.builder().minLength(null).maxLength(null).build();

        assertNull(schema.getMinLength());
        assertNull(schema.getMaxLength());
    }

    @Test
    public void formatValidatorCanBeSet() {
        FormatValidator validator = FormatValidator.forFormat("email");
        StringSchema schema = StringSchema.builder().formatValidator(validator).build();

        assertSame(validator, schema.getFormatValidator());
    }

    @Test(expected = NullPointerException.class)
    public void formatValidatorCannotBeNull() {
        StringSchema.builder().formatValidator(null);
    }

    @Test
    public void equalSchemasHaveEqualHashCodes() {
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
    public void schemasWithDifferentStringRequirementAreNotEqual() {
        StringSchema first = StringSchema.builder().requiresString(true).build();
        StringSchema second = StringSchema.builder().requiresString(false).build();

        assertNotEquals(first, second);
    }

    @Test
    public void schemaIsEqualToItselfAndNotEqualToOtherTypes() {
        StringSchema schema = new StringSchema();

        assertTrue(schema.equals(schema));
        assertFalse(schema.equals(null));
        assertFalse(schema.equals("string"));
    }
}
