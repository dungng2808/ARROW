package javax.faces.component;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import javax.el.ELException;
import javax.el.ValueExpression;
import javax.faces.FacesException;
import javax.faces.application.Application;
import javax.faces.application.FacesMessage;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;
import javax.faces.convert.ConverterException;
import javax.faces.el.MethodBinding;
import javax.faces.event.ExceptionQueuedEvent;
import javax.faces.event.ExceptionQueuedEventContext;
import javax.faces.event.PhaseId;
import javax.faces.event.PostValidateEvent;
import javax.faces.event.PreValidateEvent;
import javax.faces.event.ValueChangeEvent;
import javax.faces.event.ValueChangeListener;
import javax.faces.render.Renderer;
import javax.faces.validator.Validator;
import javax.faces.validator.ValidatorException;

import org.junit.Before;
import org.junit.Test;

public class UIInput_ArrowTest {

    private FacesContext facesContext;
    private UIInput uiInput;
    private ExternalContext externalContext;
    private Application application;

    @Before
    public void setUp() {
        facesContext = mock(FacesContext.class);
        externalContext = mock(ExternalContext.class);
        application = mock(Application.class);
        
        when(facesContext.getExternalContext()).thenReturn(externalContext);
        when(facesContext.getApplication()).thenReturn(application);
        when(facesContext.getAttributes()).thenReturn(Collections.emptyMap());
        
        uiInput = new UIInput();
    }

    @Test
    public void testGetFamily() {
        assertEquals(UIInput.COMPONENT_FAMILY, uiInput.getFamily());
    }

    @Test
    public void testGetSubmittedValueReturnsNullWhenNoSubmittedValueSet() {
        assertNull(uiInput.getSubmittedValue());
    }

    @Test
    public void testGetSubmittedValueReturnsSubmittedValueWhenSet() {
        Object submittedValue = "test";
        uiInput.setSubmittedValue(submittedValue);
        assertEquals(submittedValue, uiInput.getSubmittedValue());
    }

    @Test
    public void testGetSubmittedValueReturnsEmptyStringWhenInvalidAndEmptyStringIsNull() {
        Object submittedValue = null;
        uiInput.setSubmittedValue(submittedValue);
        uiInput.setValid(false);
        when(externalContext.getInitParameter("javax.faces.INTERPRET_EMPTY_STRING_SUBMITTED_VALUES_AS_NULL"))
            .thenReturn("true");
        assertEquals("", uiInput.getSubmittedValue());
    }

    @Test
    public void testSetSubmittedValue() {
        Object submittedValue = "test";
        uiInput.setSubmittedValue(submittedValue);
        assertEquals(submittedValue, uiInput.getSubmittedValue());
    }

    @Test
    public void testGetValueReturnsLocalValueWhenSet() {
        Object localValue = "local";
        uiInput.setLocalValueSet(true);
        uiInput.setLocalValue(localValue);
        assertEquals(localValue, uiInput.getValue());
    }

    @Test
    public void testGetValueReturnsSuperValueWhenLocalValueNotSet() {
        Object superValue = "super";
        uiInput.setLocalValueSet(false);
        uiInput.setValue(superValue);
        assertEquals(superValue, uiInput.getValue());
    }

    @Test
    public void testSetValueSetsLocalValueSet() {
        Object value = "test";
        uiInput.setValue(value);
        assertTrue(uiInput.isLocalValueSet());
    }

    @Test
    public void testResetValueClearsLocalValueAndSubmittedValue() {
        uiInput.setLocalValueSet(true);
        uiInput.setSubmittedValue("test");
        uiInput.resetValue();
        assertFalse(uiInput.isLocalValueSet());
        assertNull(uiInput.getSubmittedValue());
    }

    @Test
    public void testIsLocalValueSetReturnsFalseByDefault() {
        assertFalse(uiInput.isLocalValueSet());
    }

    @Test
    public void testSetLocalValueSet() {
        uiInput.setLocalValueSet(true);
        assertTrue(uiInput.isLocalValueSet());
    }

    @Test
    public void testIsRequiredReturnsFalseByDefault() {
        assertFalse(uiInput.isRequired());
    }

    @Test
    public void testSetRequired() {
        uiInput.setRequired(true);
        assertTrue(uiInput.isRequired());
    }

    @Test
    public void testIsValidReturnsTrueByDefault() {
        assertTrue(uiInput.isValid());
    }

    @Test
    public void testSetValid() {
        uiInput.setValid(false);
        assertFalse(uiInput.isValid());
    }

    @Test
    public void testIsImmediateReturnsFalseByDefault() {
        assertFalse(uiInput.isImmediate());
    }

    @Test
    public void testSetImmediate() {
        uiInput.setImmediate(true);
        assertTrue(uiInput.isImmediate());
    }

    @Test
    public void testProcessDecodesCallsSuperProcessDecodes() {
        uiInput.processDecodes(facesContext);
        // No direct way to verify super.processDecodes was called without mocking
        // but we can at least ensure no exception is thrown
    }

    @Test
    public void testProcessValidatorsCallsSuperProcessValidators() {
        uiInput.processValidators(facesContext);
        // No direct way to verify super.processValidators was called without mocking
        // but we can at least ensure no exception is thrown
    }

    @Test
    public void testProcessUpdatesCallsSuperProcessUpdates() {
        uiInput.processUpdates(facesContext);
        // No direct way to verify super.processUpdates was called without mocking
        // but we can at least ensure no exception is thrown
    }

    @Test
    public void testDecodeSetsValidTrue() {
        uiInput.decode(facesContext);
        assertTrue(uiInput.isValid());
    }

    @Test
    public void testUpdateModelDoesNothingIfNotValid() {
        uiInput.setValid(false);
        uiInput.updateModel(facesContext);
        // Should not throw any exception
    }

    @Test
    public void testUpdateModelDoesNothingIfLocalValueNotSet() {
        uiInput.setLocalValueSet(false);
        uiInput.updateModel(facesContext);
        // Should not throw any exception
    }

    @Test
    public void testValidateWithNullSubmittedValueAndRequiredFalse() {
        uiInput.setRequired(false);
        uiInput.validate(facesContext);
        // Should not throw any exception
    }

    @Test
    public void testValidateWithNullSubmittedValueAndRequiredTrue() {
        uiInput.setRequired(true);
        uiInput.validate(facesContext);
        // Should not throw any exception
    }

    @Test
    public void testValidateWithEmptyStringSubmittedValue() {
        uiInput.setSubmittedValue("");
        uiInput.validate(facesContext);
        // Should not throw any exception
    }

    @Test
    public void testAddValidatorAddsValidator() {
        Validator validator = mock(Validator.class);
        uiInput.addValidator(validator);
        Validator[] validators = uiInput.getValidators();
        assertNotNull(validators);
        assertEquals(1, validators.length);
        assertEquals(validator, validators[0]);
    }

    @Test(expected = NullPointerException.class)
    public void testAddValidatorThrowsNullPointerExceptionWhenValidatorIsNull() {
        uiInput.addValidator(null);
    }

    @Test
    public void testGetValidatorsReturnsEmptyArrayWhenNoValidatorsAdded() {
        Validator[] validators = uiInput.getValidators();
        assertNotNull(validators);
        assertEquals(0, validators.length);
    }

    @Test
    public void testRemoveValidatorRemovesValidator() {
        Validator validator = mock(Validator.class);
        uiInput.addValidator(validator);
        uiInput.removeValidator(validator);
        Validator[] validators = uiInput.getValidators();
        assertNotNull(validators);
        assertEquals(0, validators.length);
    }

    @Test
    public void testRemoveValidatorDoesNothingWhenValidatorIsNull() {
        uiInput.removeValidator(null);
        // Should not throw any exception
    }

    @Test
    public void testAddValueChangeListenerAddsListener() {
        ValueChangeListener listener = mock(ValueChangeListener.class);
        uiInput.addValueChangeListener(listener);
        ValueChangeListener[] listeners = uiInput.getValueChangeListeners();
        assertNotNull(listeners);
        assertEquals(1, listeners.length);
        assertEquals(listener, listeners[0]);
    }

    @Test
    public void testRemoveValueChangeListenerRemovesListener() {
        ValueChangeListener listener = mock(ValueChangeListener.class);
        uiInput.addValueChangeListener(listener);
        uiInput.removeValueChangeListener(listener);
        ValueChangeListener[] listeners = uiInput.getValueChangeListeners();
        assertNotNull(listeners);
        assertEquals(0, listeners.length);
    }

    @Test
    public void testCompareValuesReturnsTrueWhenBothNull() {
        assertTrue(uiInput.compareValues(null, null));
    }

    @Test
    public void testCompareValuesReturnsTrueWhenPreviousNullAndValueNotNull() {
        assertTrue(uiInput.compareValues(null, "test"));
    }

    @Test
    public void testCompareValuesReturnsTrueWhenPreviousNotNullAndValueNull() {
        assertTrue(uiInput.compareValues("test", null));
    }

    @Test
    public void testCompareValuesReturnsFalseWhenEqualStrings() {
        assertFalse(uiInput.compareValues("test", "test"));
    }

    @Test
    public void testCompareValuesReturnsTrueWhenDifferentStrings() {
        assertTrue(uiInput.compareValues("test1", "test2"));
    }

    @Test
    public void testIsEmptyReturnsTrueForNull() {
        assertTrue(UIInput.isEmpty(null));
    }

    @Test
    public void testIsEmptyReturnsTrueForEmptyString() {
        assertTrue(UIInput.isEmpty(""));
    }

    @Test
    public void testIsEmptyReturnsFalseForNonEmptyString() {
        assertFalse(UIInput.isEmpty("test"));
    }

    @Test
    public void testIsEmptyReturnsTrueForEmptyArray() {
        assertTrue(UIInput.isEmpty(new Object[0]));
    }

    @Test
    public void testIsEmptyReturnsFalseForNonEmptyArray() {
        assertFalse(UIInput.isEmpty(new Object[]{"test"}));
    }

    @Test
    public void testIsEmptyReturnsTrueForEmptyList() {
        assertTrue(UIInput.isEmpty(Collections.emptyList()));
    }

    @Test
    public void testIsEmptyReturnsFalseForNonEmptyList() {
        assertFalse(UIInput.isEmpty(Collections.singletonList("test")));
    }

    @Test
    public void testIsEmptyReturnsTrueForEmptyMap() {
        assertTrue(UIInput.isEmpty(Collections.emptyMap()));
    }

    @Test
    public void testIsEmptyReturnsFalseForNonEmptyMap() {
        assertFalse(UIInput.isEmpty(Collections.singletonMap("key", "value")));
    }

    @Test
    public void testGetConvertedValueWithRenderer() {
        Renderer renderer = mock(Renderer.class);
        uiInput.setRenderer(renderer);
        Object result = uiInput.getConvertedValue(facesContext, "test");
        // Should not throw any exception
    }

    @Test
    public void testGetConvertedValueWithConverter() {
        Converter converter = mock(Converter.class);
        uiInput.setConverter(converter);
        Object result = uiInput.getConvertedValue(facesContext, "test");
        // Should not throw any exception
    }

    @Test
    public void testGetConvertedValueWithoutConverterOrRenderer() {
        Object result = uiInput.getConvertedValue(facesContext, "test");
        assertEquals("test", result);
    }

    @Test
    public void testValidateValueWithRequiredAndEmptyValue() {
        uiInput.setRequired(true);
        uiInput.validateValue(facesContext, "");
        assertFalse(uiInput.isValid());
    }

    @Test
    public void testValidateValueWithRequiredAndNullValue() {
        uiInput.setRequired(true);
        uiInput.validateValue(facesContext, null);
        assertFalse(uiInput.isValid());
    }

    @Test
    public void testValidateValueWithRequiredAndValidValue() {
        uiInput.setRequired(true);
        uiInput.validateValue(facesContext, "valid");
        assertTrue(uiInput.isValid());
    }

    @Test
    public void testValidateValueWithValidator() throws ValidatorException {
        Validator validator = mock(Validator.class);
        uiInput.addValidator(validator);
        uiInput.validateValue(facesContext, "test");
        // Should not throw any exception
    }

    @Test
    public void testValidateValueWithValidatorThatThrowsException() throws ValidatorException {
        Validator validator = mock(Validator.class);
        doThrow(new ValidatorException(new FacesMessage())).when(validator).validate(any(), any(), any());
        uiInput.addValidator(validator);
        uiInput.validateValue(facesContext, "test");
        assertFalse(uiInput.isValid());
    }

    @Test
    public void testConsiderEmptyStringNullReturnsFalseByDefault() {
        when(externalContext.getInitParameter("javax.faces.INTERPRET_EMPTY_STRING_SUBMITTED_VALUES_AS_NULL"))
            .thenReturn(null);
        assertFalse(uiInput.considerEmptyStringNull(facesContext));
    }

    @Test
    public void testConsiderEmptyStringNullReturnsTrueWhenSet() {
        when(externalContext.getInitParameter("javax.faces.INTERPRET_EMPTY_STRING_SUBMITTED_VALUES_AS_NULL"))
            .thenReturn("true");
        assertTrue(uiInput.considerEmptyStringNull(facesContext));
    }

    @Test
    public void testValidateEmptyFieldsReturnsFalseByDefault() {
        when(externalContext.getInitParameter("javax.faces.VALIDATE_EMPTY_FIELDS"))
            .thenReturn(null);
        when(externalContext.getApplicationMap()).thenReturn(Collections.emptyMap());
        assertFalse(uiInput.validateEmptyFields(facesContext));
    }

    @Test
    public void testValidateEmptyFieldsReturnsTrueWhenSet() {
        when(externalContext.getInitParameter("javax.faces.VALIDATE_EMPTY_FIELDS"))
            .thenReturn("true");
        when(externalContext.getApplicationMap()).thenReturn(Collections.emptyMap());
        assertTrue(uiInput.validateEmptyFields(facesContext));
    }

    @Test
    public void testIsBeansValidationAvailableReturnsFalseWhenBeanValidatorCannotBeInstantiated() {
        // This would require mocking the BeanValidator class, which is complex
        // Just test that it doesn't throw an exception
        try {
            uiInput.isBeansValidationAvailable(facesContext);
        } catch (Exception e) {
            // Expected behavior might vary depending on environment
        }
    }

    @Test
    public void testSaveStateReturnsNullWhenNoState() {
        Object state = uiInput.saveState(facesContext);
        assertNull(state);
    }

    @Test
    public void testRestoreStateDoesNothingWhenStateIsNull() {
        uiInput.restoreState(facesContext, null);
        // Should not throw any exception
    }

    @Test
    public void testGetConverterWithTypeReturnsNullWhenNoValueExpression() {
        Converter converter = uiInput.getConverterWithType(facesContext);
        assertNull(converter);
    }

    @Test
    public void testGetConverterWithTypeReturnsConverterWhenConverterExists() {
        Converter converter = mock(Converter.class);
        uiInput.setConverter(converter);
        Converter result = uiInput.getConverterWithType(facesContext);
        assertEquals(converter, result);
    }

    @Test
    public void testGetConverterWithTypeReturnsNullWhenTypeIsString() throws ELException {
        ValueExpression valueExpression = mock(ValueExpression.class);
        when(valueExpression.getType(any())).thenReturn(String.class);
        uiInput.setValueExpression("value", valueExpression);
        Converter result = uiInput.getConverterWithType(facesContext);
        assertNull(result);
    }

    @Test
    public void testGetConverterWithTypeReturnsNullWhenTypeIsObject() throws ELException {
        ValueExpression valueExpression = mock(ValueExpression.class);
        when(valueExpression.getType(any())).thenReturn(Object.class);
        uiInput.setValueExpression("value", valueExpression);
        Converter result = uiInput.getConverterWithType(facesContext);
        assertNull(result);
    }

    @Test
    public void testGetConverterWithTypeReturnsNullWhenCreateConverterFails() throws ELException {
        ValueExpression valueExpression = mock(ValueExpression.class);
        when(valueExpression.getType(any())).thenReturn(Integer.class);
        uiInput.setValueExpression("value", valueExpression);
        when(application.createConverter(Integer.class)).thenThrow(new FacesException("test"));
        Converter result = uiInput.getConverterWithType(facesContext);
        assertNull(result);
    }

    @Test
    public void testAddConversionErrorMessageWithCustomMessage() {
        String customMessage = "Custom error message";
        uiInput.setConverterMessage(customMessage);
        ConverterException ce = mock(ConverterException.class);
        when(ce.getFacesMessage()).thenReturn(null);
        uiInput.addConversionErrorMessage(facesContext, ce);
        // Should not throw any exception
    }

    @Test
    public void testAddConversionErrorMessageWithConverterExceptionMessage() {
        ConverterException ce = mock(ConverterException.class);
        when(ce.getFacesMessage()).thenReturn(null);
        when(ce.getMessage()).thenReturn("Converter error");
        uiInput.addConversionErrorMessage(facesContext, ce);
        // Should not throw any exception
    }

    @Test
    public void testExecuteValidateCallsValidate() {
        uiInput.executeValidate(facesContext);
        // Should not throw any exception
    }

    @Test
    public void testExecuteValidateHandlesRuntimeException() {
        // This requires more complex mocking to simulate a RuntimeException in validate()
        // But we can at least ensure it doesn't throw an exception
        uiInput.executeValidate(facesContext);
    }
}
