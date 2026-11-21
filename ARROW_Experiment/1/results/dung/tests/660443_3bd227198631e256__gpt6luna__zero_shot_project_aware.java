package org.openscience.cdk.isomorphism.matchers;

import org.junit.Test;
import org.openscience.cdk.interfaces.IAtomContainer;
import org.openscience.cdk.silent.SilentChemObjectBuilder;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class QueryAtomContainerCreator_ArrowTest {

    private IAtomContainer emptyContainer() {
        return SilentChemObjectBuilder.getInstance().newAtomContainer();
    }

    @Test
    public void createBasicQueryContainerReturnsEmptyQueryContainer() {
        QueryAtomContainer result =
                QueryAtomContainerCreator.createBasicQueryContainer(emptyContainer());

        assertNotNull(result);
        assertEquals(0, result.getAtomCount());
        assertEquals(0, result.getBondCount());
    }

    @Test
    public void createSymbolAndBondOrderQueryContainerReturnsEmptyQueryContainer() {
        QueryAtomContainer result =
                QueryAtomContainerCreator.createSymbolAndBondOrderQueryContainer(emptyContainer());

        assertNotNull(result);
        assertEquals(0, result.getAtomCount());
        assertEquals(0, result.getBondCount());
    }

    @Test
    public void createSymbolAndChargeQueryContainerReturnsEmptyQueryContainer() {
        QueryAtomContainer result =
                QueryAtomContainerCreator.createSymbolAndChargeQueryContainer(emptyContainer());

        assertNotNull(result);
        assertEquals(0, result.getAtomCount());
        assertEquals(0, result.getBondCount());
    }

    @Test
    public void createSymbolChargeIDQueryContainerReturnsEmptyQueryContainer() {
        QueryAtomContainer result =
                QueryAtomContainerCreator.createSymbolChargeIDQueryContainer(emptyContainer());

        assertNotNull(result);
        assertEquals(0, result.getAtomCount());
        assertEquals(0, result.getBondCount());
    }

    @Test
    public void createAnyAtomContainerWithAromaticityReturnsEmptyQueryContainer() {
        QueryAtomContainer result =
                QueryAtomContainerCreator.createAnyAtomContainer(emptyContainer(), true);

        assertNotNull(result);
        assertEquals(0, result.getAtomCount());
        assertEquals(0, result.getBondCount());
    }

    @Test
    public void createAnyAtomContainerWithoutAromaticityReturnsEmptyQueryContainer() {
        QueryAtomContainer result =
                QueryAtomContainerCreator.createAnyAtomContainer(emptyContainer(), false);

        assertNotNull(result);
        assertEquals(0, result.getAtomCount());
        assertEquals(0, result.getBondCount());
    }

    @Test
    public void createAnyAtomAnyBondContainerWithAromaticityReturnsEmptyQueryContainer() {
        QueryAtomContainer result =
                QueryAtomContainerCreator.createAnyAtomAnyBondContainer(emptyContainer(), true);

        assertNotNull(result);
        assertEquals(0, result.getAtomCount());
        assertEquals(0, result.getBondCount());
    }

    @Test
    public void createAnyAtomAnyBondContainerWithoutAromaticityReturnsEmptyQueryContainer() {
        QueryAtomContainer result =
                QueryAtomContainerCreator.createAnyAtomAnyBondContainer(emptyContainer(), false);

        assertNotNull(result);
        assertEquals(0, result.getAtomCount());
        assertEquals(0, result.getBondCount());
    }

    @Test
    public void createAnyAtomForPseudoAtomQueryContainerReturnsEmptyQueryContainer() {
        QueryAtomContainer result =
                QueryAtomContainerCreator.createAnyAtomForPseudoAtomQueryContainer(emptyContainer());

        assertNotNull(result);
        assertEquals(0, result.getAtomCount());
        assertEquals(0, result.getBondCount());
    }
}
