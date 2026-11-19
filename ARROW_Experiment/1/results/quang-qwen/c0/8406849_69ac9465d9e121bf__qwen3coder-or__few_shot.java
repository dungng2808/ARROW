package org.btrplace.scheduler.choco.constraint;

import org.btrplace.model.Instance;
import org.btrplace.model.Mapping;
import org.btrplace.model.Node;
import org.btrplace.model.VM;
import org.btrplace.model.constraint.Ban;
import org.btrplace.scheduler.choco.Parameters;
import org.btrplace.scheduler.choco.ReconfigurationProblem;
import org.btrplace.scheduler.choco.Slice;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
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
    private VM vm;

    @Mock
    private Node node;

    @Mock
    private Slice slice;

    @BeforeMethod
    public void setUp() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void testInjectContinuousConstraintInitiallySatisfied() {
        // Setup
        when(ban.isContinuous()).thenReturn(true);
        Set<VM> vms = new HashSet<>();
        vms.add(vm);
        when(ban.getInvolvedVMs()).thenReturn(vms);
        
        Set<Node> nodes = new HashSet<>();
        nodes.add(node);
        when(ban.getInvolvedNodes()).thenReturn(nodes);
        
        when(reconfigurationProblem.getSourceModel()).thenReturn(instance);
        when(instance.getModel()).thenReturn(mapping);
        when(mapping.getVMLocation(vm)).thenReturn(node);
        
        // Execute
        CBan cBan = new CBan(ban);
        boolean result = cBan.inject(parameters, reconfigurationProblem);
        
        // Verify
        verify(reconfigurationProblem.getLogger(), never()).error(anyString());
        assertTrue(result);
    }

    @Test
    public void testInjectContinuousConstraintInitiallyViolated() {
        // Setup
        when(ban.isContinuous()).thenReturn(true);
        Set<VM> vms = new HashSet<>();
        vms.add(vm);
        when(ban.getInvolvedVMs()).thenReturn(vms);
        
        Set<Node> nodes = new HashSet<>();
        nodes.add(node);
        when(ban.getInvolvedNodes()).thenReturn(nodes);
        
        when(reconfigurationProblem.getSourceModel()).thenReturn(instance);
        when(instance.getModel()).thenReturn(mapping);
        when(mapping.getVMLocation(vm)).thenReturn(node);
        
        // Execute
        CBan cBan = new CBan(ban);
        boolean result = cBan.inject(parameters, reconfigurationProblem);
        
        // Verify
        verify(reconfigurationProblem.getLogger(), times(1)).error(anyString());
        assertFalse(result);
    }

    @Test
    public void testInjectNonContinuousConstraint() {
        // Setup
        when(ban.isContinuous()).thenReturn(false);
        Set<VM> vms = new HashSet<>();
        vms.add(vm);
        when(ban.getInvolvedVMs()).thenReturn(vms);
        
        Set<Node> nodes = new HashSet<>();
        nodes.add(node);
        when(ban.getInvolvedNodes()).thenReturn(nodes);
        
        when(reconfigurationProblem.getNode(node)).thenReturn(1);
        when(reconfigurationProblem.getVMAction(vm)).thenReturn(slice);
        when(slice.getHoster()).thenReturn(null);
        
        // Execute
        CBan cBan = new CBan(ban);
        boolean result = cBan.inject(parameters, reconfigurationProblem);
        
        // Verify
        verify(reconfigurationProblem, never()).getLogger();
        assertTrue(result);
    }

    @Test
    public void testGetMisPlacedVMsWithNoMisPlacement() {
        // Setup
        when(ban.getInvolvedVMs()).thenReturn(Collections.singleton(vm));
        when(ban.getInvolvedNodes()).thenReturn(Collections.singleton(node));
        
        when(reconfigurationProblem.getSourceModel()).thenReturn(instance);
        when(instance.getModel()).thenReturn(mapping);
        when(mapping.isRunning(vm)).thenReturn(true);
        when(mapping.getVMLocation(vm)).thenReturn(node);
        
        // Execute
        CBan cBan = new CBan(ban);
        Set<VM> result = cBan.getMisPlacedVMs(instance);
        
        // Verify
        assertTrue(result.isEmpty());
    }

    @Test
    public void testGetMisPlacedVMsWithMisPlacement() {
        // Setup
        when(ban.getInvolvedVMs()).thenReturn(Collections.singleton(vm));
        when(ban.getInvolvedNodes()).thenReturn(Collections.singleton(node));
        
        when(reconfigurationProblem.getSourceModel()).thenReturn(instance);
        when(instance.getModel()).thenReturn(mapping);
        when(mapping.isRunning(vm)).thenReturn(true);
        when(mapping.getVMLocation(vm)).thenReturn(node);
        
        // Execute
        CBan cBan = new CBan(ban);
        Set<VM> result = cBan.getMisPlacedVMs(instance);
        
        // Verify
        assertEquals(result.size(), 1);
        assertTrue(result.contains(vm));
    }

    @Test
    public void testToString() {
        // Setup
        String banString = "Ban constraint";
        when(ban.toString()).thenReturn(banString);
        
        // Execute
        CBan cBan = new CBan(ban);
        String result = cBan.toString();
        
        // Verify
        assertEquals(result, banString);
    }
}
