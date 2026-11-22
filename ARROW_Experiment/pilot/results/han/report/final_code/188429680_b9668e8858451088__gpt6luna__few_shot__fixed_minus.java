package il.ac.technion.nlp.nli.core;

import il.ac.technion.nlp.nli.core.state.NliEntity;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class NliDescriptionsUtils_ArrowTest {

    @NliDescriptions(descriptions = {"A custom description", "SECOND phrase"})
    public static class AnnotatedEntity implements NliEntity {
    }

    @NliDescriptions(descriptions = {"Open the item", "ANOTHER phrase"})
    public void openItemNow(NliEntity entity) {
    }

    @NliDescriptions(descriptions = {"Related records", "SECONDARY"})
    public String relatedRecords;

    public enum SampleEnum {
        FIRST_VALUE
    }

    @Test
    public void generatesMethodDescriptionAndAnnotation() throws Exception {
        Method method = getValidNliMethod();
        List<String> expected = Arrays.asList("open item now", "open the item", "another phrase");
        assertEquals(expected, NliDescriptionsUtils.generateDescriptionsForMethod(method, true));
    }

    @Test
    public void generatesMethodDescriptionWithoutAnnotation() throws Exception {
        Method method = getValidNliMethod();
        assertEquals(Collections.singletonList("open item now"),
                NliDescriptionsUtils.generateDescriptionsForMethod(method, false));
    }

    private static Method getValidNliMethod() throws Exception {
        return NliDescriptionsUtils_ArrowTest.class.getMethod("openItemNow", NliEntity.class);
    }

    @Test
    public void generatesRelationFieldDescriptionAndAnnotation() throws Exception {
        Field field = NliDescriptionsUtils_ArrowTest.class.getField("relatedRecords");
        List<String> expected = Arrays.asList("related records", "related records", "secondary");
        assertEquals(expected, NliDescriptionsUtils.generateDescriptionsForRelationField(field, true));
    }

    @Test
    public void generatesEntityTypeDescriptionAndAnnotation() {
        List<String> expected = Arrays.asList("annotated entity", "a custom description", "second phrase");
        assertEquals(expected,
                NliDescriptionsUtils.generateDescriptionsForNliEntityType(AnnotatedEntity.class, true));
    }

    @Test
    public void generatesEnumTypeDescriptionWithoutAnnotation() {
        @SuppressWarnings("unchecked")
        Class<Enum<?>> enumType = (Class<Enum<?>>) (Class<?>) SampleEnum.class;
        assertEquals(Collections.singletonList("sample enum"),
                NliDescriptionsUtils.generateDescriptionsForEnumType(enumType, false));
    }

    @Test
    public void generatesEnumValueDescription() {
        assertEquals(Collections.singletonList("first value"),
                NliDescriptionsUtils.generateDescriptionsForEnumValue(SampleEnum.FIRST_VALUE));
    }
}
