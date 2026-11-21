package org.btrplace.scheduler.choco.constraint;

import org.btrplace.model.Instance;
import org.btrplace.model.Mapping;
import org.btrplace.model.Node;
import org.btrplace.model.VM;
import org.btrplace.model.constraint.Ban;
import org.btrplace.scheduler.choco.Parameters;
import org.btrplace.scheduler.choco.ReconfigurationProblem;
import org.btrplace.scheduler.choco.Slice;
import org.chocosolver.solver.exception.ContradictionException;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.*;

import static org.mockito.Mockito.*;

public class CBan_ArrowTest {

    @Mock
    private Ban ban;

    @Mock
    private Parameters parameters;

    @Mock
    private ReconfigurationProblem reconfigurationProblem;

    @Mock
    private Instance instance;

    @Mock
    private Mapping mapping;

    @Mock
    private Slice slice;

    @BeforeMethod
    public void setUp() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void testInjectWithContinuousConstraintAndInitialViolation() {
        // Setup
        when(ban.isContinuous()).thenReturn(true);
        Set<VM> vms = new HashSet<>();
        VM vm = mock(VM.class);
        vms.add(vm);
        when(ban.getInvolvedVMs()).thenReturn(vms);

        Node node = mock(Node.class);
        Set<Node> nodes = new HashSet<>();
        nodes.add(node);
        when(ban.getInvolvedNodes()).thenReturn(nodes);

        when(reconfigurationProblem.getSourceModel()).thenReturn(instance);
        when(instance.getModel()).thenReturn(mock(org.btrplace.model.Model.class));
        when(instance.getModel().getMapping()).thenReturn(mapping);
        when(mapping.getVMLocation(vm)).thenReturn(node);
        when(reconfigurationProblem.getLogger()).thenReturn(mock(org.slf4j.Logger.class));

        CBan cBan = new CBan(ban);

        // When
        boolean result = cBan.inject(parameters, reconfigurationProblem);

        // Then
        Assert.assertFalse(result);
    }

    @Test
    public void testInjectWithContinuousConstraintAndNoInitialViolation() {
        // Setup
        when(ban.isContinuous()).thenReturn(true);
        Set<VM> vms = new HashSet<>();
        VM vm = mock(VM.class);
        vms.add(vm);
        when(ban.getInvolvedVMs()).thenReturn(vms);

        Node node = mock(Node.class);
        Set<Node> nodes = new HashSet<>();
        nodes.add(node);
        when(ban.getInvolvedNodes()).thenReturn(nodes);

        when(reconfigurationProblem.getSourceModel()).thenReturn(instance);
        when(instance.getModel()).thenReturn(mock(org.btrplace.model.Model.class));
        when(instance.getModel().getMapping()).thenReturn(mapping);
        when(mapping.getVMLocation(vm)).thenReturn(mock(Node.class)); // Different node

        when(reconfigurationProblem.getLogger()).thenReturn(mock(org.slf4j.Logger.class));

        CBan cBan = new CBan(ban);

        // When
        boolean result = cBan.inject(parameters, reconfigurationProblem);

        // Then
        Assert.assertTrue(result);
    }

    @Test
    public void testInjectWithNonContinuousConstraint() {
        // Setup
        when(ban.isContinuous()).thenReturn(false);
        Set<VM> vms = new HashSet<>();
        VM vm = mock(VM.class);
        vms.add(vm);
        when(ban.getInvolvedVMs()).thenReturn(vms);

        Set<Node> nodes = new HashSet<>();
        Node node = mock(Node.class);
        nodes.add(node);
        when(ban.getInvolvedNodes()).thenReturn(nodes);

        when(reconfigurationProblem.getNode(node)).thenReturn(1);
        when(reconfigurationProblem.getVMAction(vm)).thenReturn(slice);
        when(slice.getHoster()).thenReturn(null); // No hoster to remove value from

        CBan cBan = new CBan(ban);

        // When
        boolean result = cBan.inject(parameters, reconfigurationProblem);

        // Then
        Assert.assertTrue(result);
    }

    @Test
    public void testInjectWithHosterAndContradictionException() throws ContradictionException {
        // Setup
        when(ban.isContinuous()).thenReturn(false);
        Set<VM> vms = new HashSet<>();
        VM vm = mock(VM.class);
        vms.add(vm);
        when(ban.getInvolvedVMs()).thenReturn(vms);

        Set<Node> nodes = new HashSet<>();
        Node node = mock(Node.class);
        nodes.add(node);
        when(ban.getInvolvedNodes()).thenReturn(nodes);

        when(reconfigurationProblem.getNode(node)).thenReturn(1);
        when(reconfigurationProblem.getVMAction(vm)).thenReturn(slice);
        when(slice.getHoster()).thenReturn(mock(org.chocosolver.solver.variables.IntVar.class));
        doThrow(new ContradictionException()).when(slice.getHoster()).removeValue(anyInt(), any());

        when(reconfigurationProblem.getLogger()).thenReturn(mock(org.slf4j.Logger.class));

        CBan cBan = new CBan(ban);

        // When
        boolean result = cBan.inject(parameters, reconfigurationProblem);

        // Then
        Assert.assertFalse(result);
    }

    @Test
    public void testGetMisPlacedVMsWithRunningVMOnBannedNode() {
        // Setup
        Set<VM> vms = new HashSet<>();
        VM vm = mock(VM.class);
        vms.add(vm);
        when(ban.getInvolvedVMs()).thenReturn(vms);

        Set<Node> nodes = new HashSet<>();
        Node node = mock(Node.class);
        nodes.add(node);
        when(ban.getInvolvedNodes()).thenReturn(nodes);

        when(reconfigurationProblem.getSourceModel()).thenReturn(instance);
        when(instance.getModel()).thenReturn(mock(org.btrplace.model.Model.class));
        when(instance.getModel().getMapping()).thenReturn(mapping);
        when(mapping.isRunning(vm)).thenReturn(true);
        when(mapping.getVMLocation(vm)).thenReturn(node);

        CBan cBan = new CBan(ban);

        // When
        Set<VM> result = cBan.getMisPlacedVMs(instance);

        // Then
        Assert.assertEquals(result.size(), 1);
        Assert.assertTrue(result.contains(vm));
    }

    @Test
    public void testGetMisPlacedVMsWithNotRunningVM() {
        // Setup
        Set<VM> vms = new HashSet<>();
        VM vm = mock(VM.class);
        vms.add(vm);
        when(ban.getInvolvedVMs()).thenReturn(vms);

        Set<Node> nodes = new HashSet<>();
        Node node = mock(Node.class);
        nodes.add(node);
        when(ban.getInvolvedNodes()).thenReturn(nodes);

        when(reconfigurationProblem.getSourceModel()).thenReturn(instance);
        when(instance.getModel()).thenReturn(mock(org.btrplace.model.Model.class));
        when(instance.getModel().getMapping()).thenReturn(mapping);
        when(mapping.isRunning(vm)).thenReturn(false);

        CBan cBan = new CBan(ban);

        // When
        Set<VM> result = cBan.getMisPlacedVMs(instance);

        // Then
        Assert.assertTrue(result.isEmpty());
    }

    @Test
    public void testGetMisPlacedVMsWithRunningVMOnNonBannedNode() {
        // Setup
        Set<VM> vms = new HashSet<>();
        VM vm = mock(VM.class);
        vms.add(vm);
        when(ban.getInvolvedVMs()).thenReturn(vms);

        Set<Node> nodes = new HashSet<>();
        Node node = mock(Node.class);
        nodes.add(node);
        when(ban.getInvolvedNodes()).thenReturn(nodes);

        when(reconfigurationProblem.getSourceModel()).thenReturn(instance);
        when(instance.getModel()).thenReturn(mock(org.btrplace.model.Model.class));
        when(instance.getModel().getMapping()).thenReturn(mapping);
        when(mapping.isRunning(vm)).thenReturn(true);
        when(mapping.getVMLocation(vm)).thenReturn(mock(Node.class)); // Different node

        CBan cBan = new CBan(ban);

        // When
        Set<VM> result = cBan.getMisPlacedVMs(instance);

        // Then
        Assert.assertTrue(result.isEmpty());
    }

    @Test
    public void testToString() {
        // Setup
        String banString = "Ban constraint";
        when(ban.toString()).thenReturn(banString);
        CBan cBan = new CBan(ban);

        // When
        String result = cBan.toString();

        // Then
        Assert.assertEquals(result, banString);
    }
}
