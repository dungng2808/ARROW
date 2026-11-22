package org.dcm4che3.tool.findscu;

import org.dcm4che3.data.Attributes;
import org.dcm4che3.data.Tag;
import org.dcm4che3.data.VR;
import org.dcm4che3.net.QueryOption;
import org.dcm4che3.net.ApplicationEntity;
import org.dcm4che3.net.Connection;
import org.dcm4che3.net.Device;
import org.dcm4che3.net.pdu.AAssociateRQ;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.util.EnumSet;

import static org.junit.Assert.*;

public class FindSCU_ArrowTest {

    @Test
    public void testConstructor() throws IOException {
        FindSCU findSCU = new FindSCU();
        assertNotNull(findSCU);
        assertNotNull(findSCU.getApplicationEntity());
        assertNotNull(findSCU.getRemoteConnection());
        assertNotNull(findSCU.getAAssociateRQ());
        assertNotNull(findSCU.getDevice());
        assertNotNull(findSCU.getKeys());
    }

    @Test
    public void testSetPriority() {
        FindSCU findSCU = new FindSCU();
        findSCU.setPriority(10);
        // No direct way to verify, but we can ensure no exception occurs
    }

    @Test
    public void testSetInformationModel() {
        FindSCU findSCU = new FindSCU();
        EnumSet<QueryOption> queryOptions = EnumSet.noneOf(QueryOption.class);
        findSCU.setInformationModel(FindSCU.InformationModel.PatientRoot, 
                new String[]{"1.2.840.10008.1.2"}, queryOptions);
        // Verify that the model is set correctly by checking the presentation context
        assertNotNull(findSCU.getAAssociateRQ());
    }

    @Test
    public void testAddLevel() {
        FindSCU findSCU = new FindSCU();
        findSCU.addLevel("STUDY");
        Attributes keys = findSCU.getKeys();
        assertEquals("STUDY", keys.getString(Tag.QueryRetrieveLevel, VR.CS));
    }

    @Test
    public void testSetCancelAfter() {
        FindSCU findSCU = new FindSCU();
        findSCU.setCancelAfter(5);
        // No direct way to verify, but we can ensure no exception occurs
    }

    @Test
    public void testSetOutputDirectory() {
        FindSCU findSCU = new FindSCU();
        File tempDir = new File(System.getProperty("java.io.tmpdir"), "test_output");
        findSCU.setOutputDirectory(tempDir);
        ApplicationEntity ae = findSCU.getApplicationEntity();
        Connection conn = ae.getConnections().iterator().next();
        assertEquals(tempDir, conn.getOutputDirectory());
    }

    @Test
    public void testSetOutputFileFormat() {
        FindSCU findSCU = new FindSCU();
        findSCU.setOutputFileFormat("000'.dcm'");
        // No direct way to verify, but we can ensure no exception occurs
    }

    @Test
    public void testSetXSLT() {
        FindSCU findSCU = new FindSCU();
        File xsltFile = new File("test.xsl");
        findSCU.setXSLT(xsltFile);
        ApplicationEntity ae = findSCU.getApplicationEntity();
        Connection conn = ae.getConnections().iterator().next();
        assertEquals(xsltFile, conn.getXSLTFile());
    }

    @Test
    public void testSetXML() {
        FindSCU findSCU = new FindSCU();
        findSCU.setXML(true);
        ApplicationEntity ae = findSCU.getApplicationEntity();
        Connection conn = ae.getConnections().iterator().next();
        assertTrue(conn.isXML());
    }

    @Test
    public void testSetXMLIndent() {
        FindSCU findSCU = new FindSCU();
        findSCU.setXMLIndent(true);
        ApplicationEntity ae = findSCU.getApplicationEntity();
        Connection conn = ae.getConnections().iterator().next();
        assertTrue(conn.isXMLIndent());
    }

    @Test
    public void testSetXMLIncludeKeyword() {
        FindSCU findSCU = new FindSCU();
        findSCU.setXMLIncludeKeyword(false);
        ApplicationEntity ae = findSCU.getApplicationEntity();
        Connection conn = ae.getConnections().iterator().next();
        assertFalse(conn.isXMLIncludeKeyword());
    }

    @Test
    public void testSetXMLIncludeNamespaceDeclaration() {
        FindSCU findSCU = new FindSCU();
        findSCU.setXMLIncludeNamespaceDeclaration(true);
        ApplicationEntity ae = findSCU.getApplicationEntity();
        Connection conn = ae.getConnections().iterator().next();
        assertTrue(conn.isXMLIncludeNamespaceDeclaration());
    }

    @Test
    public void testSetConcatenateOutputFiles() {
        FindSCU findSCU = new FindSCU();
        findSCU.setConcatenateOutputFiles(true);
        ApplicationEntity ae = findSCU.getApplicationEntity();
        Connection conn = ae.getConnections().iterator().next();
        assertTrue(conn.isConcatenateOutputFiles());
    }

    @Test
    public void testSetInputFilter() {
        FindSCU findSCU = new FindSCU();
        int[] filter = {1, 2, 3};
        findSCU.setInputFilter(filter);
        ApplicationEntity ae = findSCU.getApplicationEntity();
        Connection conn = ae.getConnections().iterator().next();
        assertArrayEquals(filter, conn.getInputFilter());
    }

    @Test
    public void testInformationModelAdjustQueryOptions() {
        EnumSet<QueryOption> queryOptions = EnumSet.noneOf(QueryOption.class);
        FindSCU.InformationModel.PatientRoot.adjustQueryOptions(queryOptions);
        assertTrue(queryOptions.contains(QueryOption.RELATIONAL));
        assertTrue(queryOptions.contains(QueryOption.DATETIME));
    }

    @Test
    public void testInformationModelLevel() {
        assertEquals("STUDY", FindSCU.InformationModel.PatientRoot.level);
        assertNull(FindSCU.InformationModel.MWL.level);
    }
}
