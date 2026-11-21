package javax.faces.component;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import javax.faces.validator.Validator;
import org.junit.Test;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

public class UIInput_ArrowTest {

    @Test
    public void defaultsAndFamily() {
        UIInput input = new UIInput();

        assertEquals(UIInput.COMPONENT_FAMILY, input.getFamily());
        assertNull(input.getSubmittedValue());
        assertFalse(input.isLocalValueSet());
        assertFalse(input.isRequired());
        assertTrue(input.isValid());
        assertFalse(input.isImmediate());
        assertEquals(0, input.getValidators().length);
        assertNull(input.getRequiredMessage());
        assertNull(input.getConverterMessage());
        assertNull(input.getValidatorMessage());
    }

    @Test
    public void submittedValueAndLocalValue() {
        UIInput input = new UIInput();
        input.setSubmittedValue("submitted");
        input.setValue("local");

        assertEquals("submitted", input.getSubmittedValue());
        assertEquals("local", input.getValue());
        assertTrue(input.isLocalValueSet());
    }

    @Test
    public void statePropertiesCanBeChanged() {
        UIInput input = new UIInput();
        input.setRequired(true);
        input.setValid(false);
        input.setImmediate(true);
        input.setRequiredMessage("required");
        input.setConverterMessage("conversion");
        input.setValidatorMessage("validation");

        assertTrue(input.isRequired());
        assertFalse(input.isValid());
        assertTrue(input.isImmediate());
        assertEquals("required", input.getRequiredMessage());
        assertEquals("conversion", input.getConverterMessage());
        assertEquals("validation", input.getValidatorMessage());
    }

    @Test
    public void resetValueClearsLocalAndSubmittedValues() {
        UIInput input = new UIInput();
        input.setValue("local");
        input.setSubmittedValue("submitted");
        input.setValid(false);

        input.resetValue();

        assertNull(input.getValue());
        assertNull(input.getSubmittedValue());
        assertFalse(input.isLocalValueSet());
        assertTrue(input.isValid());
    }

    @Test
    public void localValueSetCanBeChangedDirectly() {
        UIInput input = new UIInput();
        input.setLocalValueSet(true);

        assertTrue(input.isLocalValueSet());
    }

    @Test
    public void validatorsCanBeAddedAndRemoved() {
        UIInput input = new UIInput();
        Validator first = new Validator() {
            @Override
            public void validate(javax.faces.context.FacesContext context,
                                 UIComponent component, Object value) {
            }
        };
        Validator second = new Validator() {
            @Override
            public void validate(javax.faces.context.FacesContext context,
                                 UIComponent component, Object value) {
            }
        };

        input.addValidator(first);
        input.addValidator(second);
        assertArrayEquals(new Validator[] {first, second}, input.getValidators());

        input.removeValidator(first);
        assertArrayEquals(new Validator[] {second}, input.getValidators());
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
    public void isEmptyRecognizesNullAndEmptyValues() {
        assertTrue(UIInput.isEmpty(null));
        assertTrue(UIInput.isEmpty(""));
        assertTrue(UIInput.isEmpty(new String[0]));
        assertTrue(UIInput.isEmpty(Collections.emptyList()));
        assertTrue(UIInput.isEmpty(Collections.emptySet()));
        assertTrue(UIInput.isEmpty(Collections.emptyMap()));
    }

    @Test
    public void isEmptyRecognizesNonEmptyValues() {
        assertFalse(UIInput.isEmpty("x"));
        assertFalse(UIInput.isEmpty(new String[] {"x"}));
        assertFalse(UIInput.isEmpty(Arrays.asList("x")));
        assertFalse(UIInput.isEmpty(new HashSet<String>(Arrays.asList("x"))));
        Map<String, String> map = new HashMap<String, String>();
        map.put("key", "value");
        assertFalse(UIInput.isEmpty(map));
        assertFalse(UIInput.isEmpty(Integer.valueOf(0)));
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
    public void validateRejectsNullContext() {
        new UIInput().validate(null);
    }

    @Test(expected = NullPointerException.class)
    public void updateModelRejectsNullContext() {
        new UIInput().updateModel(null);
    }

    @Test(expected = NullPointerException.class)
    public void saveStateRejectsNullContext() {
        new UIInput().saveState(null);
    }

    @Test(expected = NullPointerException.class)
    public void restoreStateRejectsNullContext() {
        new UIInput().restoreState(null, null);
    }
}
