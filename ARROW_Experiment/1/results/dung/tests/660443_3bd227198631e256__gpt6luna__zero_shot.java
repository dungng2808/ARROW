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
    public void createBasicQueryContainerWithEmptyContainer() {
        QueryAtomContainer result =
                QueryAtomContainerCreator.createBasicQueryContainer(emptyContainer());

        assertNotNull(result);
        assertEquals(0, result.getAtomCount());
        assertEquals(0, result.getBondCount());
    }

    @Test
    public void createSymbolAndBondOrderQueryContainerWithEmptyContainer() {
        QueryAtomContainer result =
                QueryAtomContainerCreator.createSymbolAndBondOrderQueryContainer(emptyContainer());

        assertNotNull(result);
        assertEquals(0, result.getAtomCount());
        assertEquals(0, result.getBondCount());
    }

    @Test
    public void createSymbolAndChargeQueryContainerWithEmptyContainer() {
        QueryAtomContainer result =
                QueryAtomContainerCreator.createSymbolAndChargeQueryContainer(emptyContainer());

        assertNotNull(result);
        assertEquals(0, result.getAtomCount());
        assertEquals(0, result.getBondCount());
    }

    @Test
    public void createSymbolChargeIDQueryContainerWithEmptyContainer() {
        QueryAtomContainer result =
                QueryAtomContainerCreator.createSymbolChargeIDQueryContainer(emptyContainer());

        assertNotNull(result);
        assertEquals(0, result.getAtomCount());
        assertEquals(0, result.getBondCount());
    }

    @Test
    public void createAnyAtomContainerWithAromaticityEnabledAndEmptyContainer() {
        QueryAtomContainer result =
                QueryAtomContainerCreator.createAnyAtomContainer(emptyContainer(), true);

        assertNotNull(result);
        assertEquals(0, result.getAtomCount());
        assertEquals(0, result.getBondCount());
    }

    @Test
    public void createAnyAtomContainerWithAromaticityDisabledAndEmptyContainer() {
        QueryAtomContainer result =
                QueryAtomContainerCreator.createAnyAtomContainer(emptyContainer(), false);

        assertNotNull(result);
        assertEquals(0, result.getAtomCount());
        assertEquals(0, result.getBondCount());
    }

    @Test
    public void createAnyAtomAnyBondContainerWithAromaticityEnabledAndEmptyContainer() {
        QueryAtomContainer result =
                QueryAtomContainerCreator.createAnyAtomAnyBondContainer(emptyContainer(), true);

        assertNotNull(result);
        assertEquals(0, result.getAtomCount());
        assertEquals(0, result.getBondCount());
    }

    @Test
    public void createAnyAtomAnyBondContainerWithAromaticityDisabledAndEmptyContainer() {
        QueryAtomContainer result =
                QueryAtomContainerCreator.createAnyAtomAnyBondContainer(emptyContainer(), false);

        assertNotNull(result);
        assertEquals(0, result.getAtomCount());
        assertEquals(0, result.getBondCount());
    }

    @Test
    public void createAnyAtomForPseudoAtomQueryContainerWithEmptyContainer() {
        QueryAtomContainer result =
                QueryAtomContainerCreator.createAnyAtomForPseudoAtomQueryContainer(emptyContainer());

        assertNotNull(result);
        assertEquals(0, result.getAtomCount());
        assertEquals(0, result.getBondCount());
    }
}
