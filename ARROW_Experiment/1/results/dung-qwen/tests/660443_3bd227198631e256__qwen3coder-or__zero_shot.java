package org.openscience.cdk.isomorphism.matchers;

import org.junit.Test;
import org.openscience.cdk.interfaces.IAtomContainer;
import org.openscience.cdk.interfaces.IAtom;
import org.openscience.cdk.interfaces.IBond;
import org.openscience.cdk.AtomContainer;
import org.openscience.cdk.atomtype.CDKAtomTypeMatcher;
import org.openscience.cdk.interfaces.IAtomType;
import org.openscience.cdk.Bond;
import org.openscience.cdk.Atom;
import org.openscience.cdk.Order;

import static org.junit.Assert.*;

public class QueryAtomContainerCreator_ArrowTest {

    @Test
    public void testCreateBasicQueryContainer() {
        IAtomContainer container = new AtomContainer();
        IAtom atom1 = new Atom("C");
        IAtom atom2 = new Atom("C");
        container.addAtom(atom1);
        container.addAtom(atom2);
        container.addBond(new Bond(atom1, atom2, Order.SINGLE));
        
        QueryAtomContainer result = QueryAtomContainerCreator.createBasicQueryContainer(container);
        assertNotNull(result);
        assertEquals(2, result.getAtomCount());
        assertEquals(1, result.getBondCount());
    }

    @Test
    public void testCreateSymbolAndBondOrderQueryContainer() {
        IAtomContainer container = new AtomContainer();
        IAtom atom1 = new Atom("C");
        IAtom atom2 = new Atom("C");
        container.addAtom(atom1);
        container.addAtom(atom2);
        container.addBond(new Bond(atom1, atom2, Order.SINGLE));
        
        QueryAtomContainer result = QueryAtomContainerCreator.createSymbolAndBondOrderQueryContainer(container);
        assertNotNull(result);
        assertEquals(2, result.getAtomCount());
        assertEquals(1, result.getBondCount());
    }

    @Test
    public void testCreateSymbolAndChargeQueryContainer() {
        IAtomContainer container = new AtomContainer();
        IAtom atom1 = new Atom("C");
        IAtom atom2 = new Atom("C");
        container.addAtom(atom1);
        container.addAtom(atom2);
        container.addBond(new Bond(atom1, atom2, Order.SINGLE));
        
        QueryAtomContainer result = QueryAtomContainerCreator.createSymbolAndChargeQueryContainer(container);
        assertNotNull(result);
        assertEquals(2, result.getAtomCount());
        assertEquals(1, result.getBondCount());
    }

    @Test
    public void testCreateSymbolChargeIDQueryContainer() {
        IAtomContainer container = new AtomContainer();
        IAtom atom1 = new Atom("C");
        IAtom atom2 = new Atom("C");
        container.addAtom(atom1);
        container.addAtom(atom2);
        container.addBond(new Bond(atom1, atom2, Order.SINGLE));
        
        QueryAtomContainer result = QueryAtomContainerCreator.createSymbolChargeIDQueryContainer(container);
        assertNotNull(result);
        assertEquals(2, result.getAtomCount());
        assertEquals(1, result.getBondCount());
    }

    @Test
    public void testCreateAnyAtomContainer_AromaticityTrue() {
        IAtomContainer container = new AtomContainer();
        IAtom atom1 = new Atom("C");
        IAtom atom2 = new Atom("C");
        container.addAtom(atom1);
        container.addAtom(atom2);
        container.addBond(new Bond(atom1, atom2, Order.SINGLE));
        
        QueryAtomContainer result = QueryAtomContainerCreator.createAnyAtomContainer(container, true);
        assertNotNull(result);
        assertEquals(2, result.getAtomCount());
        assertEquals(1, result.getBondCount());
    }

    @Test
    public void testCreateAnyAtomContainer_AromaticityFalse() {
        IAtomContainer container = new AtomContainer();
        IAtom atom1 = new Atom("C");
        IAtom atom2 = new Atom("C");
        container.addAtom(atom1);
        container.addAtom(atom2);
        container.addBond(new Bond(atom1, atom2, Order.SINGLE));
        
        QueryAtomContainer result = QueryAtomContainerCreator.createAnyAtomContainer(container, false);
        assertNotNull(result);
        assertEquals(2, result.getAtomCount());
        assertEquals(1, result.getBondCount());
    }

    @Test
    public void testCreateAnyAtomAnyBondContainer_AromaticityTrue() {
        IAtomContainer container = new AtomContainer();
        IAtom atom1 = new Atom("C");
        IAtom atom2 = new Atom("C");
        container.addAtom(atom1);
        container.addAtom(atom2);
        container.addBond(new Bond(atom1, atom2, Order.SINGLE));
        
        QueryAtomContainer result = QueryAtomContainerCreator.createAnyAtomAnyBondContainer(container, true);
        assertNotNull(result);
        assertEquals(2, result.getAtomCount());
        assertEquals(1, result.getBondCount());
    }

    @Test
    public void testCreateAnyAtomAnyBondContainer_AromaticityFalse() {
        IAtomContainer container = new AtomContainer();
        IAtom atom1 = new Atom("C");
        IAtom atom2 = new Atom("C");
        container.addAtom(atom1);
        container.addAtom(atom2);
        container.addBond(new Bond(atom1, atom2, Order.SINGLE));
        
        QueryAtomContainer result = QueryAtomContainerCreator.createAnyAtomAnyBondContainer(container, false);
        assertNotNull(result);
        assertEquals(2, result.getAtomCount());
        assertEquals(1, result.getBondCount());
    }

    @Test
    public void testCreateAnyAtomForPseudoAtomQueryContainer() {
        IAtomContainer container = new AtomContainer();
        IAtom atom1 = new Atom("C");
        IAtom atom2 = new Atom("C");
        container.addAtom(atom1);
        container.addAtom(atom2);
        container.addBond(new Bond(atom1, atom2, Order.SINGLE));
        
        QueryAtomContainer result = QueryAtomContainerCreator.createAnyAtomForPseudoAtomQueryContainer(container);
        assertNotNull(result);
        assertEquals(2, result.getAtomCount());
        assertEquals(1, result.getBondCount());
    }
}
