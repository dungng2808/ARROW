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
        // Create a Ban constraint that is continuous
        Ban ban = Mockito.mock(Ban.class);
        when(ban.isContinuous()).thenReturn(true);
        
        // Mock involved VMs and nodes
        Set<VM> vms = new HashSet<>();
        VM vm = Mockito.mock(VM.class);
        vms.add(vm);
        when(ban.getInvolvedVMs()).thenReturn(vms);
        
        Set<Node> nodes = new HashSet<>();
        Node node = Mockito.mock(Node.class);
        nodes.add(node);
        when(ban.getInvolvedNodes()).thenReturn(nodes);
        
        // Mock the source model mapping to indicate VM is running on banned node
        Instance instance = Mockito.mock(Instance.class);
        Mapping mapping = Mockito.mock(Mapping.class);
        when(instance.getModel()).thenReturn(Mockito.mock(org.btrplace.model.Model.class));
        when(instance.getModel().getMapping()).thenReturn(mapping);
        when(mapping.getVMLocation(vm)).thenReturn(node);
        
        // Mock reconfiguration problem
        ReconfigurationProblem rp = Mockito.mock(ReconfigurationProblem.class);
        when(rp.getSourceModel()).thenReturn(instance);
        when(rp.getLogger()).thenReturn(Mockito.mock(org.slf4j.Logger.class));
        
        // Create CBan instance
        CBan cban = new CBan(ban);
        
        // Inject should return false due to initial violation
        assertFalse(cban.inject(Mockito.mock(Parameters.class), rp));
    }

    @Test
    public void testInjectWithContinuousConstraintAndNoInitialViolation() {
        // Create a Ban constraint that is continuous
        Ban ban = Mockito.mock(Ban.class);
        when(ban.isContinuous()).thenReturn(true);
        
        // Mock involved VMs and nodes
        Set<VM> vms = new HashSet<>();
        VM vm = Mockito.mock(VM.class);
        vms.add(vm);
        when(ban.getInvolvedVMs()).thenReturn(vms);
        
        Set<Node> nodes = new HashSet<>();
        Node node = Mockito.mock(Node.class);
        nodes.add(node);
        when(ban.getInvolvedNodes()).thenReturn(nodes);
        
        // Mock the source model mapping to indicate VM is NOT running on banned node
        Instance instance = Mockito.mock(Instance.class);
        Mapping mapping = Mockito.mock(Mapping.class);
        when(instance.getModel()).thenReturn(Mockito.mock(org.btrplace.model.Model.class));
        when(instance.getModel().getMapping()).thenReturn(mapping);
        when(mapping.getVMLocation(vm)).thenReturn(Mockito.mock(Node.class)); // Different node
        
        // Mock reconfiguration problem
        ReconfigurationProblem rp = Mockito.mock(ReconfigurationProblem.class);
        when(rp.getSourceModel()).thenReturn(instance);
        when(rp.getLogger()).thenReturn(Mockito.mock(org.slf4j.Logger.class));
        when(rp.getNode(node)).thenReturn(0); // Node index
        
        // Mock VM action slice
        Slice slice = Mockito.mock(Slice.class);
        when(rp.getVMAction(vm)).thenReturn(slice);
        when(slice.getHoster()).thenReturn(Mockito.mock(org.chocosolver.solver.variables.IntVar.class));
        
        // Create CBan instance
        CBan cban = new CBan(ban);
        
        // Inject should return true
        assertTrue(cban.inject(Mockito.mock(Parameters.class), rp));
    }

    @Test
    public void testInjectWithNonContinuousConstraint() {
        // Create a Ban constraint that is not continuous
        Ban ban = Mockito.mock(Ban.class);
        when(ban.isContinuous()).thenReturn(false);
        
        // Mock involved VMs and nodes
        Set<VM> vms = new HashSet<>();
        VM vm = Mockito.mock(VM.class);
        vms.add(vm);
        when(ban.getInvolvedVMs()).thenReturn(vms);
        
        Set<Node> nodes = new HashSet<>();
        Node node = Mockito.mock(Node.class);
        nodes.add(node);
        when(ban.getInvolvedNodes()).thenReturn(nodes);
        
        // Mock reconfiguration problem
        ReconfigurationProblem rp = Mockito.mock(ReconfigurationProblem.class);
        when(rp.getNode(node)).thenReturn(0); // Node index
        
        // Mock VM action slice
        Slice slice = Mockito.mock(Slice.class);
        when(rp.getVMAction(vm)).thenReturn(slice);
        when(slice.getHoster()).thenReturn(Mockito.mock(org.chocosolver.solver.variables.IntVar.class));
        
        // Create CBan instance
        CBan cban = new CBan(ban);
        
        // Inject should return true
        assertTrue(cban.inject(Mockito.mock(Parameters.class), rp));
    }

    @Test
    public void testGetMisPlacedVMsWithRunningVMOnBannedNode() {
        // Create a Ban constraint
        Ban ban = Mockito.mock(Ban.class);
        Set<VM> vms = new HashSet<>();
        VM vm = Mockito.mock(VM.class);
        vms.add(vm);
        when(ban.getInvolvedVMs()).thenReturn(vms);
        
        Set<Node> nodes = new HashSet<>();
        Node node = Mockito.mock(Node.class);
        nodes.add(node);
        when(ban.getInvolvedNodes()).thenReturn(nodes);
        
        // Mock mapping to show VM is running on banned node
        Mapping mapping = Mockito.mock(Mapping.class);
        when(mapping.isRunning(vm)).thenReturn(true);
        when(mapping.getVMLocation(vm)).thenReturn(node);
        
        // Mock instance
        Instance instance = Mockito.mock(Instance.class);
        when(instance.getModel()).thenReturn(Mockito.mock(org.btrplace.model.Model.class));
        when(instance.getModel().getMapping()).thenReturn(mapping);
        
        // Create CBan instance
        CBan cban = new CBan(ban);
        
        // Get mis-placed VMs
        Set<VM> result = cban.getMisPlacedVMs(instance);
        
        // Should contain the VM
        assertEquals(1, result.size());
        assertTrue(result.contains(vm));
    }

    @Test
    public void testGetMisPlacedVMsWithRunningVMOnNonBannedNode() {
        // Create a Ban constraint
        Ban ban = Mockito.mock(Ban.class);
        Set<VM> vms = new HashSet<>();
        VM vm = Mockito.mock(VM.class);
        vms.add(vm);
        when(ban.getInvolvedVMs()).thenReturn(vms);
        
        Set<Node> nodes = new HashSet<>();
        Node node = Mockito.mock(Node.class);
        nodes.add(node);
        when(ban.getInvolvedNodes()).thenReturn(nodes);
        
        // Mock mapping to show VM is running on non-banned node
        Mapping mapping = Mockito.mock(Mapping.class);
        when(mapping.isRunning(vm)).thenReturn(true);
        when(mapping.getVMLocation(vm)).thenReturn(Mockito.mock(Node.class)); // Different node
        
        // Mock instance
        Instance instance = Mockito.mock(Instance.class);
        when(instance.getModel()).thenReturn(Mockito.mock(org.btrplace.model.Model.class));
        when(instance.getModel().getMapping()).thenReturn(mapping);
        
        // Create CBan instance
        CBan cban = new CBan(ban);
        
        // Get mis-placed VMs
        Set<VM> result = cban.getMisPlacedVMs(instance);
        
        // Should be empty
        assertTrue(result.isEmpty());
    }

    @Test
    public void testGetMisPlacedVMsWithNonRunningVM() {
        // Create a Ban constraint
        Ban ban = Mockito.mock(Ban.class);
        Set<VM> vms = new HashSet<>();
        VM vm = Mockito.mock(VM.class);
        vms.add(vm);
        when(ban.getInvolvedVMs()).thenReturn(vms);
        
        Set<Node> nodes = new HashSet<>();
        Node node = Mockito.mock(Node.class);
        nodes.add(node);
        when(ban.getInvolvedNodes()).thenReturn(nodes);
        
        // Mock mapping to show VM is not running
        Mapping mapping = Mockito.mock(Mapping.class);
        when(mapping.isRunning(vm)).thenReturn(false);
        
        // Mock instance
        Instance instance = Mockito.mock(Instance.class);
        when(instance.getModel()).thenReturn(Mockito.mock(org.btrplace.model.Model.class));
        when(instance.getModel().getMapping()).thenReturn(mapping);
        
        // Create CBan instance
        CBan cban = new CBan(ban);
        
        // Get mis-placed VMs
        Set<VM> result = cban.getMisPlacedVMs(instance);
        
        // Should be empty
        assertTrue(result.isEmpty());
    }

    @Test
    public void testToString() {
        // Create a Ban constraint
        Ban ban = Mockito.mock(Ban.class);
        when(ban.toString()).thenReturn("Ban constraint");
        
        // Create CBan instance
        CBan cban = new CBan(ban);
        
        // Test toString
        assertEquals("Ban constraint", cban.toString());
    }
}
