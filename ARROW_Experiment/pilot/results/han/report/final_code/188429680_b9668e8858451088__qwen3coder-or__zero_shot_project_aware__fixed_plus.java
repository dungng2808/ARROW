package il.ac.technion.nlp.nli.core;

import il.ac.technion.nlp.nli.core.reflection.NliMethodReflection;
import il.ac.technion.nlp.nli.core.state.NliEntity;
import org.apache.commons.lang3.StringUtils;
import org.junit.Test;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static org.junit.Assert.*;

public class NliDescriptionsUtils_ArrowTest {

    @Test
    public void testGenerateDescriptionsForMethod() throws NoSuchMethodException {
        Method method = TestClass.class.getMethod("moveTo");
        List<String> result = NliDescriptionsUtils.generateDescriptionsForMethod(method, false);
        assertEquals(1, result.size());
        assertEquals("move to", result.get(0));
    }

    @Test
    public void testGenerateDescriptionsForMethodWithAnnotation() throws NoSuchMethodException {
        Method method = TestClass.class.getMethod("moveTo");
        List<String> result = NliDescriptionsUtils.generateDescriptionsForMethod(method, true);
        assertEquals(2, result.size());
        assertEquals("move to", result.get(0));
        assertEquals("go to", result.get(1));
    }

    @Test
    public void testGenerateDescriptionsForRelationField() throws NoSuchFieldException {
        Field field = TestClass.class.getField("destination");
        List<String> result = NliDescriptionsUtils.generateDescriptionsForRelationField(field, false);
        assertEquals(1, result.size());
        assertEquals("destination", result.get(0));
    }

    @Test
    public void testGenerateDescriptionsForRelationFieldWithAnnotation() throws NoSuchFieldException {
        Field field = TestClass.class.getField("destination");
        List<String> result = NliDescriptionsUtils.generateDescriptionsForRelationField(field, true);
        assertEquals(2, result.size());
        assertEquals("destination", result.get(0));
        assertEquals("target", result.get(1));
    }

    @Test
    public void testGenerateDescriptionsForNliEntityType() {
        List<String> result = NliDescriptionsUtils.generateDescriptionsForNliEntityType(TestClass.class, false);
        assertEquals(1, result.size());
        assertEquals("test class", result.get(0));
    }

    @Test
    public void testGenerateDescriptionsForNliEntityTypeWithAnnotation() {
        List<String> result = NliDescriptionsUtils.generateDescriptionsForNliEntityType(TestClass.class, true);
        assertEquals(2, result.size());
        assertEquals("test class", result.get(0));
        assertEquals("sample class", result.get(1));
    }

    @Test
    public void testGenerateDescriptionsForEnumType() {
        List<String> result = NliDescriptionsUtils.generateDescriptionsForEnumType((Class<Enum<?>>) (Class) TestEnum.class, false);
        assertEquals(1, result.size());
        assertEquals("first value", result.get(0));
    }

    @Test
    public void testGenerateDescriptionsForEnumTypeWithAnnotation() {
        List<String> result = NliDescriptionsUtils.generateDescriptionsForEnumType((Class<Enum<?>>) (Class) TestEnum.class, true);
        assertEquals(3, result.size());
        assertEquals("first value", result.get(0));
        assertEquals("primary option", result.get(1));
        assertEquals("test enum", result.get(2));
    }

    @Test
    public void testGenerateDescriptionsForEnumValue() {
        List<String> result = NliDescriptionsUtils.generateDescriptionsForEnumValue(TestEnum.FIRST_VALUE);
        assertEquals(1, result.size());
        assertEquals("first value", result.get(0));
    }

    @Test
    public void testCreateDescriptionFromIdentifierName() {
        String result = StringUtils.join(StringUtils.splitByCharacterTypeCamelCase("moveTo"), ' ').toLowerCase();
        assertEquals("move to", result);
    }

    @Test
    public void testCreateDescriptionFromIdentifierNameWithUnderscore() {
        String result = StringUtils.join(StringUtils.splitByCharacterTypeCamelCase("move_to"), ' ').toLowerCase();
        assertEquals("move to", result);
    }

    @Test
    public void testCreateDescriptionFromIdentifierNameWithMultipleCamelCase() {
        String result = StringUtils.join(StringUtils.splitByCharacterTypeCamelCase("getNumberOfItems"), ' ').toLowerCase();
        assertEquals("get number of items", result);
    }

    @Test
    public void testGenerateDescriptionsFromAnnotation() {
        NliDescriptions annotation = new NliDescriptions() {
            @Override
            public String[] descriptions() {
                return new String[]{"Go To", "Move To"};
            }

            @Override
            public Class<? extends java.lang.annotation.Annotation> annotationType() {
                return NliDescriptions.class;
            }
        };
        List<String> result = Arrays.asList(annotation.descriptions())
                .stream()
                .map(String::toLowerCase)
                .collect(Collectors.toList());
        assertEquals(2, result.size());
        assertEquals("go to", result.get(0));
        assertEquals("move to", result.get(1));
    }

    // Helper class for testing
    public static class TestClass implements NliEntity {
        public String destination;
        
        @NliDescriptions(descriptions = {"go to", "move to"})
        public void moveTo() {}
    }

    // Helper enum for testing
    @NliDescriptions(descriptions = {"primary option", "first value"})
    public enum TestEnum {
        FIRST_VALUE
    }
}
