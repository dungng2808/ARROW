package javax.faces.component;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import org.junit.Test;

import static org.junit.Assert.*;

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
        input.setSubmittedValue("submitted");

        assertEquals("submitted", input.getSubmittedValue());
    }

    @Test
    public void valueSetLocallyAndReset() {
        UIInput input = new UIInput();
        input.setValue("local");

        assertEquals("local", input.getValue());
        assertTrue(input.isLocalValueSet());

        input.resetValue();

        assertFalse(input.isLocalValueSet());
        assertNull(input.getSubmittedValue());
    }

    @Test
    public void requiredValidAndImmediatePropertiesDefaultAndChange() {
        UIInput input = new UIInput();

        assertFalse(input.isRequired());
        assertTrue(input.isValid());
        assertFalse(input.isImmediate());

        input.setRequired(true);
        input.setValid(false);
        input.setImmediate(true);

        assertTrue(input.isRequired());
        assertFalse(input.isValid());
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
    public void validatorsCanBeAddedRetrievedAndRemoved() {
        UIInput input = new UIInput();
        javax.faces.validator.Validator validator = new javax.faces.validator.Validator() {
            public void validate(javax.faces.context.FacesContext context,
                                 javax.faces.component.UIComponent component,
                                 Object value) {
            }
        };

        assertEquals(0, input.getValidators().length);
        input.addValidator(validator);
        assertArrayEquals(new javax.faces.validator.Validator[] { validator }, input.getValidators());

        input.removeValidator(validator);
        assertEquals(0, input.getValidators().length);
    }

    @Test(expected = NullPointerException.class)
    public void addingNullValidatorThrows() {
        new UIInput().addValidator(null);
    }

    @Test
    public void valueChangeListenersCanBeAddedRetrievedAndRemoved() {
        UIInput input = new UIInput();
        javax.faces.event.ValueChangeListener listener = new javax.faces.event.ValueChangeListener() {
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
        assertFalse(UIInput.isEmpty(Integer.valueOf(0)));
    }

    @Test
    public void compareValuesHandlesNullAndEqualValues() {
        UIInput input = new UIInput();

        assertFalse(input.compareValues(null, null));
        assertTrue(input.compareValues(null, "value"));
        assertTrue(input.compareValues("value", null));
        assertFalse(input.compareValues("same", "same"));
        assertTrue(input.compareValues("one", "two"));
    }

    @Test
    public void compareValuesUsesComparableEquality() {
        UIInput input = new UIInput();

        assertFalse(input.compareValues(Integer.valueOf(4), Integer.valueOf(4)));
        assertTrue(input.compareValues(Integer.valueOf(4), Integer.valueOf(5)));
    }

    @Test
    public void decodeWithNullContextThrows() {
        UIInput input = new UIInput();
        try {
            input.decode(null);
            fail("Expected NullPointerException");
        } catch (NullPointerException expected) {
            assertNotNull(expected);
        }
    }

    @Test
    public void processDecodesWithNullContextThrows() {
        UIInput input = new UIInput();
        try {
            input.processDecodes(null);
            fail("Expected NullPointerException");
        } catch (NullPointerException expected) {
            assertNotNull(expected);
        }
    }

    @Test
    public void processValidatorsWithNullContextThrows() {
        UIInput input = new UIInput();
        try {
            input.processValidators(null);
            fail("Expected NullPointerException");
        } catch (NullPointerException expected) {
            assertNotNull(expected);
        }
    }

    @Test
    public void processUpdatesWithNullContextThrows() {
        UIInput input = new UIInput();
        try {
            input.processUpdates(null);
            fail("Expected NullPointerException");
        } catch (NullPointerException expected) {
            assertNotNull(expected);
        }
    }

    @Test
    public void updateModelWithNullContextThrows() {
        UIInput input = new UIInput();
        try {
            input.updateModel(null);
            fail("Expected NullPointerException");
        } catch (NullPointerException expected) {
            assertNotNull(expected);
        }
    }

    @Test
    public void validateWithNullContextThrows() {
        UIInput input = new UIInput();
        try {
            input.validate(null);
            fail("Expected NullPointerException");
        } catch (NullPointerException expected) {
            assertNotNull(expected);
        }
    }

    @Test
    public void saveStateWithNullContextThrows() {
        UIInput input = new UIInput();
        try {
            input.saveState(null);
            fail("Expected NullPointerException");
        } catch (NullPointerException expected) {
            assertNotNull(expected);
        }
    }

    @Test
    public void restoreStateWithNullContextThrows() {
        UIInput input = new UIInput();
        try {
            input.restoreState(null, null);
            fail("Expected NullPointerException");
        } catch (NullPointerException expected) {
            assertNotNull(expected);
        }
    }
}
