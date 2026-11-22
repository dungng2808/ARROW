package il.ac.technion.nlp.nli.core;

import il.ac.technion.nlp.nli.core.state.NliEntity;
import org.junit.Test;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Collections;

import static org.junit.Assert.assertEquals;

public class NliDescriptionsUtils_ArrowTest {

    @NliDescriptions(descriptions = {"ADD A NEW ITEM", "Create item"})
    public void addNewItem() {
    }

    @NliDescriptions(descriptions = {"PRIMARY RELATION", "Related item"})
    public String relatedItems;

    @NliDescriptions(descriptions = {"PERSON RECORD", "Human"})
    public static class PersonRecord implements NliEntity {
    }

    @NliDescriptions(descriptions = {"TASK STATUS"})
    public enum TaskStatus {
        IN_PROGRESS
    }

    public enum PlainStatus {
        NEEDS_REVIEW
    }

    @Test
    public void generateDescriptionsForMethodWithAnnotation() throws Exception {
        Method method = NliDescriptionsUtils_ArrowTest.class.getMethod("validNliMethod", NliEntity.class);
        assertEquals(Arrays.asList("valid nli method", "add a new item", "create item"),
                NliDescriptionsUtils.generateDescriptionsForMethod(method, true));
    }

    @Test
    public void generateDescriptionsForMethodWithoutAnnotation() throws Exception {
        Method method = NliDescriptionsUtils_ArrowTest.class.getMethod("validNliMethod", NliEntity.class);
        assertEquals(Collections.singletonList("valid nli method"),
                NliDescriptionsUtils.generateDescriptionsForMethod(method, false));
    }

    @NliDescriptions(descriptions = {"ADD A NEW ITEM", "Create item"})
    public void validNliMethod(NliEntity entity) {
    }

    @Test
    public void generateDescriptionsForRelationFieldWithAnnotation() throws Exception {
        Field field = NliDescriptionsUtils_ArrowTest.class.getField("relatedItems");
        assertEquals(Arrays.asList("related items", "primary relation", "related item"),
                NliDescriptionsUtils.generateDescriptionsForRelationField(field, true));
    }

    @Test
    public void generateDescriptionsForRelationFieldWithoutAnnotation() throws Exception {
        Field field = NliDescriptionsUtils_ArrowTest.class.getField("relatedItems");
        assertEquals(Collections.singletonList("related items"),
                NliDescriptionsUtils.generateDescriptionsForRelationField(field, false));
    }

    @Test
    public void generateDescriptionsForNliEntityTypeWithAnnotation() {
        assertEquals(Arrays.asList("person record", "person record", "human"),
                NliDescriptionsUtils.generateDescriptionsForNliEntityType(PersonRecord.class, true));
    }

    @Test
    public void generateDescriptionsForNliEntityTypeWithoutAnnotation() {
        assertEquals(Collections.singletonList("person record"),
                NliDescriptionsUtils.generateDescriptionsForNliEntityType(PersonRecord.class, false));
    }

    @Test
    public void generateDescriptionsForEnumTypeWithAnnotation() {
        @SuppressWarnings("unchecked")
        Class<Enum<?>> enumType = (Class<Enum<?>>) (Class<?>) TaskStatus.class;
        assertEquals(Arrays.asList("task status", "task status"),
                NliDescriptionsUtils.generateDescriptionsForEnumType(enumType, true));
    }

    @Test
    public void generateDescriptionsForEnumTypeWithoutAnnotation() {
        @SuppressWarnings("unchecked")
        Class<Enum<?>> enumType = (Class<Enum<?>>) (Class<?>) PlainStatus.class;
        assertEquals(Collections.singletonList("plain status"),
                NliDescriptionsUtils.generateDescriptionsForEnumType(enumType, false));
    }

    @Test
    public void generateDescriptionsForEnumValue() {
        assertEquals(Collections.singletonList("in progress"),
                NliDescriptionsUtils.generateDescriptionsForEnumValue(TaskStatus.IN_PROGRESS));
    }

    @Test
    public void generateDescriptionsForEnumValueWithMultipleUnderscores() {
        assertEquals(Collections.singletonList("needs review"),
                NliDescriptionsUtils.generateDescriptionsForEnumValue(PlainStatus.NEEDS_REVIEW));
    }
}
