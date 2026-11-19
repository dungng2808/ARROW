package org.everit.json.schema;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.regex.Pattern;

import org.everit.json.schema.regexp.Regexp;
import org.junit.Test;

public class StringSchema_ArrowTest {

    @Test
    public void testDefaultConstructor() {
        StringSchema schema = new StringSchema();
        assertNotNull(schema);
        assertNull(schema.getPattern());
        assertNull(schema.getMinLength());
        assertNull(schema.getMaxLength());
        assertTrue(schema.requireString());
        assertEquals(FormatValidator.NONE, schema.getFormatValidator());
    }

    @Test
    public void testBuilderWithAllFields() {
        Regexp regexp = new org.everit.json.schema.regexp.JavaUtilRegexpFactory().createHandler("^[a-z]+$");
        StringSchema schema = StringSchema.builder()
                .minLength(3)
                .maxLength(10)
                .pattern(regexp)
                .requiresString(false)
                .formatValidator(FormatValidator.forFormat("date"))
                .build();

        assertEquals(Integer.valueOf(3), schema.getMinLength());
        assertEquals(Integer.valueOf(10), schema.getMaxLength());
        assertEquals(regexp, schema.getRegexpPattern());
        assertFalse(schema.requireString());
        assertEquals(FormatValidator.forFormat("date"), schema.getFormatValidator());
    }

    @Test
    public void testBuilderWithPatternString() {
        StringSchema schema = StringSchema.builder()
                .pattern("^[a-z]+$")
                .build();

        assertNotNull(schema.getPattern());
        assertNotNull(schema.getRegexpPattern());
        assertEquals(Pattern.compile("^[a-z]+$"), schema.getPattern());
    }

    @Test
    public void testGetPatternWhenNull() {
        StringSchema schema = new StringSchema();
        assertNull(schema.getPattern());
    }

    @Test
    public void testEqualsAndHashCode() {
        StringSchema schema1 = StringSchema.builder()
                .minLength(3)
                .maxLength(10)
                .pattern("^[a-z]+$")
                .requiresString(true)
                .formatValidator(FormatValidator.NONE)
                .build();

        StringSchema schema2 = StringSchema.builder()
                .minLength(3)
                .maxLength(10)
                .pattern("^[a-z]+$")
                .requiresString(true)
                .formatValidator(FormatValidator.NONE)
                .build();

        assertEquals(schema1, schema2);
        assertEquals(schema1.hashCode(), schema2.hashCode());

        StringSchema schema3 = StringSchema.builder()
                .minLength(5)
                .maxLength(10)
                .pattern("^[a-z]+$")
                .requiresString(true)
                .formatValidator(FormatValidator.NONE)
                .build();

        assertFalse(schema1.equals(schema3));
    }

    @Test
    public void testCanEqual() {
        StringSchema schema = new StringSchema();
        assertTrue(schema.canEqual(new StringSchema()));
        assertFalse(schema.canEqual(new Object()));
    }
}
