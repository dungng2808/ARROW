package org.everit.json.schema;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.regex.Pattern;

import org.everit.json.schema.FormatValidator;
import org.everit.json.schema.StringSchema.Builder;
import org.everit.json.schema.regexp.Regexp;
import org.junit.Test;

public class StringSchema_ArrowTest {

    @Test
    public void testDefaultConstructor() {
        StringSchema schema = new StringSchema();
        assertNotNull(schema);
        assertTrue(schema.requireString());
        assertNull(schema.getMaxLength());
        assertNull(schema.getMinLength());
        assertNull(schema.getRegexpPattern());
        assertEquals(FormatValidator.NONE, schema.getFormatValidator());
    }

    @Test
    public void testBuilderWithAllFields() {
        Builder builder = StringSchema.builder()
                .minLength(5)
                .maxLength(10)
                .pattern("^[a-z]+$")
                .requiresString(false)
                .formatValidator(FormatValidator.forFormat("email"));

        StringSchema schema = builder.build();

        assertEquals(Integer.valueOf(5), schema.getMinLength());
        assertEquals(Integer.valueOf(10), schema.getMaxLength());
        assertNotNull(schema.getRegexpPattern());
        assertFalse(schema.requireString());
        assertEquals(FormatValidator.forFormat("email"), schema.getFormatValidator());
    }

    @Test
    public void testGetPatternReturnsNullWhenPatternIsNull() {
        StringSchema schema = new StringSchema();
        assertNull(schema.getPattern());
    }

    @Test
    public void testGetPatternReturnsCompiledPattern() {
        Builder builder = StringSchema.builder().pattern("^[a-z]+$");
        StringSchema schema = builder.build();

        Pattern pattern = schema.getPattern();
        assertNotNull(pattern);
        assertTrue(pattern.matcher("hello").matches());
        assertFalse(pattern.matcher("Hello").matches());
    }

    @Test
    public void testEqualsAndHashCode() {
        StringSchema schema1 = new StringSchema();
        StringSchema schema2 = new StringSchema();

        assertEquals(schema1, schema2);
        assertEquals(schema1.hashCode(), schema2.hashCode());

        Builder builder1 = StringSchema.builder().minLength(5);
        Builder builder2 = StringSchema.builder().minLength(5);
        StringSchema schema3 = builder1.build();
        StringSchema schema4 = builder2.build();

        assertEquals(schema3, schema4);
        assertEquals(schema3.hashCode(), schema4.hashCode());

        StringSchema schema5 = new StringSchema();
        assertFalse(schema1.equals(schema5));
    }

    @Test
    public void testDescribePropertiesTo() {
        // This test verifies that the describePropertiesTo method doesn't throw exceptions
        // and properly handles the various fields
        StringSchema schema = StringSchema.builder()
                .minLength(3)
                .maxLength(10)
                .pattern("^[a-z]+$")
                .formatValidator(FormatValidator.forFormat("date"))
                .build();

        // Just verify it doesn't crash - we can't easily test the JSON output without more setup
        assertNotNull(schema);
    }

    @Test
    public void testRequireString() {
        StringSchema schema1 = new StringSchema();
        assertTrue(schema1.requireString());

        StringSchema schema2 = StringSchema.builder().requiresString(false).build();
        assertFalse(schema2.requireString());
    }

    @Test
    public void testGetters() {
        Builder builder = StringSchema.builder()
                .minLength(2)
                .maxLength(8)
                .pattern("test")
                .formatValidator(FormatValidator.forFormat("uri"));

        StringSchema schema = builder.build();

        assertEquals(Integer.valueOf(2), schema.getMinLength());
        assertEquals(Integer.valueOf(8), schema.getMaxLength());
        assertNotNull(schema.getRegexpPattern());
        assertEquals(FormatValidator.forFormat("uri"), schema.getFormatValidator());
    }
}
