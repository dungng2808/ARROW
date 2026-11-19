package org.everit.json.schema;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import org.everit.json.schema.internal.JSONPrinter;
import org.everit.json.schema.regexp.JavaUtilRegexpFactory;
import org.everit.json.schema.regexp.Regexp;
import org.junit.Test;

public class StringSchema_ArrowTest {

    @Test
    public void testBuilderDefaults() {
        StringSchema schema = StringSchema.builder().build();
        assertNull(schema.getMaxLength());
        assertNull(schema.getMinLength());
        assertNull(schema.getRegexpPattern());
        assertTrue(schema.requireString());
        assertEquals(FormatValidator.NONE, schema.getFormatValidator());
    }

    @Test
    public void testBuilderWithAllFields() {
        Regexp pattern = new JavaUtilRegexpFactory().createHandler("^[a-z]+$");
        StringSchema schema = StringSchema.builder()
                .minLength(5)
                .maxLength(10)
                .pattern(pattern)
                .requiresString(false)
                .formatValidator(FormatValidator.forFormat("email"))
                .build();

        assertEquals(Integer.valueOf(5), schema.getMinLength());
        assertEquals(Integer.valueOf(10), schema.getMaxLength());
        assertEquals(pattern, schema.getRegexpPattern());
        assertFalse(schema.requireString());
        assertEquals(FormatValidator.forFormat("email"), schema.getFormatValidator());
    }

    @Test
    public void testGetPatternReturnsNullWhenPatternIsNull() {
        StringSchema schema = StringSchema.builder().build();
        assertNull(schema.getPattern());
    }

    @Test
    public void testGetPatternReturnsCompiledPattern() {
        Regexp pattern = new JavaUtilRegexpFactory().createHandler("^[a-z]+$");
        StringSchema schema = StringSchema.builder().pattern(pattern).build();
        assertEquals(java.util.regex.Pattern.compile("^[a-z]+$"), schema.getPattern());
    }

    @Test
    public void testAcceptCallsVisitor() {
        StringSchema schema = StringSchema.builder().build();
        TestVisitor visitor = new TestVisitor();
        schema.accept(visitor);
        assertTrue(visitor.visitedStringSchema);
    }

    @Test
    public void testEqualsAndHashCode() {
        StringSchema schema1 = StringSchema.builder().build();
        StringSchema schema2 = StringSchema.builder().build();
        assertEquals(schema1, schema2);
        assertEquals(schema1.hashCode(), schema2.hashCode());

        StringSchema schema3 = StringSchema.builder().minLength(5).build();
        assertFalse(schema1.equals(schema3));
        assertFalse(schema1.hashCode() == schema3.hashCode());
    }

    @Test
    public void testDescribePropertiesTo() {
        StringSchema schema = StringSchema.builder()
                .minLength(3)
                .maxLength(10)
                .pattern("^[a-z]+$")
                .formatValidator(FormatValidator.forFormat("email"))
                .build();

        JSONPrinter printer = new JSONPrinter();
        schema.describePropertiesTo(printer);
        String result = printer.toString();
        assertTrue(result.contains("\"minLength\":3"));
        assertTrue(result.contains("\"maxLength\":10"));
        assertTrue(result.contains("\"pattern\":\"^[a-z]+$\""));
        assertTrue(result.contains("\"format\":\"email\""));
    }

    @Test
    public void testDescribePropertiesToWithoutFormat() {
        StringSchema schema = StringSchema.builder()
                .minLength(3)
                .maxLength(10)
                .pattern("^[a-z]+$")
                .build();

        JSONPrinter printer = new JSONPrinter();
        schema.describePropertiesTo(printer);
        String result = printer.toString();
        assertTrue(result.contains("\"minLength\":3"));
        assertTrue(result.contains("\"maxLength\":10"));
        assertTrue(result.contains("\"pattern\":\"^[a-z]+$\""));
        assertFalse(result.contains("\"format\""));
    }

    @Test
    public void testDescribePropertiesToWithoutPattern() {
        StringSchema schema = StringSchema.builder()
                .minLength(3)
                .maxLength(10)
                .build();

        JSONPrinter printer = new JSONPrinter();
        schema.describePropertiesTo(printer);
        String result = printer.toString();
        assertTrue(result.contains("\"minLength\":3"));
        assertTrue(result.contains("\"maxLength\":10"));
        assertFalse(result.contains("\"pattern\""));
    }

    private static class TestVisitor extends Schema.Visitor {
        boolean visitedStringSchema = false;

        @Override
        public void visitStringSchema(StringSchema schema) {
            visitedStringSchema = true;
        }
    }
}
