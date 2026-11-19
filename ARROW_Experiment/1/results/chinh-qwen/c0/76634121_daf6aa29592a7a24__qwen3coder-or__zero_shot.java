package org.apache.qpid.proton.codec;

import static org.junit.Assert.*;
import org.junit.Test;

public class StringType_ArrowTest {

    @Test
    public void testGetTypeClass() {
        StringType stringType = new StringType(null, null);
        assertEquals(String.class, stringType.getTypeClass());
    }

    @Test
    public void testCalculateUTF8Length_ASCII() {
        assertEquals(5, StringType.calculateUTF8Length("Hello"));
    }

    @Test
    public void testCalculateUTF8Length_UTF8() {
        assertEquals(6, StringType.calculateUTF8Length("Héllo"));
    }

    @Test
    public void testCalculateUTF8Length_Empty() {
        assertEquals(0, StringType.calculateUTF8Length(""));
    }

    @Test
    public void testExtendedCalculateUTF8Length() {
        String testString = "Héllo";
        int result = StringType.extendedCalculateUTF8Length(testString, 1, testString.length(), 5);
        assertEquals(6, result);
    }

    @Test
    public void testGetCanonicalEncoding() {
        StringType stringType = new StringType(null, null);
        assertNotNull(stringType.getCanonicalEncoding());
    }

    @Test
    public void testGetAllEncodings() {
        StringType stringType = new StringType(null, null);
        assertNotNull(stringType.getAllEncodings());
        assertEquals(2, stringType.getAllEncodings().size());
    }
}
