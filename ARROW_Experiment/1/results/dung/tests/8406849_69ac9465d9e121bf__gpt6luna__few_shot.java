package org.btrplace.scheduler.choco.constraint;

import org.btrplace.model.Instance;
import org.btrplace.model.Model;
import org.btrplace.model.Node;
import org.btrplace.model.VM;
import org.btrplace.model.constraint.Ban;
import org.btrplace.model.constraint.Constraints;
import org.btrplace.model.DefaultModel;
import org.btrplace.model.DefaultMapping;
import org.btrplace.scheduler.choco.Parameters;
import org.btrplace.scheduler.choco.ReconfigurationProblem;
import org.junit.Test;

import java.util.Collections;
import java.util.Set;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class CBan_ArrowTest {

    @Test
    public void toStringReturnsBanRepresentation() {
        VM vm = new VM(0);
        Node node = new Node(0);
        Ban ban = new Ban(vm, Collections.singleton(node));
        CBan constraint = new CBan(ban);

        assertEquals(ban.toString(), constraint.toString());
    }

    @Test
    public void misplacedVMsIsEmptyWhenVMIsNotRunning() {
        VM vm = new VM(0);
        Node node = new Node(0);
        Model model = new DefaultModel();
        model.getMapping().addReadyVM(vm);
        Instance instance = new Instance(model, new Ban(vm, Collections.singleton(node)));

        Set<VM> misplaced = new CBan(new Ban(vm, Collections.singleton(node))).getMisPlacedVMs(instance);

        assertTrue(misplaced.isEmpty());
    }

    @Test
    public void misplacedVMsContainsRunningVMOnBannedNode() {
        VM vm = new VM(0);
        Node node = new Node(0);
        Model model = new DefaultModel();
        model.getMapping().addOnlineNode(node);
        model.getMapping().addRunningVM(vm, node);
        Instance instance = new Instance(model, new Ban(vm, Collections.singleton(node)));

        Set<VM> misplaced = new CBan(new Ban(vm, Collections.singleton(node))).getMisPlacedVMs(instance);

        assertEquals(Collections.singleton(vm), misplaced);
    }

    @Test
    public void misplacedVMsIsEmptyWhenRunningOnAllowedNode() {
        VM vm = new VM(0);
        Node bannedNode = new Node(0);
        Node allowedNode = new Node(1);
        Model model = new DefaultModel();
        model.getMapping().addOnlineNode(bannedNode);
        model.getMapping().addOnlineNode(allowedNode);
        model.getMapping().addRunningVM(vm, allowedNode);
        Instance instance = new Instance(model, new Ban(vm, Collections.singleton(bannedNode)));

        Set<VM> misplaced = new CBan(new Ban(vm, Collections.singleton(bannedNode))).getMisPlacedVMs(instance);

        assertTrue(misplaced.isEmpty());
    }
}
