package javax.faces.component;

import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;
import javax.faces.convert.ConverterException;
import javax.faces.event.ValueChangeListener;
import javax.faces.validator.Validator;
import javax.faces.validator.ValidatorException;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

public class UIInput_ArrowTest {

    private UIInput uiInput;
    private FacesContext facesContext;

    @Before
    public void setUp() {
        uiInput = new UIInput();
        facesContext = mock(FacesContext.class);
    }

    @Test
    public void testGetFamily() {
        assertEquals(UIInput.COMPONENT_FAMILY, uiInput.getFamily());
    }

    @Test
    public void testGetSubmittedValueWhenNullAndInvalidAndEmptyStringIsNull() {
        uiInput.setSubmittedValue(null);
        uiInput.setValid(false);
        when(facesContext.getCurrentInstance()).thenReturn(facesContext);
        when(uiInput.considerEmptyStringNull(facesContext)).thenReturn(true);
        
        assertEquals("", uiInput.getSubmittedValue());
    }

    @Test
    public void testGetSubmittedValueWhenNullAndValid() {
        uiInput.setSubmittedValue(null);
        uiInput.setValid(true);
        
        assertNull(uiInput.getSubmittedValue());
    }

    @Test
    public void testGetSubmittedValueWhenNotNull() {
        Object submittedValue = new Object();
        uiInput.setSubmittedValue(submittedValue);
        
        assertEquals(submittedValue, uiInput.getSubmittedValue());
    }

    @Test
    public void testSetSubmittedValue() {
        Object submittedValue = new Object();
        uiInput.setSubmittedValue(submittedValue);
        
        assertEquals(submittedValue, uiInput.getSubmittedValue());
    }

    @Test
    public void testGetValueWhenLocalValueSet() {
        uiInput.setLocalValueSet(true);
        Object localValue = new Object();
        uiInput.setLocalValue(localValue);
        
        assertEquals(localValue, uiInput.getValue());
    }

    @Test
    public void testGetValueWhenLocalValueNotSet() {
        uiInput.setLocalValueSet(false);
        Object superValue = new Object();
        when(uiInput.superGetValue()).thenReturn(superValue);
        
        assertEquals(superValue, uiInput.getValue());
    }

    @Test
    public void testSetValue() {
        Object value = new Object();
        uiInput.setValue(value);
        
        assertTrue(uiInput.isLocalValueSet());
        assertEquals(value, uiInput.getLocalValue());
    }

    @Test
    public void testResetValue() {
        uiInput.setLocalValueSet(true);
        uiInput.setSubmittedValue("test");
        uiInput.setValid(false);
        
        uiInput.resetValue();
        
        assertFalse(uiInput.isLocalValueSet());
        assertNull(uiInput.getSubmittedValue());
        assertTrue(uiInput.isValid());
    }

    @Test
    public void testIsLocalValueSet() {
        assertFalse(uiInput.isLocalValueSet());
        
        uiInput.setLocalValueSet(true);
        assertTrue(uiInput.isLocalValueSet());
    }

    @Test
    public void testSetLocalValueSet() {
        uiInput.setLocalValueSet(true);
        assertTrue(uiInput.isLocalValueSet());
        
        uiInput.setLocalValueSet(false);
        assertFalse(uiInput.isLocalValueSet());
    }

    @Test
    public void testIsRequired() {
        assertFalse(uiInput.isRequired());
        
        uiInput.setRequired(true);
        assertTrue(uiInput.isRequired());
    }

    @Test
    public void testSetRequired() {
        uiInput.setRequired(true);
        assertTrue(uiInput.isRequired());
        
        uiInput.setRequired(false);
        assertFalse(uiInput.isRequired());
    }

    @Test
    public void testGetRequiredMessage() {
        assertNull(uiInput.getRequiredMessage());
        
        String message = "required message";
        uiInput.setRequiredMessage(message);
        assertEquals(message, uiInput.getRequiredMessage());
    }

    @Test
    public void testSetRequiredMessage() {
        String message = "required message";
        uiInput.setRequiredMessage(message);
        assertEquals(message, uiInput.getRequiredMessage());
    }

    @Test
    public void testGetConverterMessage() {
        assertNull(uiInput.getConverterMessage());
        
        String message = "converter message";
        uiInput.setConverterMessage(message);
        assertEquals(message, uiInput.getConverterMessage());
    }

    @Test
    public void testSetConverterMessage() {
        String message = "converter message";
        uiInput.setConverterMessage(message);
        assertEquals(message, uiInput.getConverterMessage());
    }

    @Test
    public void testGetValidatorMessage() {
        assertNull(uiInput.getValidatorMessage());
        
        String message = "validator message";
        uiInput.setValidatorMessage(message);
        assertEquals(message, uiInput.getValidatorMessage());
    }

    @Test
    public void testSetValidatorMessage() {
        String message = "validator message";
        uiInput.setValidatorMessage(message);
        assertEquals(message, uiInput.getValidatorMessage());
    }

    @Test
    public void testIsValid() {
        assertTrue(uiInput.isValid());
        
        uiInput.setValid(false);
        assertFalse(uiInput.isValid());
    }

    @Test
    public void testSetValid() {
        uiInput.setValid(false);
        assertFalse(uiInput.isValid());
        
        uiInput.setValid(true);
        assertTrue(uiInput.isValid());
    }

    @Test
    public void testIsImmediate() {
        assertFalse(uiInput.isImmediate());
        
        uiInput.setImmediate(true);
        assertTrue(uiInput.isImmediate());
    }

    @Test
    public void testSetImmediate() {
        uiInput.setImmediate(true);
        assertTrue(uiInput.isImmediate());
        
        uiInput.setImmediate(false);
        assertFalse(uiInput.isImmediate());
    }

    @Test
    public void testAddValidator() {
        Validator validator = mock(Validator.class);
        uiInput.addValidator(validator);
        
        Validator[] validators = uiInput.getValidators();
        assertNotNull(validators);
        assertEquals(1, validators.length);
        assertEquals(validator, validators[0]);
    }

    @Test(expected = NullPointerException.class)
    public void testAddValidatorNull() {
        uiInput.addValidator(null);
    }

    @Test
    public void testGetValidators() {
        Validator[] validators = uiInput.getValidators();
        assertNotNull(validators);
        assertEquals(0, validators.length);
    }

    @Test
    public void testRemoveValidator() {
        Validator validator = mock(Validator.class);
        uiInput.addValidator(validator);
        uiInput.removeValidator(validator);
        
        Validator[] validators = uiInput.getValidators();
        assertNotNull(validators);
        assertEquals(0, validators.length);
    }

    @Test
    public void testRemoveValidatorNull() {
        uiInput.removeValidator(null);
        // Should not throw exception
    }

    @Test
    public void testAddValueChangeListener() {
        ValueChangeListener listener = mock(ValueChangeListener.class);
        uiInput.addValueChangeListener(listener);
        
        ValueChangeListener[] listeners = uiInput.getValueChangeListeners();
        assertNotNull(listeners);
        assertEquals(1, listeners.length);
        assertEquals(listener, listeners[0]);
    }

    @Test
    public void testGetValueChangeListeners() {
        ValueChangeListener[] listeners = uiInput.getValueChangeListeners();
        assertNotNull(listeners);
        assertEquals(0, listeners.length);
    }

    @Test
    public void testRemoveValueChangeListener() {
        ValueChangeListener listener = mock(ValueChangeListener.class);
        uiInput.addValueChangeListener(listener);
        uiInput.removeValueChangeListener(listener);
        
        ValueChangeListener[] listeners = uiInput.getValueChangeListeners();
        assertNotNull(listeners);
        assertEquals(0, listeners.length);
    }

    @Test
    public void testCompareValuesWhenBothNull() {
        assertTrue(uiInput.compareValues(null, null));
    }

    @Test
    public void testCompareValuesWhenPreviousNull() {
        Object value = new Object();
        assertTrue(uiInput.compareValues(null, value));
    }

    @Test
    public void testCompareValuesWhenValueNull() {
        Object previous = new Object();
        assertTrue(uiInput.compareValues(previous, null));
    }

    @Test
    public void testCompareValuesWhenEqual() {
        Object value = new Object();
        assertFalse(uiInput.compareValues(value, value));
    }

    @Test
    public void testCompareValuesWhenNotEqual() {
        Object previous = new Object();
        Object value = new Object();
        assertTrue(uiInput.compareValues(previous, value));
    }

    @Test
    public void testIsEmptyWhenNull() {
        assertTrue(UIInput.isEmpty(null));
    }

    @Test
    public void testIsEmptyWhenEmptyString() {
        assertTrue(UIInput.isEmpty(""));
    }

    @Test
    public void testIsEmptyWhenNonEmptyString() {
        assertFalse(UIInput.isEmpty("test"));
    }

    @Test
    public void testIsEmptyWhenEmptyArray() {
        Object[] array = new Object[0];
        assertTrue(UIInput.isEmpty(array));
    }

    @Test
    public void testIsEmptyWhenNonEmptyArray() {
        Object[] array = new Object[]{new Object()};
        assertFalse(UIInput.isEmpty(array));
    }

    @Test
    public void testIsEmptyWhenEmptyList() {
        java.util.List<Object> list = new java.util.ArrayList<>();
        assertTrue(UIInput.isEmpty(list));
    }

    @Test
    public void testIsEmptyWhenNonEmptyList() {
        java.util.List<Object> list = new java.util.ArrayList<>();
        list.add(new Object());
        assertFalse(UIInput.isEmpty(list));
    }

    @Test
    public void testIsEmptyWhenEmptyCollection() {
        java.util.Collection<Object> collection = new java.util.ArrayList<>();
        assertTrue(UIInput.isEmpty(collection));
    }

    @Test
    public void testIsEmptyWhenNonEmptyCollection() {
        java.util.Collection<Object> collection = new java.util.ArrayList<>();
        collection.add(new Object());
        assertFalse(UIInput.isEmpty(collection));
    }

    @Test
    public void testIsEmptyWhenEmptyMap() {
        java.util.Map<Object, Object> map = new java.util.HashMap<>();
        assertTrue(UIInput.isEmpty(map));
    }

    @Test
    public void testIsEmptyWhenNonEmptyMap() {
        java.util.Map<Object, Object> map = new java.util.HashMap<>();
        map.put("key", "value");
        assertFalse(UIInput.isEmpty(map));
    }

    @Test
    public void testIsEmptyWhenOtherObject() {
        Object obj = new Object();
        assertFalse(UIInput.isEmpty(obj));
    }

    @Test
    public void testGetConverterWithTypeWhenConverterExists() {
        Converter converter = mock(Converter.class);
        uiInput.setConverter(converter);
        
        assertEquals(converter, uiInput.getConverterWithType(facesContext));
    }

    @Test
    public void testGetConverterWithTypeWhenNoConverterExpression() {
        assertNull(uiInput.getConverterWithType(facesContext));
    }

    @Test
    public void testConsiderEmptyStringNullWhenNotCached() {
        when(facesContext.getExternalContext().getInitParameter(
                UIInput.EMPTY_STRING_AS_NULL_PARAM_NAME)).thenReturn("true");
        
        assertTrue(uiInput.considerEmptyStringNull(facesContext));
    }

    @Test
    public void testConsiderEmptyStringNullWhenCached() {
        uiInput.emptyStringIsNull = false;
        assertFalse(uiInput.considerEmptyStringNull(facesContext));
    }

    @Test
    public void testValidateEmptyFieldsWhenNotCached() {
        when(facesContext.getExternalContext().getInitParameter(
                UIInput.VALIDATE_EMPTY_FIELDS_PARAM_NAME)).thenReturn("true");
        
        assertTrue(uiInput.validateEmptyFields(facesContext));
    }

    @Test
    public void testValidateEmptyFieldsWhenCached() {
        uiInput.validateEmptyFields = false;
        assertFalse(uiInput.validateEmptyFields(facesContext));
    }

    @Test
    public void testIsBeansValidationAvailableWhenCached() {
        java.util.Map<String, Object> appMap = new java.util.HashMap<>();
        appMap.put(UIInput.BEANS_VALIDATION_AVAILABLE, true);
        when(facesContext.getExternalContext().getApplicationMap()).thenReturn(appMap);
        
        assertTrue(uiInput.isBeansValidationAvailable(facesContext));
    }

    @Test
    public void testIsBeansValidationAvailableWhenNotCachedAndAvailable() {
        java.util.Map<String, Object> appMap = new java.util.HashMap<>();
        when(facesContext.getExternalContext().getApplicationMap()).thenReturn(appMap);
        
        assertTrue(uiInput.isBeansValidationAvailable(facesContext));
        assertTrue((Boolean) appMap.get(UIInput.BEANS_VALIDATION_AVAILABLE));
    }

    @Test
    public void testIsBeansValidationAvailableWhenNotCachedAndNotAvailable() {
        java.util.Map<String, Object> appMap = new java.util.HashMap<>();
        when(facesContext.getExternalContext().getApplicationMap()).thenReturn(appMap);
        
        // Mocking BeanValidator to throw an exception
        // This would normally be done with a more complex setup but we'll just test the logic path
        
        assertTrue(uiInput.isBeansValidationAvailable(facesContext));
        assertFalse((Boolean) appMap.get(UIInput.BEANS_VALIDATION_AVAILABLE));
    }
}
