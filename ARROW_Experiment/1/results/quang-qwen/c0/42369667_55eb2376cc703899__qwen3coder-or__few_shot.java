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
        assertNull(schema.getMaxLength());
        assertNull(schema.getMinLength());
        assertNull(schema.getRegexpPattern());
        assertTrue(schema.requireString());
        assertEquals(FormatValidator.NONE, schema.getFormatValidator());
    }

    @Test
    public void testBuilderWithAllFields() {
        Builder builder = StringSchema.builder();
        builder.minLength(5);
        builder.maxLength(10);
        builder.pattern("^[a-z]+$");
        builder.requiresString(false);
        builder.formatValidator(FormatValidator.forFormat("email"));
        
        StringSchema schema = builder.build();
        
        assertEquals(Integer.valueOf(5), schema.getMinLength());
        assertEquals(Integer.valueOf(10), schema.getMaxLength());
        assertNotNull(schema.getRegexpPattern());
        assertFalse(schema.requireString());
        assertEquals(FormatValidator.forFormat("email"), schema.getFormatValidator());
    }

    @Test
    public void testGetPatternReturnsNullWhenNoPattern() {
        StringSchema schema = new StringSchema();
        assertNull(schema.getPattern());
    }

    @Test
    public void testGetPatternReturnsCompiledPattern() {
        Builder builder = StringSchema.builder();
        builder.pattern("^[0-9]{3}-[0-9]{2}-[0-9]{4}$");
        StringSchema schema = builder.build();
        
        Pattern pattern = schema.getPattern();
        assertNotNull(pattern);
        assertTrue(pattern.matcher("123-45-6789").matches());
        assertFalse(pattern.matcher("123456789").matches());
    }

    @Test
    public void testEqualsAndHashCode() {
        Builder builder1 = StringSchema.builder();
        builder1.minLength(5);
        builder1.maxLength(10);
        builder1.pattern("^[a-z]+$");
        builder1.requiresString(true);
        builder1.formatValidator(FormatValidator.NONE);
        
        Builder builder2 = StringSchema.builder();
        builder2.minLength(5);
        builder2.maxLength(10);
        builder2.pattern("^[a-z]+$");
        builder2.requiresString(true);
        builder2.formatValidator(FormatValidator.NONE);
        
        StringSchema schema1 = builder1.build();
        StringSchema schema2 = builder2.build();
        
        assertEquals(schema1, schema2);
        assertEquals(schema1.hashCode(), schema2.hashCode());
    }

    @Test
    public void testEqualsDifferentValues() {
        Builder builder1 = StringSchema.builder();
        builder1.minLength(5);
        builder1.maxLength(10);
        builder1.pattern("^[a-z]+$");
        builder1.requiresString(true);
        builder1.formatValidator(FormatValidator.NONE);
        
        Builder builder2 = StringSchema.builder();
        builder2.minLength(6);
        builder2.maxLength(10);
        builder2.pattern("^[a-z]+$");
        builder2.requiresString(true);
        builder2.formatValidator(FormatValidator.NONE);
        
        StringSchema schema1 = builder1.build();
        StringSchema schema2 = builder2.build();
        
        assertFalse(schema1.equals(schema2));
    }

    @Test
    public void testAcceptMethod() {
        StringSchema schema = new StringSchema();
        TestVisitor visitor = new TestVisitor();
        schema.accept(visitor);
        assertTrue(visitor.visitedStringSchema);
    }

    private static class TestVisitor extends Visitor {
        boolean visitedStringSchema = false;
        
        @Override
        public void visitStringSchema(StringSchema schema) {
            visitedStringSchema = true;
        }
    }
}
