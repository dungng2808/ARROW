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
    public void testGetSubmittedValueWhenNullAndInvalidAndEmptyStringNull() {
        uiInput.setSubmittedValue(null);
        uiInput.setValid(false);
        when(facesContext.getCurrentInstance()).thenReturn(facesContext);
        when(uiInput.considerEmptyStringNull(facesContext)).thenReturn(true);
        
        assertEquals("", uiInput.getSubmittedValue());
    }

    @Test
    public void testGetSubmittedValueWhenNotNull() {
        uiInput.setSubmittedValue("test");
        assertEquals("test", uiInput.getSubmittedValue());
    }

    @Test
    public void testGetValueWhenLocalValueSet() {
        uiInput.setLocalValueSet(true);
        uiInput.setLocalValue("testValue");
        assertEquals("testValue", uiInput.getValue());
    }

    @Test
    public void testGetValueWhenLocalValueNotSet() {
        uiInput.setLocalValueSet(false);
        uiInput.setValue("defaultValue");
        assertEquals("defaultValue", uiInput.getValue());
    }

    @Test
    public void testSetValueSetsLocalValueSet() {
        uiInput.setValue("testValue");
        assertTrue(uiInput.isLocalValueSet());
    }

    @Test
    public void testResetValueClearsSubmittedValueAndStates() {
        uiInput.setSubmittedValue("test");
        uiInput.setLocalValueSet(true);
        uiInput.setValid(false);
        
        uiInput.resetValue();
        
        assertNull(uiInput.getSubmittedValue());
        assertFalse(uiInput.isLocalValueSet());
        assertTrue(uiInput.isValid());
    }

    @Test
    public void testIsRequiredDefaultsToFalse() {
        assertFalse(uiInput.isRequired());
    }

    @Test
    public void testSetRequired() {
        uiInput.setRequired(true);
        assertTrue(uiInput.isRequired());
    }

    @Test
    public void testGetRequiredMessage() {
        assertNull(uiInput.getRequiredMessage());
    }

    @Test
    public void testSetRequiredMessage() {
        uiInput.setRequiredMessage("required message");
        assertEquals("required message", uiInput.getRequiredMessage());
    }

    @Test
    public void testGetConverterMessage() {
        assertNull(uiInput.getConverterMessage());
    }

    @Test
    public void testSetConverterMessage() {
        uiInput.setConverterMessage("converter message");
        assertEquals("converter message", uiInput.getConverterMessage());
    }

    @Test
    public void testGetValidatorMessage() {
        assertNull(uiInput.getValidatorMessage());
    }

    @Test
    public void testSetValidatorMessage() {
        uiInput.setValidatorMessage("validator message");
        assertEquals("validator message", uiInput.getValidatorMessage());
    }

    @Test
    public void testIsValidDefaultsToTrue() {
        assertTrue(uiInput.isValid());
    }

    @Test
    public void testSetValid() {
        uiInput.setValid(false);
        assertFalse(uiInput.isValid());
    }

    @Test
    public void testIsImmediateDefaultsToFalse() {
        assertFalse(uiInput.isImmediate());
    }

    @Test
    public void testSetImmediate() {
        uiInput.setImmediate(true);
        assertTrue(uiInput.isImmediate());
    }

    @Test
    public void testAddValidator() {
        Validator validator = mock(Validator.class);
        uiInput.addValidator(validator);
        assertNotNull(uiInput.getValidators());
        assertEquals(1, uiInput.getValidators().length);
    }

    @Test(expected = NullPointerException.class)
    public void testAddValidatorNull() {
        uiInput.addValidator(null);
    }

    @Test
    public void testGetValidatorsEmpty() {
        assertArrayEquals(UIInput.EMPTY_VALIDATOR, uiInput.getValidators());
    }

    @Test
    public void testRemoveValidator() {
        Validator validator = mock(Validator.class);
        uiInput.addValidator(validator);
        uiInput.removeValidator(validator);
        assertArrayEquals(UIInput.EMPTY_VALIDATOR, uiInput.getValidators());
    }

    @Test
    public void testAddValueChangeListener() {
        ValueChangeListener listener = mock(ValueChangeListener.class);
        uiInput.addValueChangeListener(listener);
        assertNotNull(uiInput.getValueChangeListeners());
    }

    @Test
    public void testGetValueChangeListeners() {
        assertNotNull(uiInput.getValueChangeListeners());
    }

    @Test
    public void testRemoveValueChangeListener() {
        ValueChangeListener listener = mock(ValueChangeListener.class);
        uiInput.addValueChangeListener(listener);
        uiInput.removeValueChangeListener(listener);
        assertNotNull(uiInput.getValueChangeListeners());
    }

    @Test
    public void testIsEmptyWithNull() {
        assertTrue(UIInput.isEmpty(null));
    }

    @Test
    public void testIsEmptyWithStringEmpty() {
        assertTrue(UIInput.isEmpty(""));
    }

    @Test
    public void testIsEmptyWithStringNonEmpty() {
        assertFalse(UIInput.isEmpty("test"));
    }

    @Test
    public void testIsEmptyWithArrayEmpty() {
        Object[] array = new Object[0];
        assertTrue(UIInput.isEmpty(array));
    }

    @Test
    public void testIsEmptyWithArrayNonEmpty() {
        Object[] array = new Object[]{1};
        assertFalse(UIInput.isEmpty(array));
    }

    @Test
    public void testIsEmptyWithListEmpty() {
        java.util.List<Object> list = new java.util.ArrayList<>();
        assertTrue(UIInput.isEmpty(list));
    }

    @Test
    public void testIsEmptyWithListNonEmpty() {
        java.util.List<Object> list = new java.util.ArrayList<>();
        list.add("test");
        assertFalse(UIInput.isEmpty(list));
    }

    @Test
    public void testIsEmptyWithMapEmpty() {
        java.util.Map<Object, Object> map = new java.util.HashMap<>();
        assertTrue(UIInput.isEmpty(map));
    }

    @Test
    public void testIsEmptyWithMapNonEmpty() {
        java.util.Map<Object, Object> map = new java.util.HashMap<>();
        map.put("key", "value");
        assertFalse(UIInput.isEmpty(map));
    }

    @Test
    public void testCompareValuesWithNullPreviousAndNullValue() {
        assertTrue(uiInput.compareValues(null, null));
    }

    @Test
    public void testCompareValuesWithNullPreviousAndNonNullValue() {
        assertFalse(uiInput.compareValues(null, "test"));
    }

    @Test
    public void testCompareValuesWithNonNullPreviousAndNullValue() {
        assertTrue(uiInput.compareValues("test", null));
    }

    @Test
    public void testCompareValuesWithEqualStrings() {
        assertFalse(uiInput.compareValues("test", "test"));
    }

    @Test
    public void testCompareValuesWithDifferentStrings() {
        assertTrue(uiInput.compareValues("test", "other"));
    }

    @Test
    public void testCompareValuesWithComparableObjects() {
        assertTrue(uiInput.compareValues(1, 2));
    }

    @Test
    public void testCompareValuesWithComparableObjectsEqual() {
        assertFalse(uiInput.compareValues(1, 1));
    }

    @Test
    public void testCompareValuesWithNonComparableObjects() {
        assertTrue(uiInput.compareValues(new Object(), new Object()));
    }

    @Test
    public void testConsiderEmptyStringNull() {
        when(facesContext.getExternalContext()).thenReturn(mock(javax.faces.context.ExternalContext.class));
        when(facesContext.getExternalContext().getInitParameter(UIInput.EMPTY_STRING_AS_NULL_PARAM_NAME))
                .thenReturn("true");
        assertTrue(uiInput.considerEmptyStringNull(facesContext));
    }

    @Test
    public void testValidateEmptyFieldsAuto() {
        when(facesContext.getExternalContext()).thenReturn(mock(javax.faces.context.ExternalContext.class));
        when(facesContext.getExternalContext().getInitParameter(UIInput.VALIDATE_EMPTY_FIELDS_PARAM_NAME))
                .thenReturn("auto");
        when(facesContext.getExternalContext().getApplicationMap()).thenReturn(new java.util.HashMap<>());
        // This will return false since BeanValidator is not available in test environment
        assertFalse(uiInput.validateEmptyFields(facesContext));
    }

    @Test
    public void testValidateEmptyFieldsTrue() {
        when(facesContext.getExternalContext()).thenReturn(mock(javax.faces.context.ExternalContext.class));
        when(facesContext.getExternalContext().getInitParameter(UIInput.VALIDATE_EMPTY_FIELDS_PARAM_NAME))
                .thenReturn("true");
        assertTrue(uiInput.validateEmptyFields(facesContext));
    }

    @Test
    public void testValidateEmptyFieldsFalse() {
        when(facesContext.getExternalContext()).thenReturn(mock(javax.faces.context.ExternalContext.class));
        when(facesContext.getExternalContext().getInitParameter(UIInput.VALIDATE_EMPTY_FIELDS_PARAM_NAME))
                .thenReturn("false");
        assertFalse(uiInput.validateEmptyFields(facesContext));
    }

    @Test
    public void testIsBeansValidationAvailable() {
        when(facesContext.getExternalContext()).thenReturn(mock(javax.faces.context.ExternalContext.class));
        when(facesContext.getExternalContext().getApplicationMap()).thenReturn(new java.util.HashMap<>());
        // This will return false since BeanValidator is not available in test environment
        assertFalse(uiInput.isBeansValidationAvailable(facesContext));
    }

    @Test
    public void testGetConverterWithTypeReturnsNullWhenNoConverter() {
        when(facesContext.getApplication()).thenReturn(mock(javax.faces.application.Application.class));
        when(facesContext.getELContext()).thenReturn(mock(javax.el.ELContext.class));
        when(facesContext.getApplication().createConverter(any(Class.class))).thenReturn(null);
        assertNull(uiInput.getConverterWithType(facesContext));
    }

    @Test
    public void testGetConvertedValueWithRenderer() {
        javax.faces.render.Renderer renderer = mock(javax.faces.render.Renderer.class);
        when(renderer.getConvertedValue(any(FacesContext.class), any(UIComponent.class), any(Object.class)))
                .thenReturn("convertedValue");
        uiInput.setRenderer(renderer);
        assertEquals("convertedValue", uiInput.getConvertedValue(facesContext, "inputValue"));
    }

    @Test
    public void testGetConvertedValueWithConverter() throws ConverterException {
        Converter converter = mock(Converter.class);
        when(converter.getAsObject(any(FacesContext.class), any(UIComponent.class), any(String.class)))
                .thenReturn("convertedValue");
        uiInput.setConverter(converter);
        assertEquals("convertedValue", uiInput.getConvertedValue(facesContext, "inputValue"));
    }

    @Test
    public void testValidateValueWithRequiredAndEmptyValue() {
        uiInput.setRequired(true);
        uiInput.setValid(true);
        uiInput.validateValue(facesContext, null);
        assertFalse(uiInput.isValid());
    }

    @Test
    public void testValidateValueWithRequiredAndNonEmptyValue() {
        uiInput.setRequired(true);
        uiInput.setValid(true);
        uiInput.validateValue(facesContext, "value");
        assertTrue(uiInput.isValid());
    }

    @Test
    public void testValidateValueWithValidator() throws ValidatorException {
        Validator validator = mock(Validator.class);
        uiInput.addValidator(validator);
        uiInput.setValid(true);
        uiInput.validateValue(facesContext, "value");
        verify(validator).validate(any(FacesContext.class), any(UIComponent.class), any(Object.class));
    }

    @Test
    public void testValidateValueWithValidatorException() throws ValidatorException {
        Validator validator = mock(Validator.class);
        doThrow(new ValidatorException(new FacesMessage())).when(validator).validate(any(FacesContext.class), any(UIComponent.class), any(Object.class));
        uiInput.addValidator(validator);
        uiInput.setValid(true);
        uiInput.validateValue(facesContext, "value");
        assertFalse(uiInput.isValid());
    }
}
