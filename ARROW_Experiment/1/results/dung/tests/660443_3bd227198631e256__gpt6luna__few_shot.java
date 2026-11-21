package org.openscience.cdk.isomorphism.matchers;

import org.junit.Test;
import org.openscience.cdk.interfaces.IAtomContainer;
import org.openscience.cdk.silent.SilentChemObjectBuilder;
import org.openscience.cdk.silent.AtomContainer;
import org.openscience.cdk.silent.Atom;
import org.openscience.cdk.silent.Bond;
import org.openscience.cdk.interfaces.IBond;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class QueryAtomContainerCreator_ArrowTest {

    private IAtomContainer emptyContainer() {
        return new AtomContainer();
    }

    @Test
    public void createBasicQueryContainerReturnsEmptyQuery() {
        QueryAtomContainer result =
                QueryAtomContainerCreator.createBasicQueryContainer(emptyContainer());
        assertNotNull(result);
        assertEquals(0, result.getAtomCount());
        assertEquals(0, result.getBondCount());
    }

    @Test
    public void createSymbolAndBondOrderQueryContainerReturnsEmptyQuery() {
        QueryAtomContainer result =
                QueryAtomContainerCreator.createSymbolAndBondOrderQueryContainer(emptyContainer());
        assertNotNull(result);
        assertEquals(0, result.getAtomCount());
        assertEquals(0, result.getBondCount());
    }

    @Test
    public void createSymbolAndChargeQueryContainerReturnsEmptyQuery() {
        QueryAtomContainer result =
                QueryAtomContainerCreator.createSymbolAndChargeQueryContainer(emptyContainer());
        assertNotNull(result);
        assertEquals(0, result.getAtomCount());
        assertEquals(0, result.getBondCount());
    }

    @Test
    public void createSymbolChargeIDQueryContainerReturnsEmptyQuery() {
        QueryAtomContainer result =
                QueryAtomContainerCreator.createSymbolChargeIDQueryContainer(emptyContainer());
        assertNotNull(result);
        assertEquals(0, result.getAtomCount());
        assertEquals(0, result.getBondCount());
    }

    @Test
    public void createAnyAtomContainerReturnsEmptyQueryForBothAromaticityOptions() {
        IAtomContainer container = emptyContainer();
        QueryAtomContainer aromatic =
                QueryAtomContainerCreator.createAnyAtomContainer(container, true);
        QueryAtomContainer nonAromatic =
                QueryAtomContainerCreator.createAnyAtomContainer(container, false);

        assertNotNull(aromatic);
        assertEquals(0, aromatic.getAtomCount());
        assertEquals(0, aromatic.getBondCount());
        assertNotNull(nonAromatic);
        assertEquals(0, nonAromatic.getAtomCount());
        assertEquals(0, nonAromatic.getBondCount());
    }

    @Test
    public void createAnyAtomAnyBondContainerReturnsEmptyQueryForBothAromaticityOptions() {
        IAtomContainer container = emptyContainer();
        QueryAtomContainer aromatic =
                QueryAtomContainerCreator.createAnyAtomAnyBondContainer(container, true);
        QueryAtomContainer nonAromatic =
                QueryAtomContainerCreator.createAnyAtomAnyBondContainer(container, false);

        assertNotNull(aromatic);
        assertEquals(0, aromatic.getAtomCount());
        assertEquals(0, aromatic.getBondCount());
        assertNotNull(nonAromatic);
        assertEquals(0, nonAromatic.getAtomCount());
        assertEquals(0, nonAromatic.getBondCount());
    }

    @Test
    public void createAnyAtomForPseudoAtomQueryContainerReturnsEmptyQuery() {
        QueryAtomContainer result =
                QueryAtomContainerCreator.createAnyAtomForPseudoAtomQueryContainer(emptyContainer());
        assertNotNull(result);
        assertEquals(0, result.getAtomCount());
        assertEquals(0, result.getBondCount());
    }
}
