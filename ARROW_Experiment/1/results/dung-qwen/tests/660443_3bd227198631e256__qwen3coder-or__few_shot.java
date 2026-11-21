package org.openscience.cdk.isomorphism.matchers;

import org.junit.Test;
import org.junit.Before;
import static org.junit.Assert.*;
import org.openscience.cdk.AtomContainer;
import org.openscience.cdk.interfaces.IAtomContainer;
import org.openscience.cdk.interfaces.IAtom;
import org.openscience.cdk.interfaces.IBond;
import org.openscience.cdk.atomtype.CDKAtomTypeMatcher;
import org.openscience.cdk.tools.manipulator.AtomContainerManipulator;
import org.openscience.cdk.Bond;
import org.openscience.cdk.Atom;
import org.openscience.cdk.PseudoAtom;
import org.openscience.cdk.Order;
import org.openscience.cdk.exception.CDKException;

public class QueryAtomContainerCreator_ArrowTest {

    private IAtomContainer container;

    @Before
    public void setUp() throws CDKException {
        container = new AtomContainer();
        IAtom atom1 = new Atom("C");
        IAtom atom2 = new Atom("C");
        IBond bond = new Bond(atom1, atom2, Order.SINGLE);
        container.addAtom(atom1);
        container.addAtom(atom2);
        container.addBond(bond);
    }

    @Test
    public void testCreateBasicQueryContainer() {
        QueryAtomContainer queryContainer = QueryAtomContainerCreator.createBasicQueryContainer(container);
        assertNotNull(queryContainer);
        assertEquals(container.getAtomCount(), queryContainer.getAtomCount());
        assertEquals(container.getBondCount(), queryContainer.getBondCount());
    }

    @Test
    public void testCreateSymbolAndBondOrderQueryContainer() {
        QueryAtomContainer queryContainer = QueryAtomContainerCreator.createSymbolAndBondOrderQueryContainer(container);
        assertNotNull(queryContainer);
        assertEquals(container.getAtomCount(), queryContainer.getAtomCount());
        assertEquals(container.getBondCount(), queryContainer.getBondCount());
    }

    @Test
    public void testCreateSymbolAndChargeQueryContainer() {
        QueryAtomContainer queryContainer = QueryAtomContainerCreator.createSymbolAndChargeQueryContainer(container);
        assertNotNull(queryContainer);
        assertEquals(container.getAtomCount(), queryContainer.getAtomCount());
        assertEquals(container.getBondCount(), queryContainer.getBondCount());
    }

    @Test
    public void testCreateSymbolChargeIDQueryContainer() {
        QueryAtomContainer queryContainer = QueryAtomContainerCreator.createSymbolChargeIDQueryContainer(container);
        assertNotNull(queryContainer);
        assertEquals(container.getAtomCount(), queryContainer.getAtomCount());
        assertEquals(container.getBondCount(), queryContainer.getBondCount());
    }

    @Test
    public void testCreateAnyAtomContainerWithAromaticityTrue() {
        QueryAtomContainer queryContainer = QueryAtomContainerCreator.createAnyAtomContainer(container, true);
        assertNotNull(queryContainer);
        assertEquals(container.getAtomCount(), queryContainer.getAtomCount());
        assertEquals(container.getBondCount(), queryContainer.getBondCount());
    }

    @Test
    public void testCreateAnyAtomContainerWithAromaticityFalse() {
        QueryAtomContainer queryContainer = QueryAtomContainerCreator.createAnyAtomContainer(container, false);
        assertNotNull(queryContainer);
        assertEquals(container.getAtomCount(), queryContainer.getAtomCount());
        assertEquals(container.getBondCount(), queryContainer.getBondCount());
    }

    @Test
    public void testCreateAnyAtomAnyBondContainerWithAromaticityTrue() {
        QueryAtomContainer queryContainer = QueryAtomContainerCreator.createAnyAtomAnyBondContainer(container, true);
        assertNotNull(queryContainer);
        assertEquals(container.getAtomCount(), queryContainer.getAtomCount());
        assertEquals(container.getBondCount(), queryContainer.getBondCount());
    }

    @Test
    public void testCreateAnyAtomAnyBondContainerWithAromaticityFalse() {
        QueryAtomContainer queryContainer = QueryAtomContainerCreator.createAnyAtomAnyBondContainer(container, false);
        assertNotNull(queryContainer);
        assertEquals(container.getAtomCount(), queryContainer.getAtomCount());
        assertEquals(container.getBondCount(), queryContainer.getBondCount());
    }

    @Test
    public void testCreateAnyAtomForPseudoAtomQueryContainer() {
        QueryAtomContainer queryContainer = QueryAtomContainerCreator.createAnyAtomForPseudoAtomQueryContainer(container);
        assertNotNull(queryContainer);
        assertEquals(container.getAtomCount(), queryContainer.getAtomCount());
        assertEquals(container.getBondCount(), queryContainer.getBondCount());
    }
}
