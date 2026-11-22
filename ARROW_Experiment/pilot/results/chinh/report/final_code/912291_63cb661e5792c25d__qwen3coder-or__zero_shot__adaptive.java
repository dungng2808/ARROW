package org.dcm4che3.tool.findscu;

import org.junit.Test;
import org.junit.Before;
import static org.junit.Assert.*;
import java.io.File;
import java.io.IOException;
import java.util.EnumSet;
import java.util.ResourceBundle;

public class FindSCU_ArrowTest {

    private FindSCU findSCU;

    @Before
    public void setUp() throws IOException {
        findSCU = new FindSCU();
    }

    @Test
    public void testConstructor() throws IOException {
        assertNotNull(findSCU);
        assertNotNull(findSCU.getApplicationEntity());
        assertNotNull(findSCU.getRemoteConnection());
        assertNotNull(findSCU.getAAssociateRQ());
        assertNotNull(findSCU.getDevice());
        assertNotNull(findSCU.getKeys());
    }

    @Test
    public void testSetPriority() {
        int priority = 1;
        findSCU.setPriority(priority);
        // No direct way to verify, but we can ensure no exception occurs
    }

    @Test
    public void testSetInformationModel() {
        String[] tss = {"1.2.840.10008.1.2"};
        EnumSet<org.dcm4che3.net.QueryOption> queryOptions = EnumSet.noneOf(org.dcm4che3.net.QueryOption.class);
        
        findSCU.setInformationModel(FindSCU.InformationModel.PatientRoot, tss, queryOptions);
        // No direct way to verify, but we can ensure no exception occurs
    }

    @Test
    public void testAddLevel() {
        String level = "STUDY";
        findSCU.addLevel(level);
        assertEquals(level, findSCU.getKeys().getString(org.dcm4che3.data.Tag.QueryRetrieveLevel, null));
    }

    @Test
    public void testSetCancelAfter() {
        int cancelAfter = 5;
        findSCU.setCancelAfter(cancelAfter);
        // No direct way to verify, but we can ensure no exception occurs
    }

    @Test
    public void testSetOutputDirectory() {
        File outDir = new File("test_output");
        findSCU.setOutputDirectory(outDir);
        // No direct way to verify, but we can ensure no exception occurs
    }

    @Test
    public void testSetOutputFileFormat() {
        String outFileFormat = "000'.dcm'";
        findSCU.setOutputFileFormat(outFileFormat);
        // No direct way to verify, but we can ensure no exception occurs
    }

    @Test
    public void testSetXSLT() {
        File xsltFile = new File("test.xsl");
        findSCU.setXSLT(xsltFile);
        // No direct way to verify, but we can ensure no exception occurs
    }

    @Test
    public void testSetXML() {
        findSCU.setXML(true);
        // No direct way to verify, but we can ensure no exception occurs
    }

    @Test
    public void testSetXMLIndent() {
        findSCU.setXMLIndent(true);
        // No direct way to verify, but we can ensure no exception occurs
    }

    @Test
    public void testSetXMLIncludeKeyword() {
        findSCU.setXMLIncludeKeyword(false);
        // No direct way to verify, but we can ensure no exception occurs
    }

    @Test
    public void testSetXMLIncludeNamespaceDeclaration() {
        findSCU.setXMLIncludeNamespaceDeclaration(true);
        // No direct way to verify, but we can ensure no exception occurs
    }

    @Test
    public void testSetConcatenateOutputFiles() {
        findSCU.setConcatenateOutputFiles(true);
        // No direct way to verify, but we can ensure no exception occurs
    }

    @Test
    public void testSetInputFilter() {
        int[] inFilter = {1, 2, 3};
        findSCU.setInputFilter(inFilter);
        // No direct way to verify, but we can ensure no exception occurs
    }

    @Test
    public void testInformationModelEnum() {
        FindSCU.InformationModel[] models = FindSCU.InformationModel.values();
        assertNotNull(models);
        assertTrue(models.length > 0);
        
        for (FindSCU.InformationModel model : models) {
            assertNotNull(model.cuid);
            assertNotNull(model.level);
        }
    }

    @Test
    public void testInformationModelAdjustQueryOptions() {
        EnumSet<org.dcm4che3.net.QueryOption> queryOptions = EnumSet.noneOf(org.dcm4che3.net.QueryOption.class);
        FindSCU.InformationModel.PatientRoot.adjustQueryOptions(queryOptions);
        // Verify that RELATIONAL and DATETIME are added for models with null level
        assertTrue(queryOptions.contains(org.dcm4che3.net.QueryOption.RELATIONAL));
        assertTrue(queryOptions.contains(org.dcm4che3.net.QueryOption.DATETIME));
    }

    @Test
    public void testMergeKeys() {
        org.dcm4che3.data.Attributes keys = new org.dcm4che3.data.Attributes();
        keys.setString(org.dcm4che3.data.Tag.PatientName, org.dcm4che3.data.VR.PN, "Test^Patient");
        
        org.dcm4che3.data.Attributes attrs = new org.dcm4che3.data.Attributes();
        attrs.setString(org.dcm4che3.data.Tag.PatientID, org.dcm4che3.data.VR.LO, "12345");
        
        FindSCU.mergeKeys(attrs, keys);
        
        assertEquals("Test^Patient", attrs.getString(org.dcm4che3.data.Tag.PatientName, null));
        assertEquals("12345", attrs.getString(org.dcm4che3.data.Tag.PatientID, null));
    }
}
