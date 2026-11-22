package org.dcm4che3.tool.findscu;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.util.EnumSet;

import org.dcm4che3.data.Attributes;
import org.dcm4che3.data.Tag;
import org.dcm4che3.data.VR;
import org.dcm4che3.net.QueryOption;
import org.dcm4che3.net.pdu.AAssociateRQ;
import org.junit.Test;

public class FindSCU_ArrowTest {

    @Test
    public void constructorCreatesEmptyKeysAndRequest() throws Exception {
        FindSCU findSCU = new FindSCU();

        assertNotNull(findSCU.getKeys());
        assertEquals(0, findSCU.getKeys().size());
        assertNotNull(findSCU.getAAssociateRQ());
    }

    @Test
    public void addLevelStoresQueryRetrieveLevel() throws Exception {
        FindSCU findSCU = new FindSCU();

        findSCU.addLevel("STUDY");

        assertEquals("STUDY", findSCU.getKeys().getString(Tag.QueryRetrieveLevel));
    }

    @Test
    public void setInformationModelAddsDefaultStudyLevel() throws Exception {
        FindSCU findSCU = new FindSCU();

        findSCU.setInformationModel(FindSCU.InformationModel.StudyRoot,
                new String[0], EnumSet.noneOf(QueryOption.class));

        assertEquals("STUDY", findSCU.getKeys().getString(Tag.QueryRetrieveLevel));
        AAssociateRQ request = findSCU.getAAssociateRQ();
        assertNotNull(request);
    }

    @Test
    public void nonHierarchicalModelAddsRelationalAndDateTimeOptions() {
        EnumSet<QueryOption> options = EnumSet.noneOf(QueryOption.class);

        FindSCU.InformationModel.MWL.adjustQueryOptions(options);

        assertTrue(options.contains(QueryOption.RELATIONAL));
        assertTrue(options.contains(QueryOption.DATETIME));
        assertEquals(2, options.size());
    }

    @Test
    public void hierarchicalModelLeavesQueryOptionsUnchanged() {
        EnumSet<QueryOption> options = EnumSet.noneOf(QueryOption.class);
        options.add(QueryOption.FUZZY);

        FindSCU.InformationModel.StudyRoot.adjustQueryOptions(options);

        assertEquals(EnumSet.of(QueryOption.FUZZY), options);
    }

    @Test
    public void gettersReturnConfiguredObjects() throws Exception {
        FindSCU findSCU = new FindSCU();

        assertNotNull(findSCU.getApplicationEntity());
        assertNotNull(findSCU.getRemoteConnection());
        assertNotNull(findSCU.getDevice());
    }

    @Test
    public void setOutputDirectoryCreatesDirectory() throws Exception {
        File directory = new File(System.getProperty("java.io.tmpdir"),
                "findscu-arrow-test-output");

        FindSCU findSCU = new FindSCU();
        findSCU.setOutputDirectory(directory);

        assertTrue(directory.isDirectory());
        directory.delete();
    }
}
