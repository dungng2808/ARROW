package com.graphhopper.jsprit.core.algorithm.recreate;

import com.graphhopper.jsprit.core.problem.JobActivityFactory;
import com.graphhopper.jsprit.core.problem.constraint.ConstraintManager;
import com.graphhopper.jsprit.core.problem.cost.VehicleRoutingActivityCosts;
import com.graphhopper.jsprit.core.problem.cost.VehicleRoutingTransportCosts;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.mock;

public class ShipmentInsertionCalculator_ArrowTest {

    @Test
    public void toStringReturnsCalculatorName() {
        ShipmentInsertionCalculator calculator = new ShipmentInsertionCalculator(
            mock(VehicleRoutingTransportCosts.class),
            mock(VehicleRoutingActivityCosts.class),
            mock(ActivityInsertionCostsCalculator.class),
            mock(ConstraintManager.class),
            mock(JobActivityFactory.class));

        assertEquals("[name=calculatesShipmentInsertion]", calculator.toString());
    }

    @Test
    public void constructorCreatesCalculator() {
        ShipmentInsertionCalculator calculator = new ShipmentInsertionCalculator(
            mock(VehicleRoutingTransportCosts.class),
            mock(VehicleRoutingActivityCosts.class),
            mock(ActivityInsertionCostsCalculator.class),
            mock(ConstraintManager.class),
            mock(JobActivityFactory.class));

        assertNotNull(calculator);
    }
}
