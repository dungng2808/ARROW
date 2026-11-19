package org.btrplace.scheduler.choco.constraint;

import org.btrplace.model.Instance;
import org.btrplace.model.Mapping;
import org.btrplace.model.Node;
import org.btrplace.model.VM;
import org.btrplace.model.constraint.Ban;
import org.btrplace.scheduler.choco.Parameters;
import org.btrplace.scheduler.choco.ReconfigurationProblem;
import org.btrplace.scheduler.choco.Slice;
import org.junit.Test;
import org.mockito.Mockito;

import java.util.*;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

public class CBan_ArrowTest {

    @Test
    public void testInjectWithContinuousConstraintAndInitialViolation() {
        // Setup
        Ban ban = Mockito.mock(Ban.class);
        CBan cban = new CBan(ban);

        Parameters params = Mockito.mock(Parameters.class);
        ReconfigurationProblem rp = Mockito.mock(ReconfigurationProblem.class);
        Instance instance = Mockito.mock(Instance.class);
        Mapping mapping = Mockito.mock(Mapping.class);

        VM vm = Mockito.mock(VM.class);
        Node node = Mockito.mock(Node.class);

        // Mock behavior
        when(ban.isContinuous()).thenReturn(true);
        when(ban.getInvolvedVMs()).thenReturn(Collections.singleton(vm));
        when(ban.getInvolvedNodes()).thenReturn(Collections.singleton(node));

        when(rp.getSourceModel()).thenReturn(instance);
        when(instance.getModel()).thenReturn(Mockito.mock(org.btrplace.model.Model.class));
        when(instance.getModel().getMapping()).thenReturn(mapping);
        when(mapping.getVMLocation(vm)).thenReturn(node);
        when(rp.getLogger()).thenReturn(Mockito.mock(org.slf4j.Logger.class));

        // Execute
        boolean result = cban.inject(params, rp);

        // Verify
        assertFalse(result);
    }

    @Test
    public void testInjectWithContinuousConstraintAndNoInitialViolation() {
        // Setup
        Ban ban = Mockito.mock(Ban.class);
        CBan cban = new CBan(ban);

        Parameters params = Mockito.mock(Parameters.class);
        ReconfigurationProblem rp = Mockito.mock(ReconfigurationProblem.class);
        Instance instance = Mockito.mock(Instance.class);
        Mapping mapping = Mockito.mock(Mapping.class);

        VM vm = Mockito.mock(VM.class);
        Node node = Mockito.mock(Node.class);

        // Mock behavior
        when(ban.isContinuous()).thenReturn(true);
        when(ban.getInvolvedVMs()).thenReturn(Collections.singleton(vm));
        when(ban.getInvolvedNodes()).thenReturn(Collections.singleton(node));

        when(rp.getSourceModel()).thenReturn(instance);
        when(instance.getModel()).thenReturn(Mockito.mock(org.btrplace.model.Model.class));
        when(instance.getModel().getMapping()).thenReturn(mapping);
        when(mapping.getVMLocation(vm)).thenReturn(Mockito.mock(Node.class)); // Different node
        when(rp.getLogger()).thenReturn(Mockito.mock(org.slf4j.Logger.class));

        // Execute
        boolean result = cban.inject(params, rp);

        // Verify
        assertTrue(result);
    }

    @Test
    public void testInjectWithNonContinuousConstraint() {
        // Setup
        Ban ban = Mockito.mock(Ban.class);
        CBan cban = new CBan(ban);

        Parameters params = Mockito.mock(Parameters.class);
        ReconfigurationProblem rp = Mockito.mock(ReconfigurationProblem.class);

        VM vm = Mockito.mock(VM.class);
        Node node = Mockito.mock(Node.class);

        // Mock behavior
        when(ban.isContinuous()).thenReturn(false);
        when(ban.getInvolvedVMs()).thenReturn(Collections.singleton(vm));
        when(ban.getInvolvedNodes()).thenReturn(Collections.singleton(node));

        // Execute
        boolean result = cban.inject(params, rp);

        // Verify
        assertTrue(result);
    }

    @Test
    public void testInjectWithContinuousConstraintAndSliceNull() {
        // Setup
        Ban ban = Mockito.mock(Ban.class);
        CBan cban = new CBan(ban);

        Parameters params = Mockito.mock(Parameters.class);
        ReconfigurationProblem rp = Mockito.mock(ReconfigurationProblem.class);

        VM vm = Mockito.mock(VM.class);
        Node node = Mockito.mock(Node.class);

        // Mock behavior
        when(ban.isContinuous()).thenReturn(true);
        when(ban.getInvolvedVMs()).thenReturn(Collections.singleton(vm));
        when(ban.getInvolvedNodes()).thenReturn(Collections.singleton(node));

        when(rp.getVMAction(vm)).thenReturn(Mockito.mock(Slice.class));
        when(rp.getVMAction(vm).getDSlice()).thenReturn(null);

        // Execute
        boolean result = cban.inject(params, rp);

        // Verify
        assertTrue(result);
    }

    @Test
    public void testGetMisPlacedVMsWithRunningVMOnBannedNode() {
        // Setup
        Ban ban = Mockito.mock(Ban.class);
        CBan cban = new CBan(ban);

        Instance instance = Mockito.mock(Instance.class);
        Mapping mapping = Mockito.mock(Mapping.class);

        VM vm = Mockito.mock(VM.class);
        Node node = Mockito.mock(Node.class);

        // Mock behavior
        when(ban.getInvolvedVMs()).thenReturn(Collections.singleton(vm));
        when(ban.getInvolvedNodes()).thenReturn(Collections.singleton(node));

        when(instance.getModel()).thenReturn(Mockito.mock(org.btrplace.model.Model.class));
        when(instance.getModel().getMapping()).thenReturn(mapping);
        when(mapping.isRunning(vm)).thenReturn(true);
        when(mapping.getVMLocation(vm)).thenReturn(node);

        // Execute
        Set<VM> result = cban.getMisPlacedVMs(instance);

        // Verify
        assertEquals(1, result.size());
        assertTrue(result.contains(vm));
    }

    @Test
    public void testGetMisPlacedVMsWithNotRunningVM() {
        // Setup
        Ban ban = Mockito.mock(Ban.class);
        CBan cban = new CBan(ban);

        Instance instance = Mockito.mock(Instance.class);
        Mapping mapping = Mockito.mock(Mapping.class);

        VM vm = Mockito.mock(VM.class);
        Node node = Mockito.mock(Node.class);

        // Mock behavior
        when(ban.getInvolvedVMs()).thenReturn(Collections.singleton(vm));
        when(ban.getInvolvedNodes()).thenReturn(Collections.singleton(node));

        when(instance.getModel()).thenReturn(Mockito.mock(org.btrplace.model.Model.class));
        when(instance.getModel().getMapping()).thenReturn(mapping);
        when(mapping.isRunning(vm)).thenReturn(false);

        // Execute
        Set<VM> result = cban.getMisPlacedVMs(instance);

        // Verify
        assertTrue(result.isEmpty());
    }

    @Test
    public void testGetMisPlacedVMsWithRunningVMOnNonBannedNode() {
        // Setup
        Ban ban = Mockito.mock(Ban.class);
        CBan cban = new CBan(ban);

        Instance instance = Mockito.mock(Instance.class);
        Mapping mapping = Mockito.mock(Mapping.class);

        VM vm = Mockito.mock(VM.class);
        Node bannedNode = Mockito.mock(Node.class);
        Node otherNode = Mockito.mock(Node.class);

        // Mock behavior
        when(ban.getInvolvedVMs()).thenReturn(Collections.singleton(vm));
        when(ban.getInvolvedNodes()).thenReturn(Collections.singleton(bannedNode));

        when(instance.getModel()).thenReturn(Mockito.mock(org.btrplace.model.Model.class));
        when(instance.getModel().getMapping()).thenReturn(mapping);
        when(mapping.isRunning(vm)).thenReturn(true);
        when(mapping.getVMLocation(vm)).thenReturn(otherNode);

        // Execute
        Set<VM> result = cban.getMisPlacedVMs(instance);

        // Verify
        assertTrue(result.isEmpty());
    }

    @Test
    public void testToString() {
        // Setup
        Ban ban = Mockito.mock(Ban.class);
        CBan cban = new CBan(ban);

        // Mock behavior
        when(ban.toString()).thenReturn("Ban constraint");

        // Execute
        String result = cban.toString();

        // Verify
        assertEquals("Ban constraint", result);
    }
}
