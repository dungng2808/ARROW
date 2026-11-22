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
        List<String> descriptions = NliDescriptionsUtils.generateDescriptionsForMethod(method, false);
        assertEquals(1, descriptions.size());
        assertEquals("test method", descriptions.get(0));
    }

    @Test
    public void testGenerateDescriptionsForMethodWithAnnotation() throws NoSuchMethodException {
        Method method = TestClass.class.getMethod("annotatedMethod");
        List<String> descriptions = NliDescriptionsUtils.generateDescriptionsForMethod(method, true);
        assertEquals(2, descriptions.size());
        assertEquals("test method", descriptions.get(0));
        assertEquals("annotated description", descriptions.get(1));
    }

    @Test
    public void testGenerateDescriptionsForRelationField() throws NoSuchFieldException {
        Field field = TestClass.class.getField("testField");
        List<String> descriptions = NliDescriptionsUtils.generateDescriptionsForRelationField(field, false);
        assertEquals(1, descriptions.size());
        assertEquals("test field", descriptions.get(0));
    }

    @Test
    public void testGenerateDescriptionsForRelationFieldWithAnnotation() throws NoSuchFieldException {
        Field field = TestClass.class.getField("annotatedField");
        List<String> descriptions = NliDescriptionsUtils.generateDescriptionsForRelationField(field, true);
        assertEquals(2, descriptions.size());
        assertEquals("test field", descriptions.get(0));
        assertEquals("annotated field", descriptions.get(1));
    }

    @Test
    public void testGenerateDescriptionsForNliEntityType() throws ClassNotFoundException {
        Class<? extends NliEntity> entityType = (Class<? extends NliEntity>) Class.forName("il.ac.technion.nlp.nli.core.TestEntity");
        List<String> descriptions = NliDescriptionsUtils.generateDescriptionsForNliEntityType(entityType, false);
        assertEquals(1, descriptions.size());
        assertEquals("test entity", descriptions.get(0));
    }

    @Test
    public void testGenerateDescriptionsForNliEntityTypeWithAnnotation() throws ClassNotFoundException {
        Class<? extends NliEntity> entityType = (Class<? extends NliEntity>) Class.forName("il.ac.technion.nlp.nli.core.TestEntity");
        List<String> descriptions = NliDescriptionsUtils.generateDescriptionsForNliEntityType(entityType, true);
        assertEquals(2, descriptions.size());
        assertEquals("test entity", descriptions.get(0));
        assertEquals("entity description", descriptions.get(1));
    }

    @Test
    public void testGenerateDescriptionsForEnumType() {
        List<String> descriptions = NliDescriptionsUtils.generateDescriptionsForEnumType(TestEnum.class, false);
        assertEquals(1, descriptions.size());
        assertEquals("test enum", descriptions.get(0));
    }

    @Test
    public void testGenerateDescriptionsForEnumTypeWithAnnotation() {
        List<String> descriptions = NliDescriptionsUtils.generateDescriptionsForEnumType(TestEnum.class, true);
        assertEquals(2, descriptions.size());
        assertEquals("test enum", descriptions.get(0));
        assertEquals("enum description", descriptions.get(1));
    }

    @Test
    public void testGenerateDescriptionsForEnumValue() {
        TestEnum enumValue = TestEnum.TEST_ENUM_VALUE;
        List<String> descriptions = NliDescriptionsUtils.generateDescriptionsForEnumValue(enumValue);
        assertEquals(1, descriptions.size());
        assertEquals("test enum value", descriptions.get(0));
    }

    @Test
    public void testGenerateDescriptionsFromAnnotation() {
        NliDescriptions annotation = new NliDescriptions() {
            @Override
            public String[] descriptions() {
                return new String[]{"First Description", "SECOND DESCRIPTION"};
            }

            @Override
            public Class<? extends java.lang.annotation.Annotation> annotationType() {
                return NliDescriptions.class;
            }
        };
        List<String> descriptions = NliDescriptionsUtils.generateDescriptionsFromAnnotation(annotation);
        assertEquals(2, descriptions.size());
        assertEquals("first description", descriptions.get(0));
        assertEquals("second description", descriptions.get(1));
    }

    @Test
    public void testCreateDescriptionFromIdentifierName() {
        String result = NliDescriptionsUtils.createDescriptionFromIdentifierName("camelCaseName");
        assertEquals("camel case name", result);
    }

    @Test
    public void testCreateDescriptionFromIdentifierNameWithUnderscores() {
        String result = NliDescriptionsUtils.createDescriptionFromIdentifierName("snake_case_name");
        assertEquals("snake case name", result);
    }

    // Helper classes for testing
    public static class TestClass {
        public String testField;
        @NliDescriptions(descriptions = {"annotated field"})
        public String annotatedField;

        public void testMethod() {}
        @NliDescriptions(descriptions = {"annotated description"})
        public void annotatedMethod() {}
    }

    @NliDescriptions(descriptions = {"entity description"})
    public static class TestEntity extends NliEntity {
        @Override
        public Object getId() {
            return null;
        }
    }

    @NliDescriptions(descriptions = {"enum description"})
    public enum TestEnum {
        TEST_ENUM_VALUE
    }
}
