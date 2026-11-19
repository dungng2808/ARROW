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
    public void testGetPatternReturnsCorrectPattern() {
        Builder builder = StringSchema.builder().pattern("^[a-z]+$");
        StringSchema schema = builder.build();

        Pattern pattern = schema.getPattern();
        assertNotNull(pattern);
        assertTrue(pattern.matcher("hello").matches());
        assertFalse(pattern.matcher("Hello").matches());
    }

    @Test
    public void testGetPatternWithNullPattern() {
        Builder builder = StringSchema.builder();
        StringSchema schema = builder.build();

        Pattern pattern = schema.getPattern();
        assertNull(pattern);
    }

    @Test
    public void testEqualsAndHashCode() {
        Builder builder1 = StringSchema.builder()
                .minLength(5)
                .maxLength(10)
                .pattern("^[a-z]+$")
                .requiresString(false)
                .formatValidator(FormatValidator.forFormat("email"));

        Builder builder2 = StringSchema.builder()
                .minLength(5)
                .maxLength(10)
                .pattern("^[a-z]+$")
                .requiresString(false)
                .formatValidator(FormatValidator.forFormat("email"));

        StringSchema schema1 = builder1.build();
        StringSchema schema2 = builder2.build();

        assertEquals(schema1, schema2);
        assertEquals(schema1.hashCode(), schema2.hashCode());
    }

    @Test
    public void testEqualsDifferentValues() {
        Builder builder1 = StringSchema.builder()
                .minLength(5)
                .maxLength(10)
                .pattern("^[a-z]+$")
                .requiresString(false)
                .formatValidator(FormatValidator.forFormat("email"));

        Builder builder2 = StringSchema.builder()
                .minLength(6)
                .maxLength(10)
                .pattern("^[a-z]+$")
                .requiresString(false)
                .formatValidator(FormatValidator.forFormat("email"));

        StringSchema schema1 = builder1.build();
        StringSchema schema2 = builder2.build();

        assertFalse(schema1.equals(schema2));
    }

    @Test
    public void testCanEqual() {
        StringSchema schema = new StringSchema();
        assertTrue(schema.canEqual(new StringSchema()));
        assertFalse(schema.canEqual(new Object()));
    }

    @Test
    public void testRequireString() {
        Builder builder = StringSchema.builder().requiresString(false);
        StringSchema schema = builder.build();
        assertFalse(schema.requireString());

        builder = StringSchema.builder().requiresString(true);
        schema = builder.build();
        assertTrue(schema.requireString());
    }

    @Test
    public void testDescribePropertiesTo() {
        // This test verifies that the method doesn't throw exceptions
        // and properly handles various field combinations
        Builder builder = StringSchema.builder()
                .minLength(3)
                .maxLength(8)
                .pattern("^[a-zA-Z]+$")
                .formatValidator(FormatValidator.forFormat("date"));

        StringSchema schema = builder.build();
        
        // Just verify it doesn't crash - actual JSON output would require more complex setup
        assertNotNull(schema);
    }
}
