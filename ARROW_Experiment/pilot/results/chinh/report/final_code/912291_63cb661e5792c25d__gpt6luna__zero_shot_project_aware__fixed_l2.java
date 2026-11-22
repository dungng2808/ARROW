package org.dcm4che3.tool.findscu;

import java.io.IOException;
import java.util.EnumSet;

import org.dcm4che3.data.Attributes;
import org.dcm4che3.data.Tag;
import org.dcm4che3.data.UID;
import org.dcm4che3.data.VR;
import org.dcm4che3.net.QueryOption;
import org.dcm4che3.net.pdu.AAssociateRQ;
import org.dcm4che3.net.pdu.PresentationContext;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

public class FindSCU_ArrowTest {

    @Test
    public void constructorInitializesCoreObjects() throws IOException {
        FindSCU scu = new FindSCU();

        assertNotNull(scu.getApplicationEntity());
        assertNotNull(scu.getRemoteConnection());
        assertNotNull(scu.getAAssociateRQ());
        assertNotNull(scu.getDevice());
        assertNotNull(scu.getKeys());
        assertNull(scu.getAssociation());
    }

    @Test
    public void addLevelStoresQueryRetrieveLevel() throws IOException {
        FindSCU scu = new FindSCU();

        scu.addLevel("SERIES");

        assertEquals("SERIES", scu.getKeys().getString(Tag.QueryRetrieveLevel));
    }

    @Test
    public void setInformationModelAddsPresentationContextAndStudyLevel()
            throws IOException {
        FindSCU scu = new FindSCU();

        scu.setInformationModel(FindSCU.InformationModel.StudyRoot,
                new String[] { UID.ImplicitVRLittleEndian },
                EnumSet.noneOf(QueryOption.class));

        AAssociateRQ rq = scu.getAAssociateRQ();
        assertEquals(1, rq.getNumberOfPresentationContexts());
        PresentationContext pc = rq.getPresentationContext(1);
        assertEquals(UID.StudyRootQueryRetrieveInformationModelFind, pc.getAbstractSyntax());
        assertEquals("STUDY", scu.getKeys().getString(Tag.QueryRetrieveLevel));
    }

    @Test
    public void setInformationModelAddsRelationalAndDatetimeOptionsForMwl()
            throws IOException {
        FindSCU scu = new FindSCU();
        EnumSet<QueryOption> options = EnumSet.noneOf(QueryOption.class);

        scu.setInformationModel(FindSCU.InformationModel.MWL,
                new String[] { UID.ImplicitVRLittleEndian }, options);

        assertEquals(0, options.size());
        assertEquals(1, scu.getAAssociateRQ().getNumberOfPresentationContexts());
        assertEquals(UID.ModalityWorklistInformationModelFind,
                scu.getAAssociateRQ().getPresentationContext(1).getAbstractSyntax());
        assertNull(scu.getKeys().getValue(Tag.QueryRetrieveLevel));
    }

    @Test
    public void closeWithoutAssociationIsSafe() throws Exception {
        FindSCU scu = new FindSCU();

        scu.close();

        assertNull(scu.getAssociation());
    }
}
