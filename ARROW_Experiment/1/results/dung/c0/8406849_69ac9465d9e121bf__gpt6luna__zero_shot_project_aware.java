package org.btrplace.scheduler.choco.constraint;

import org.btrplace.model.Instance;
import org.btrplace.model.Model;
import org.btrplace.model.Node;
import org.btrplace.model.VM;
import org.btrplace.model.constraint.Ban;
import org.btrplace.scheduler.choco.Parameters;
import org.btrplace.scheduler.choco.ReconfigurationProblem;
import org.testng.annotations.Test;

import java.util.Collections;
import java.util.Set;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

public class CBan_ArrowTest {

    @Test
    public void testToString() {
        Ban ban = new Ban(Collections.<VM>emptySet(), Collections.<Node>emptySet());
        CBan constraint = new CBan(ban);

        assertEquals(constraint.toString(), ban.toString());
    }

    @Test
    public void testGetMisPlacedVMsWhenNoVMsAreInvolved() {
        Ban ban = new Ban(Collections.<VM>emptySet(), Collections.<Node>emptySet());
        CBan constraint = new CBan(ban);

        try {
            constraint.getMisPlacedVMs((Instance) null);
            assertTrue(false);
        } catch (Exception expected) {
            assertTrue(expected instanceof RuntimeException);
        }
    }
}
