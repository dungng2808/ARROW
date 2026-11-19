package com.graphhopper.jsprit.core.algorithm.recreate;

import com.graphhopper.jsprit.core.problem.JobActivityFactory;
import com.graphhopper.jsprit.core.problem.constraint.ConstraintManager;
import com.graphhopper.jsprit.core.problem.cost.VehicleRoutingActivityCosts;
import com.graphhopper.jsprit.core.problem.cost.VehicleRoutingTransportCosts;
import com.graphhopper.jsprit.core.problem.job.Shipment;
import com.graphhopper.jsprit.core.problem.solution.route.VehicleRoute;
import com.graphhopper.jsprit.core.problem.vehicle.Vehicle;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class ShipmentInsertionCalculator_ArrowTest {

    @Test
    public void toStringReturnsCalculatorName() {
        ShipmentInsertionCalculator calculator = new ShipmentInsertionCalculator(
            null, null, null, null, null);

        assertEquals("[name=calculatesShipmentInsertion]", calculator.toString());
    }

    @Test
    public void insertionWithoutRouteAndDependenciesReturnsNoInsertion() {
        ShipmentInsertionCalculator calculator = new ShipmentInsertionCalculator(
            null, null, null, null, null);

        InsertionData result = calculator.getInsertionData(
            null, null, null, 0.0, null, 0.0);

        assertNotNull(result);
        assertEquals(InsertionData.NO_INDEX, result.getPickupInsertionIndex());
        assertEquals(InsertionData.NO_INDEX, result.getDeliveryInsertionIndex());
    }
}
