package org.dcm4che3.tool.findscu;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.io.File;
import java.io.IOException;
import java.util.EnumSet;

import org.dcm4che3.data.Tag;
import org.dcm4che3.net.QueryOption;
import org.junit.Test;

public class FindSCU_ArrowTest {

    @Test
    public void constructorInitializesCoreObjects() throws IOException {
        FindSCU findSCU = new FindSCU();

        assertNotNull(findSCU.getApplicationEntity());
        assertNotNull(findSCU.getRemoteConnection());
        assertNotNull(findSCU.getAAssociateRQ());
        assertNotNull(findSCU.getDevice());
        assertNotNull(findSCU.getKeys());
        assertNull(findSCU.getAssociation());
    }

    @Test
    public void addLevelStoresQueryRetrieveLevel() throws IOException {
        FindSCU findSCU = new FindSCU();

        findSCU.addLevel("SERIES");

        assertEquals("SERIES", findSCU.getKeys().getString(Tag.QueryRetrieveLevel));
    }

    @Test
    public void setInformationModelAddsLevelForStudyRoot() throws IOException {
        FindSCU findSCU = new FindSCU();

        findSCU.setInformationModel(FindSCU.InformationModel.StudyRoot,
                new String[0], EnumSet.noneOf(QueryOption.class));

        assertEquals("STUDY", findSCU.getKeys().getString(Tag.QueryRetrieveLevel));
        assertNotNull(findSCU.getAAssociateRQ());
    }

    @Test
    public void setInformationModelAddsRelationalAndDatetimeForWorklist() throws IOException {
        FindSCU findSCU = new FindSCU();
        EnumSet<QueryOption> options = EnumSet.noneOf(QueryOption.class);

        findSCU.setInformationModel(FindSCU.InformationModel.MWL,
                new String[0], options);

        assertEquals(false, options.contains(QueryOption.RELATIONAL));
        assertEquals(false, options.contains(QueryOption.DATETIME));
        assertNull(findSCU.getKeys().getString(Tag.QueryRetrieveLevel));
    }

    @Test
    public void closeWithoutAssociationIsSafe() throws Exception {
        FindSCU findSCU = new FindSCU();

        findSCU.close();

        assertNull(findSCU.getAssociation());
    }

    @Test
    public void setOutputDirectoryCreatesDirectory() throws IOException {
        File directory = new File(System.getProperty("java.io.tmpdir"),
                "findscu-arrow-test-output");
        directory.delete();
        FindSCU findSCU = new FindSCU();

        findSCU.setOutputDirectory(directory);

        assertEquals(true, directory.isDirectory());
        directory.delete();
    }
}
