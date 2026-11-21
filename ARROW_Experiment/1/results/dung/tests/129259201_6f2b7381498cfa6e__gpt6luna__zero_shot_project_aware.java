package javax.faces.component;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import org.junit.Test;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

public class UIInput_ArrowTest {

    @Test
    public void constructorSetsFamilyAndRendererType() {
        UIInput input = new UIInput();

        assertEquals(UIInput.COMPONENT_FAMILY, input.getFamily());
        assertEquals("javax.faces.Text", input.getRendererType());
    }

    @Test
    public void submittedValueCanBeSetAndRetrieved() {
        UIInput input = new UIInput();
        Object submitted = "submitted";

        input.setSubmittedValue(submitted);

        assertSame(submitted, input.getSubmittedValue());
    }

    @Test
    public void valueSetIsLocalAndReturned() {
        UIInput input = new UIInput();
        Object value = "local";

        input.setValue(value);

        assertSame(value, input.getValue());
        assertTrue(input.isLocalValueSet());
    }

    @Test
    public void resetValueClearsLocalAndSubmittedValues() {
        UIInput input = new UIInput();
        input.setValue("local");
        input.setSubmittedValue("submitted");

        input.resetValue();

        assertNull(input.getSubmittedValue());
        assertFalse(input.isLocalValueSet());
    }

    @Test
    public void requiredDefaultsToFalseAndCanBeChanged() {
        UIInput input = new UIInput();

        assertFalse(input.isRequired());
        input.setRequired(true);
        assertTrue(input.isRequired());
    }

    @Test
    public void validDefaultsToTrueAndCanBeChanged() {
        UIInput input = new UIInput();

        assertTrue(input.isValid());
        input.setValid(false);
        assertFalse(input.isValid());
    }

    @Test
    public void immediateDefaultsToFalseAndCanBeChanged() {
        UIInput input = new UIInput();

        assertFalse(input.isImmediate());
        input.setImmediate(true);
        assertTrue(input.isImmediate());
    }

    @Test
    public void messagePropertiesCanBeSetAndRetrieved() {
        UIInput input = new UIInput();

        input.setRequiredMessage("required");
        input.setConverterMessage("converter");
        input.setValidatorMessage("validator");

        assertEquals("required", input.getRequiredMessage());
        assertEquals("converter", input.getConverterMessage());
        assertEquals("validator", input.getValidatorMessage());
    }

    @Test
    public void validatorCollectionCanBeAddedRetrievedAndRemoved() {
        UIInput input = new UIInput();
        javax.faces.validator.Validator validator = new javax.faces.validator.Validator() {
            public void validate(javax.faces.context.FacesContext context,
                                 UIComponent component, Object value) {
            }
        };

        assertEquals(0, input.getValidators().length);
        input.addValidator(validator);
        assertArrayEquals(new javax.faces.validator.Validator[] { validator },
                          input.getValidators());

        input.removeValidator(validator);

        assertEquals(0, input.getValidators().length);
    }

    @Test(expected = NullPointerException.class)
    public void addingNullValidatorThrows() {
        new UIInput().addValidator(null);
    }

    @Test
    public void removingNullValidatorDoesNothing() {
        UIInput input = new UIInput();

        input.removeValidator(null);

        assertEquals(0, input.getValidators().length);
    }

    @Test
    public void valueChangeListenerCanBeAddedRetrievedAndRemoved() {
        UIInput input = new UIInput();
        javax.faces.event.ValueChangeListener listener =
                new javax.faces.event.ValueChangeListener() {
                    public void processValueChange(javax.faces.event.ValueChangeEvent event) {
                    }
                };

        input.addValueChangeListener(listener);
        assertArrayEquals(new javax.faces.event.ValueChangeListener[] { listener },
                          input.getValueChangeListeners());

        input.removeValueChangeListener(listener);

        assertEquals(0, input.getValueChangeListeners().length);
    }

    @Test
    public void isEmptyRecognizesNullAndEmptyValues() {
        assertTrue(UIInput.isEmpty(null));
        assertTrue(UIInput.isEmpty(""));
        assertTrue(UIInput.isEmpty(new int[0]));
        assertTrue(UIInput.isEmpty(Collections.emptyList()));
        assertTrue(UIInput.isEmpty(Collections.emptySet()));
        assertTrue(UIInput.isEmpty(Collections.emptyMap()));
    }

    @Test
    public void isEmptyRecognizesNonEmptyValues() {
        assertFalse(UIInput.isEmpty("x"));
        assertFalse(UIInput.isEmpty(new int[] { 1 }));
        assertFalse(UIInput.isEmpty(Arrays.asList("x")));
        assertFalse(UIInput.isEmpty(new HashSet<String>(Arrays.asList("x"))));
        Map<String, String> map = new HashMap<String, String>();
        map.put("key", "value");
        assertFalse(UIInput.isEmpty(map));
        assertFalse(UIInput.isEmpty(Integer.valueOf(1)));
    }

    @Test
    public void compareValuesHandlesNullAndEqualValues() {
        UIInput input = new UIInput();

        assertFalse(input.compareValues(null, null));
        assertTrue(input.compareValues(null, "value"));
        assertTrue(input.compareValues("value", null));
        assertFalse(input.compareValues("same", "same"));
        assertTrue(input.compareValues("first", "second"));
    }

    @Test
    public void compareValuesUsesComparableEquality() {
        UIInput input = new UIInput();

        assertFalse(input.compareValues(Integer.valueOf(1), Long.valueOf(1)));
        assertTrue(input.compareValues(Integer.valueOf(1), Long.valueOf(2)));
    }

    @Test
    public void compareValuesTreatsIncompatibleComparablesAsDifferent() {
        UIInput input = new UIInput();

        assertTrue(input.compareValues(Integer.valueOf(1), "1"));
    }

    @Test(expected = NullPointerException.class)
    public void decodeRejectsNullContext() {
        new UIInput().decode(null);
    }

    @Test(expected = NullPointerException.class)
    public void processDecodesRejectsNullContext() {
        new UIInput().processDecodes(null);
    }

    @Test(expected = NullPointerException.class)
    public void processValidatorsRejectsNullContext() {
        new UIInput().processValidators(null);
    }

    @Test(expected = NullPointerException.class)
    public void processUpdatesRejectsNullContext() {
        new UIInput().processUpdates(null);
    }

    @Test(expected = NullPointerException.class)
    public void updateModelRejectsNullContext() {
        new UIInput().updateModel(null);
    }

    @Test(expected = NullPointerException.class)
    public void validateRejectsNullContext() {
        new UIInput().validate(null);
    }

    @Test(expected = NullPointerException.class)
    public void saveStateRejectsNullContext() {
        new UIInput().saveState(null);
    }

    @Test(expected = NullPointerException.class)
    public void restoreStateRejectsNullContext() {
        new UIInput().restoreState(null, null);
    }

    @Test
    public void updateModelDoesNothingWhenNoLocalValueIsSet() {
        UIInput input = new UIInput();

        input.updateModel(null);
    }

    @Test
    public void restoreNullStateDoesNothing() {
        UIInput input = new UIInput();

        input.restoreState(null, null);
    }
}
