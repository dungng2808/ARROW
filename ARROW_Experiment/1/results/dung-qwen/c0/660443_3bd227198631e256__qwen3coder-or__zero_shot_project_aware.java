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
import org.openscience.cdk.DefaultChemObjectBuilder;
import org.openscience.cdk.Bond;
import org.openscience.cdk.Atom;
import org.openscience.cdk.PseudoAtom;
import org.openscience.cdk.Order;
import org.openscience.cdk.exception.CDKException;

public class QueryAtomContainerCreator_ArrowTest {

    private IAtomContainer molecule;

    @Before
    public void setUp() throws CDKException {
        molecule = new AtomContainer();
        IAtom atom1 = new Atom("C");
        IAtom atom2 = new Atom("C");
        IBond bond = new Bond(atom1, atom2, Order.SINGLE);
        molecule.addAtom(atom1);
        molecule.addAtom(atom2);
        molecule.addBond(bond);
    }

    @Test
    public void testCreateBasicQueryContainer() {
        QueryAtomContainer result = QueryAtomContainerCreator.createBasicQueryContainer(molecule);
        assertNotNull(result);
        assertEquals(molecule.getAtomCount(), result.getAtomCount());
        assertEquals(molecule.getBondCount(), result.getBondCount());
    }

    @Test
    public void testCreateSymbolAndBondOrderQueryContainer() {
        QueryAtomContainer result = QueryAtomContainerCreator.createSymbolAndBondOrderQueryContainer(molecule);
        assertNotNull(result);
        assertEquals(molecule.getAtomCount(), result.getAtomCount());
        assertEquals(molecule.getBondCount(), result.getBondCount());
    }

    @Test
    public void testCreateSymbolAndChargeQueryContainer() {
        QueryAtomContainer result = QueryAtomContainerCreator.createSymbolAndChargeQueryContainer(molecule);
        assertNotNull(result);
        assertEquals(molecule.getAtomCount(), result.getAtomCount());
        assertEquals(molecule.getBondCount(), result.getBondCount());
    }

    @Test
    public void testCreateSymbolChargeIDQueryContainer() {
        QueryAtomContainer result = QueryAtomContainerCreator.createSymbolChargeIDQueryContainer(molecule);
        assertNotNull(result);
        assertEquals(molecule.getAtomCount(), result.getAtomCount());
        assertEquals(molecule.getBondCount(), result.getBondCount());
    }

    @Test
    public void testCreateAnyAtomContainerWithAromaticityTrue() {
        QueryAtomContainer result = QueryAtomContainerCreator.createAnyAtomContainer(molecule, true);
        assertNotNull(result);
        assertEquals(molecule.getAtomCount(), result.getAtomCount());
        assertEquals(molecule.getBondCount(), result.getBondCount());
    }

    @Test
    public void testCreateAnyAtomContainerWithAromaticityFalse() {
        QueryAtomContainer result = QueryAtomContainerCreator.createAnyAtomContainer(molecule, false);
        assertNotNull(result);
        assertEquals(molecule.getAtomCount(), result.getAtomCount());
        assertEquals(molecule.getBondCount(), result.getBondCount());
    }

    @Test
    public void testCreateAnyAtomAnyBondContainerWithAromaticityTrue() {
        QueryAtomContainer result = QueryAtomContainerCreator.createAnyAtomAnyBondContainer(molecule, true);
        assertNotNull(result);
        assertEquals(molecule.getAtomCount(), result.getAtomCount());
        assertEquals(molecule.getBondCount(), result.getBondCount());
    }

    @Test
    public void testCreateAnyAtomAnyBondContainerWithAromaticityFalse() {
        QueryAtomContainer result = QueryAtomContainerCreator.createAnyAtomAnyBondContainer(molecule, false);
        assertNotNull(result);
        assertEquals(molecule.getAtomCount(), result.getAtomCount());
        assertEquals(molecule.getBondCount(), result.getBondCount());
    }

    @Test
    public void testCreateAnyAtomForPseudoAtomQueryContainer() {
        QueryAtomContainer result = QueryAtomContainerCreator.createAnyAtomForPseudoAtomQueryContainer(molecule);
        assertNotNull(result);
        assertEquals(molecule.getAtomCount(), result.getAtomCount());
        assertEquals(molecule.getBondCount(), result.getBondCount());
    }
}
