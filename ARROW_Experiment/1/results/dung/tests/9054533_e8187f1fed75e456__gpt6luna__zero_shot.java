package com.graphhopper.jsprit.core.algorithm.recreate;

import com.graphhopper.jsprit.core.problem.JobActivityFactory;
import com.graphhopper.jsprit.core.problem.constraint.ConstraintManager;
import com.graphhopper.jsprit.core.problem.cost.VehicleRoutingActivityCosts;
import com.graphhopper.jsprit.core.problem.cost.VehicleRoutingTransportCosts;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class ShipmentInsertionCalculator_ArrowTest {

    @Test
    public void toStringReturnsCalculatorName() {
        ShipmentInsertionCalculator calculator = new ShipmentInsertionCalculator(
            (VehicleRoutingTransportCosts) null,
            (VehicleRoutingActivityCosts) null,
            (ActivityInsertionCostsCalculator) null,
            (ConstraintManager) null,
            (JobActivityFactory) null
        );

        assertEquals("[name=calculatesShipmentInsertion]", calculator.toString());
    }
}
