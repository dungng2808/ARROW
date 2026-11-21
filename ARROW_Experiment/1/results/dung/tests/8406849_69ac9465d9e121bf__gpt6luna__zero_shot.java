package org.btrplace.scheduler.choco.constraint;

import org.btrplace.model.Instance;
import org.btrplace.model.Model;
import org.btrplace.model.Node;
import org.btrplace.model.VM;
import org.btrplace.model.constraint.Ban;
import org.btrplace.scheduler.choco.Parameters;
import org.btrplace.scheduler.choco.ReconfigurationProblem;
import org.junit.Test;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class CBan_ArrowTest {

    @Test
    public void toStringReturnsBanString() {
        Ban ban = new Ban(Collections.<VM>emptySet(), Collections.<Node>emptySet());
        CBan constraint = new CBan(ban);

        assertEquals(ban.toString(), constraint.toString());
    }

    @Test
    public void misplacedVMsIsEmptyWhenNoVMIsInvolved() {
        Ban ban = new Ban(Collections.<VM>emptySet(), Collections.<Node>emptySet());
        CBan constraint = new CBan(ban);
        Instance instance = new Instance(new Model());

        assertTrue(constraint.getMisPlacedVMs(instance).isEmpty());
    }

    @Test
    public void misplacedVMsIsEmptyWhenVMIsNotRunning() {
        Model model = new Model();
        VM vm = model.newVM();
        Node node = model.newNode();
        Ban ban = new Ban(Collections.singleton(vm), Collections.singleton(node));
        CBan constraint = new CBan(ban);
        Instance instance = new Instance(model);

        assertTrue(constraint.getMisPlacedVMs(instance).isEmpty());
    }

    @Test
    public void misplacedVMsContainsRunningVMOnBannedNode() {
        Model model = new Model();
        VM vm = model.newVM();
        Node node = model.newNode();
        model.getMapping().addOnlineNode(node);
        model.getMapping().addRunningVM(vm, node);
        Ban ban = new Ban(Collections.singleton(vm), Collections.singleton(node));
        CBan constraint = new CBan(ban);
        Instance instance = new Instance(model);

        Set<VM> expected = new HashSet<>();
        expected.add(vm);
        assertEquals(expected, constraint.getMisPlacedVMs(instance));
    }

    @Test
    public void misplacedVMsIsEmptyWhenRunningOnAllowedNode() {
        Model model = new Model();
        VM vm = model.newVM();
        Node banned = model.newNode();
        Node allowed = model.newNode();
        model.getMapping().addOnlineNode(allowed);
        model.getMapping().addRunningVM(vm, allowed);
        Ban ban = new Ban(Collections.singleton(vm), Collections.singleton(banned));
        CBan constraint = new CBan(ban);
        Instance instance = new Instance(model);

        assertFalse(constraint.getMisPlacedVMs(instance).contains(vm));
    }

    @Test
    public void injectReturnsFalseForContinuousBanInitiallyViolated() {
        Model model = new Model();
        VM vm = model.newVM();
        Node node = model.newNode();
        model.getMapping().addOnlineNode(node);
        model.getMapping().addRunningVM(vm, node);
        Ban ban = new Ban(Collections.singleton(vm), Collections.singleton(node));
        ban.setContinuous(true);
        CBan constraint = new CBan(ban);

        assertFalse(constraint.inject(new Parameters(), new ReconfigurationProblem(model)));
    }
}
