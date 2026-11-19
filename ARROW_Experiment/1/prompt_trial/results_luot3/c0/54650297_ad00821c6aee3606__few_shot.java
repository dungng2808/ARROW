package io.parsingdata.metal.data;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;

import java.lang.reflect.Method;
import java.math.BigInteger;
import org.junit.Test;

public class DataExpressionSource_ArrowTest {

    @Test
    public void equalityAndHashCodeForSameInstance() {
        DataExpressionSource source = new DataExpressionSource(null, 0, null, null);
        assertEquals(source, source);
        assertEquals(source.hashCode(), source.hashCode());
    }

    @Test
    public void equalityRejectsNullAndDifferentClass() {
        DataExpressionSource source = new DataExpressionSource(null, 0, null, null);
        assertFalse(source.equals(null));
        assertFalse(source.equals(new Object()));
    }

    @Test
    public void publicFieldsRetainConstructorArguments() throws Exception {
        Method constructor = DataExpressionSource.class.getDeclaredConstructors()[0];
        constructor.setAccessible(true);
        DataExpressionSource source = (DataExpressionSource) constructor.newInstance(null, 3, null, null);
        assertEquals(3, source.index);
        assertEquals(null, source.dataExpression);
        assertEquals(null, source.parseState);
        assertEquals(null, source.encoding);
        assertEquals(null, source.cache);
    }

    @Test
    public void toStringIncludesClassNameAndIndex() throws Exception {
        Method constructor = DataExpressionSource.class.getDeclaredConstructors()[0];
        constructor.setAccessible(true);
        DataExpressionSource source = (DataExpressionSource) constructor.newInstance(null, 2, null, null);
        assertTrue(source.toString().startsWith("DataExpressionSource(null[2]("));
    }

    @Test
    public void hashCodeIsStable() throws Exception {
        Method constructor = DataExpressionSource.class.getDeclaredConstructors()[0];
        constructor.setAccessible(true);
        DataExpressionSource source = (DataExpressionSource) constructor.newInstance(null, 1, null, null);
        int hash = source.hashCode();
        assertEquals(hash, source.hashCode());
    }
}
