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

import java.util.*;

import static org.mockito.Mockito.*;

public class CBan_ArrowTest {

    @Mock
    private Ban ban;

    @Mock
    private Parameters ps;

    @Mock
    private ReconfigurationProblem rp;

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

    private CBan cBan;

    public void setUp() {
        MockitoAnnotations.initMocks(this);
        cBan = new CBan(ban);
    }

    @org.junit.Test
    public void testInjectContinuousConstraintInitiallySatisfied() {
        setUp();
        when(ban.isContinuous()).thenReturn(true);
        Set<VM> vms = Collections.singleton(vm);
        when(ban.getInvolvedVMs()).thenReturn(vms);
        Set<Node> nodes = Collections.singleton(node);
        when(ban.getInvolvedNodes()).thenReturn(nodes);
        when(rp.getSourceModel()).thenReturn(instance);
        when(instance.getModel()).thenReturn(mapping);
        when(mapping.getVMLocation(vm)).thenReturn(node);
        when(rp.getNode(node)).thenReturn(0);
        when(rp.getLogger()).thenReturn(mock(org.slf4j.Logger.class));

        boolean result = cBan.inject(ps, rp);

        verify(rp.getLogger(), never()).error(anyString());
        assertTrue(result);
    }

    @org.junit.Test
    public void testInjectContinuousConstraintInitiallyViolated() {
        setUp();
        when(ban.isContinuous()).thenReturn(true);
        Set<VM> vms = Collections.singleton(vm);
        when(ban.getInvolvedVMs()).thenReturn(vms);
        Set<Node> nodes = Collections.singleton(node);
        when(ban.getInvolvedNodes()).thenReturn(nodes);
        when(rp.getSourceModel()).thenReturn(instance);
        when(instance.getModel()).thenReturn(mapping);
        when(mapping.getVMLocation(vm)).thenReturn(node);
        when(rp.getNode(node)).thenReturn(0);
        when(rp.getLogger()).thenReturn(mock(org.slf4j.Logger.class));

        boolean result = cBan.inject(ps, rp);

        verify(rp.getLogger()).error(anyString(), eq(ban));
        assertFalse(result);
    }

    @org.junit.Test
    public void testInjectNonContinuousConstraint() {
        setUp();
        when(ban.isContinuous()).thenReturn(false);
        Set<VM> vms = Collections.singleton(vm);
        when(ban.getInvolvedVMs()).thenReturn(vms);
        Set<Node> nodes = Collections.singleton(node);
        when(ban.getInvolvedNodes()).thenReturn(nodes);
        when(rp.getNode(node)).thenReturn(0);
        when(rp.getVMAction(vm)).thenReturn(slice);
        when(slice.getHoster()).thenReturn(null);

        boolean result = cBan.inject(ps, rp);

        assertTrue(result);
    }

    @org.junit.Test
    public void testInjectWithContradictionException() {
        setUp();
        when(ban.isContinuous()).thenReturn(false);
        Set<VM> vms = Collections.singleton(vm);
        when(ban.getInvolvedVMs()).thenReturn(vms);
        Set<Node> nodes = Collections.singleton(node);
        when(ban.getInvolvedNodes()).thenReturn(nodes);
        when(rp.getNode(node)).thenReturn(0);
        when(rp.getVMAction(vm)).thenReturn(slice);
        when(slice.getHoster()).thenReturn(mock(org.chocosolver.solver.variables.IntVar.class));
        doThrow(new ContradictionException()).when(slice.getHoster()).removeValue(anyInt(), any());

        boolean result = cBan.inject(ps, rp);

        verify(rp.getLogger()).error(anyString(), any(ContradictionException.class));
        assertFalse(result);
    }

    @org.junit.Test
    public void testGetMisPlacedVMsWhenVMIsRunningOnBannedNode() {
        setUp();
        when(ban.getInvolvedVMs()).thenReturn(Collections.singleton(vm));
        when(ban.getInvolvedNodes()).thenReturn(Collections.singleton(node));
        when(rp.getSourceModel()).thenReturn(instance);
        when(instance.getModel()).thenReturn(mapping);
        when(mapping.isRunning(vm)).thenReturn(true);
        when(mapping.getVMLocation(vm)).thenReturn(node);

        Set<VM> result = cBan.getMisPlacedVMs(instance);

        assertEquals(Collections.singleton(vm), result);
    }

    @org.junit.Test
    public void testGetMisPlacedVMsWhenVMIsNotRunning() {
        setUp();
        when(ban.getInvolvedVMs()).thenReturn(Collections.singleton(vm));
        when(ban.getInvolvedNodes()).thenReturn(Collections.singleton(node));
        when(rp.getSourceModel()).thenReturn(instance);
        when(instance.getModel()).thenReturn(mapping);
        when(mapping.isRunning(vm)).thenReturn(false);

        Set<VM> result = cBan.getMisPlacedVMs(instance);

        assertEquals(Collections.emptySet(), result);
    }

    @org.junit.Test
    public void testGetMisPlacedVMsWhenVMIsNotOnBannedNode() {
        setUp();
        when(ban.getInvolvedVMs()).thenReturn(Collections.singleton(vm));
        when(ban.getInvolvedNodes()).thenReturn(Collections.singleton(node));
        when(rp.getSourceModel()).thenReturn(instance);
        when(instance.getModel()).thenReturn(mapping);
        when(mapping.isRunning(vm)).thenReturn(true);
        when(mapping.getVMLocation(vm)).thenReturn(mock(Node.class));

        Set<VM> result = cBan.getMisPlacedVMs(instance);

        assertEquals(Collections.emptySet(), result);
    }

    @org.junit.Test
    public void testToString() {
        setUp();
        String banString = "Ban constraint";
        when(ban.toString()).thenReturn(banString);

        String result = cBan.toString();

        assertEquals(banString, result);
    }
}
