package il.ac.technion.nlp.nli.core;

import org.junit.Test;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Collections;

import static org.junit.Assert.assertEquals;

public class NliDescriptionsUtils_ArrowTest {

    @NliDescriptions(descriptions = {"A CUSTOM DESCRIPTION", "Another Phrase"})
    public static class AnnotatedEntity {
    }

    public static class PlainEntity {
    }

    @NliDescriptions(descriptions = {"ANNOTATED METHOD", "Second Method Phrase"})
    public void annotatedMethod() {
    }

    public void plainMethod() {
    }

    @NliDescriptions(descriptions = {"ANNOTATED FIELD", "Second Field Phrase"})
    public String annotatedField;

    public String plainField;

    @NliDescriptions(descriptions = {"ANNOTATED ENUM", "Second Enum Phrase"})
    public enum AnnotatedEnum {
        VALUE
    }

    public enum PlainEnum {
        VALUE
    }

    @Test
    public void generateDescriptionsForMethodWithoutAnnotation() throws Exception {
        Method method = NliDescriptionsUtils_ArrowTest.class.getMethod("plainMethod");
        assertEquals(Collections.singletonList("plain method"),
                NliDescriptionsUtils.generateDescriptionsForMethod(method, false));
    }

    @Test
    public void generateDescriptionsForMethodWithAnnotation() throws Exception {
        Method method = NliDescriptionsUtils_ArrowTest.class.getMethod("annotatedMethod");
        assertEquals(Arrays.asList("annotated method", "annotated method", "second method phrase"),
                NliDescriptionsUtils.generateDescriptionsForMethod(method, true));
    }

    @Test
    public void generateDescriptionsForRelationFieldWithoutAnnotation() throws Exception {
        Field field = NliDescriptionsUtils_ArrowTest.class.getField("plainField");
        assertEquals(Collections.singletonList("plain field"),
                NliDescriptionsUtils.generateDescriptionsForRelationField(field, false));
    }

    @Test
    public void generateDescriptionsForRelationFieldWithAnnotation() throws Exception {
        Field field = NliDescriptionsUtils_ArrowTest.class.getField("annotatedField");
        assertEquals(Arrays.asList("annotated field", "annotated field", "second field phrase"),
                NliDescriptionsUtils.generateDescriptionsForRelationField(field, true));
    }

    @Test
    public void generateDescriptionsForEnumTypeWithoutAnnotation() {
        assertEquals(Collections.singletonList("plain enum"),
                NliDescriptionsUtils.generateDescriptionsForEnumType(enumClass(PlainEnum.class), false));
    }

    @Test
    public void generateDescriptionsForEnumTypeWithAnnotation() {
        assertEquals(Arrays.asList("annotated enum", "annotated enum", "second enum phrase"),
                NliDescriptionsUtils.generateDescriptionsForEnumType(enumClass(AnnotatedEnum.class), true));
    }

    @Test
    public void generateDescriptionsForEnumValue() {
        assertEquals(Collections.singletonList("first value"),
                NliDescriptionsUtils.generateDescriptionsForEnumValue(ExampleEnum.FIRST_VALUE));
    }

    private static <E extends Enum<E>> Class<Enum<?>> enumClass(Class<E> type) {
        @SuppressWarnings({"unchecked", "rawtypes"})
        Class<Enum<?>> result = (Class) type;
        return result;
    }

    private enum ExampleEnum {
        FIRST_VALUE
    }
}
