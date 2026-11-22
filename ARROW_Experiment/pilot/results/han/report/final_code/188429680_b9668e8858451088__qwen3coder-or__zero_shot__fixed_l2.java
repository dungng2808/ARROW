package il.ac.technion.nlp.nli.core;

import il.ac.technion.nlp.nli.core.reflection.NliMethodReflection;
import il.ac.technion.nlp.nli.core.state.NliEntity;
import org.junit.Test;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.List;

import static org.junit.Assert.*;

public class NliDescriptionsUtils_ArrowTest {

    @Test
    public void testGenerateDescriptionsForMethod() throws NoSuchMethodException {
        Method method = TestClass.class.getMethod("testMethod");
        List<String> result = NliDescriptionsUtils.generateDescriptionsForMethod(method, false);
        assertEquals(1, result.size());
        assertEquals("test method", result.get(0));
    }

    @Test
    public void testGenerateDescriptionsForMethodWithAnnotation() throws NoSuchMethodException {
        Method method = TestClass.class.getMethod("annotatedMethod");
        List<String> result = NliDescriptionsUtils.generateDescriptionsForMethod(method, true);
        assertEquals(2, result.size());
        assertEquals("test method", result.get(0));
        assertEquals("annotated description", result.get(1));
    }

    @Test
    public void testGenerateDescriptionsForRelationField() throws NoSuchFieldException {
        Field field = TestClass.class.getField("testField");
        List<String> result = NliDescriptionsUtils.generateDescriptionsForRelationField(field, false);
        assertEquals(1, result.size());
        assertEquals("test field", result.get(0));
    }

    @Test
    public void testGenerateDescriptionsForRelationFieldWithAnnotation() throws NoSuchFieldException {
        Field field = TestClass.class.getField("annotatedField");
        List<String> result = NliDescriptionsUtils.generateDescriptionsForRelationField(field, true);
        assertEquals(2, result.size());
        assertEquals("test field", result.get(0));
        assertEquals("field annotated description", result.get(1));
    }

    @Test
    public void testGenerateDescriptionsForNliEntityType() throws ClassNotFoundException {
        Class<? extends NliEntity> entityType = TestEntity.class;
        List<String> result = NliDescriptionsUtils.generateDescriptionsForNliEntityType(entityType, false);
        assertEquals(1, result.size());
        assertEquals("test entity", result.get(0));
    }

    @Test
    public void testGenerateDescriptionsForNliEntityTypeWithAnnotation() throws ClassNotFoundException {
        Class<? extends NliEntity> entityType = AnnotatedEntity.class;
        List<String> result = NliDescriptionsUtils.generateDescriptionsForNliEntityType(entityType, true);
        assertEquals(2, result.size());
        assertEquals("test entity", result.get(0));
        assertEquals("entity annotated description", result.get(1));
    }

    @Test
    public void testGenerateDescriptionsForEnumType() throws ClassNotFoundException {
        Class<Enum<?>> enumType = TestEnum.class;
        List<String> result = NliDescriptionsUtils.generateDescriptionsForEnumType(enumType, false);
        assertEquals(1, result.size());
        assertEquals("test enum", result.get(0));
    }

    @Test
    public void testGenerateDescriptionsForEnumTypeWithAnnotation() throws ClassNotFoundException {
        Class<Enum<?>> enumType = AnnotatedEnum.class;
        List<String> result = NliDescriptionsUtils.generateDescriptionsForEnumType(enumType, true);
        assertEquals(2, result.size());
        assertEquals("test enum", result.get(0));
        assertEquals("enum annotated description", result.get(1));
    }

    @Test
    public void testGenerateDescriptionsForEnumValue() {
        TestEnum enumValue = TestEnum.TEST_ENUM_VALUE;
        List<String> result = NliDescriptionsUtils.generateDescriptionsForEnumValue(enumValue);
        assertEquals(1, result.size());
        assertEquals("test enum value", result.get(0));
    }

    @Test
    public void testCreateDescriptionFromIdentifierName() throws NoSuchMethodException {
        String result = NliDescriptionsUtils.generateDescriptionsForMethod(TestClass.class.getMethod("testMethod"), false).get(0);
        assertEquals("test method", result);
    }

    @Test
    public void testCreateDescriptionFromIdentifierNameWithUnderscores() throws NoSuchFieldException {
        String result = NliDescriptionsUtils.generateDescriptionsForRelationField(TestClass.class.getField("testField"), false).get(0);
        assertEquals("test field", result);
    }

    @Test
    public void testGenerateDescriptionsFromAnnotation() throws NoSuchMethodException {
        Method method = TestClass.class.getMethod("annotatedMethod");
        List<String> result = NliDescriptionsUtils.generateDescriptionsForMethod(method, true);
        assertEquals(2, result.size());
        assertEquals("test method", result.get(0));
        assertEquals("annotated description", result.get(1));
    }

    // Helper classes for testing
    public static class TestClass {
        public int testField;
        @NliDescriptions(descriptions = {"field annotated description"})
        public int annotatedField;

        public void testMethod() {}
        @NliDescriptions(descriptions = {"annotated description"})
        public void annotatedMethod() {}
    }

    @NliDescriptions(descriptions = {"entity annotated description"})
    public static class AnnotatedEntity extends NliEntity {
        public NliEntity copy() {
            return null;
        }
    }

    public static class TestEntity extends NliEntity {
        public NliEntity copy() {
            return null;
        }
    }

    @NliDescriptions(descriptions = {"enum annotated description"})
    public enum AnnotatedEnum {
        ANNOTATED_ENUM_VALUE
    }

    public enum TestEnum {
        TEST_ENUM_VALUE
    }
}
