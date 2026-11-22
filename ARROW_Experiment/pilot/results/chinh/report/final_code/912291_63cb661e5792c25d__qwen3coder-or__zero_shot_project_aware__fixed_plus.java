package org.dcm4che3.tool.findscu;

import static org.junit.Assert.*;

import java.io.File;
import java.io.IOException;
import java.util.EnumSet;
import java.util.ResourceBundle;

import org.dcm4che3.data.Attributes;
import org.dcm4che3.data.Tag;
import org.dcm4che3.data.UID;
import org.dcm4che3.net.ApplicationEntity;
import org.dcm4che3.net.Connection;
import org.dcm4che3.net.Device;
import org.dcm4che3.net.QueryOption;
import org.dcm4che3.net.pdu.AAssociateRQ;
import org.junit.Test;

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
        findSCU.setPriority(1);
        // No direct way to verify, but we can ensure no exception occurs
    }

    @Test
    public void testSetInformationModel() {
        FindSCU findSCU = new FindSCU();
        EnumSet<QueryOption> queryOptions = EnumSet.noneOf(QueryOption.class);
        findSCU.setInformationModel(FindSCU.InformationModel.PatientRoot, 
                new String[]{UID.ImplicitVRLittleEndian}, queryOptions);
        // Verify that the model was set correctly by checking the cuid
        assertEquals(UID.PatientRootQueryRetrieveInformationModelFind, 
                findSCU.getAAssociateRQ().getPresentationContext(1).getAbstractSyntax());
    }

    @Test
    public void testAddLevel() {
        FindSCU findSCU = new FindSCU();
        findSCU.addLevel("STUDY");
        Attributes keys = findSCU.getKeys();
        assertEquals("STUDY", keys.getString(Tag.QueryRetrieveLevel, null));
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
        File dir = new File("test_output_dir");
        findSCU.setOutputDirectory(dir);
        assertEquals(dir, findSCU.getClass().getDeclaredField("outDir").get(findSCU));
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
        assertEquals(xsltFile, findSCU.getClass().getDeclaredField("xsltFile").get(findSCU));
    }

    @Test
    public void testSetXML() {
        FindSCU findSCU = new FindSCU();
        findSCU.setXML(true);
        assertTrue(findSCU.getClass().getDeclaredField("xml").getBoolean(findSCU));
    }

    @Test
    public void testSetXMLIndent() {
        FindSCU findSCU = new FindSCU();
        findSCU.setXMLIndent(true);
        assertTrue(findSCU.getClass().getDeclaredField("xmlIndent").getBoolean(findSCU));
    }

    @Test
    public void testSetXMLIncludeKeyword() {
        FindSCU findSCU = new FindSCU();
        findSCU.setXMLIncludeKeyword(false);
        assertFalse(findSCU.getClass().getDeclaredField("xmlIncludeKeyword").getBoolean(findSCU));
    }

    @Test
    public void testSetXMLIncludeNamespaceDeclaration() {
        FindSCU findSCU = new FindSCU();
        findSCU.setXMLIncludeNamespaceDeclaration(true);
        assertTrue(findSCU.getClass().getDeclaredField("xmlIncludeNamespaceDeclaration").getBoolean(findSCU));
    }

    @Test
    public void testSetConcatenateOutputFiles() {
        FindSCU findSCU = new FindSCU();
        findSCU.setConcatenateOutputFiles(true);
        assertTrue(findSCU.getClass().getDeclaredField("catOut").getBoolean(findSCU));
    }

    @Test
    public void testSetInputFilter() {
        FindSCU findSCU = new FindSCU();
        int[] filter = {Tag.PatientName, Tag.PatientID};
        findSCU.setInputFilter(filter);
        assertArrayEquals(filter, (int[]) findSCU.getClass().getDeclaredField("inFilter").get(findSCU));
    }

    @Test
    public void testInformationModelAdjustQueryOptions() {
        EnumSet<QueryOption> queryOptions = EnumSet.noneOf(QueryOption.class);
        FindSCU.InformationModel model = FindSCU.InformationModel.MWL;
        model.adjustQueryOptions(queryOptions);
        assertTrue(queryOptions.contains(QueryOption.RELATIONAL));
        assertTrue(queryOptions.contains(QueryOption.DATETIME));
    }

    @Test
    public void testInformationModelWithLevel() {
        EnumSet<QueryOption> queryOptions = EnumSet.noneOf(QueryOption.class);
        FindSCU.InformationModel model = FindSCU.InformationModel.PatientRoot;
        model.adjustQueryOptions(queryOptions);
        // Should not add relational or datetime options since level is not null
        assertFalse(queryOptions.contains(QueryOption.RELATIONAL));
        assertFalse(queryOptions.contains(QueryOption.DATETIME));
    }

    @Test
    public void testGetApplicationEntity() {
        FindSCU findSCU = new FindSCU();
        ApplicationEntity ae = findSCU.getApplicationEntity();
        assertNotNull(ae);
        assertEquals("FINDSCU", ae.getAETitle());
    }

    @Test
    public void testGetRemoteConnection() {
        FindSCU findSCU = new FindSCU();
        Connection remote = findSCU.getRemoteConnection();
        assertNotNull(remote);
    }

    @Test
    public void testGetAAssociateRQ() {
        FindSCU findSCU = new FindSCU();
        AAssociateRQ rq = findSCU.getAAssociateRQ();
        assertNotNull(rq);
    }

    @Test
    public void testGetDevice() {
        FindSCU findSCU = new FindSCU();
        Device device = findSCU.getDevice();
        assertNotNull(device);
        assertEquals("findscu", device.getDeviceName());
    }

    @Test
    public void testGetKeys() {
        FindSCU findSCU = new FindSCU();
        Attributes keys = findSCU.getKeys();
        assertNotNull(keys);
    }

    @Test
    public void testMergeKeys() {
        Attributes attrs = new Attributes();
        Attributes keys = new Attributes();
        keys.setString(Tag.PatientName, "Doe^John");
        FindSCU.mergeKeys(attrs, keys);
        assertEquals("Doe^John", attrs.getString(Tag.PatientName));
    }
}
