package com.graphhopper.jsprit.core.algorithm.recreate;

import com.graphhopper.jsprit.core.problem.JobActivityFactory;
import com.graphhopper.jsprit.core.problem.constraint.ConstraintManager;
import com.graphhopper.jsprit.core.problem.cost.VehicleRoutingActivityCosts;
import com.graphhopper.jsprit.core.problem.cost.VehicleRoutingTransportCosts;
import com.graphhopper.jsprit.core.problem.driver.Driver;
import com.graphhopper.jsprit.core.problem.job.Shipment;
import com.graphhopper.jsprit.core.problem.misc.JobInsertionContext;
import com.graphhopper.jsprit.core.problem.solution.route.VehicleRoute;
import com.graphhopper.jsprit.core.problem.solution.route.activity.End;
import com.graphhopper.jsprit.core.problem.solution.route.activity.Start;
import com.graphhopper.jsprit.core.problem.solution.route.activity.TimeWindow;
import com.graphhopper.jsprit.core.problem.solution.route.activity.TourActivity;
import com.graphhopper.jsprit.core.problem.vehicle.Vehicle;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

public class ShipmentInsertionCalculator_ArrowTest {

    @Test
    public void testToString() {
        VehicleRoutingTransportCosts transportCosts = mock(VehicleRoutingTransportCosts.class);
        VehicleRoutingActivityCosts activityCosts = mock(VehicleRoutingActivityCosts.class);
        ActivityInsertionCostsCalculator costsCalculator = mock(ActivityInsertionCostsCalculator.class);
        ConstraintManager constraintManager = mock(ConstraintManager.class);
        JobActivityFactory jobActivityFactory = mock(JobActivityFactory.class);

        ShipmentInsertionCalculator calculator = new ShipmentInsertionCalculator(transportCosts, activityCosts, costsCalculator, constraintManager, jobActivityFactory);
        assertEquals("[name=calculatesShipmentInsertion]", calculator.toString());
    }

    @Test
    public void testGetInsertionDataNoInsertionFound() {
        VehicleRoutingTransportCosts transportCosts = mock(VehicleRoutingTransportCosts.class);
        VehicleRoutingActivityCosts activityCosts = mock(VehicleRoutingActivityCosts.class);
        ActivityInsertionCostsCalculator costsCalculator = mock(ActivityInsertionCostsCalculator.class);
        ConstraintManager constraintManager = mock(ConstraintManager.class);
        JobActivityFactory jobActivityFactory = mock(JobActivityFactory.class);

        ShipmentInsertionCalculator calculator = new ShipmentInsertionCalculator(transportCosts, activityCosts, costsCalculator, constraintManager, jobActivityFactory);

        VehicleRoute currentRoute = mock(VehicleRoute.class);
        Shipment shipment = mock(Shipment.class);
        Vehicle newVehicle = mock(Vehicle.class);
        Driver newDriver = mock(Driver.class);
        double bestKnownCosts = 100.0;

        // Mocking the constraint manager to return a no insertion found
        when(constraintManager.checkRouteConstraints(any(JobInsertionContext.class))).thenReturn(InsertionData.NO_INSERTION_FOUND);

        InsertionData result = calculator.getInsertionData(currentRoute, shipment, newVehicle, 0.0, newDriver, bestKnownCosts);
        
        assertNotNull(result);
        assertTrue(result instanceof InsertionData.NoInsertionFound);
    }

    @Test
    public void testGetInsertionDataWithValidInsertion() {
        VehicleRoutingTransportCosts transportCosts = mock(VehicleRoutingTransportCosts.class);
        VehicleRoutingActivityCosts activityCosts = mock(VehicleRoutingActivityCosts.class);
        ActivityInsertionCostsCalculator costsCalculator = mock(ActivityInsertionCostsCalculator.class);
        ConstraintManager constraintManager = mock(ConstraintManager.class);
        JobActivityFactory jobActivityFactory = mock(JobActivityFactory.class);

        ShipmentInsertionCalculator calculator = new ShipmentInsertionCalculator(transportCosts, activityCosts, costsCalculator, constraintManager, jobActivityFactory);

        VehicleRoute currentRoute = mock(VehicleRoute.class);
        Shipment shipment = mock(Shipment.class);
        Vehicle newVehicle = mock(Vehicle.class);
        Driver newDriver = mock(Driver.class);
        double bestKnownCosts = 100.0;

        // Setup mocks for successful insertion
        when(constraintManager.checkRouteConstraints(any(JobInsertionContext.class))).thenReturn(null);
        when(shipment.getPickupTimeWindows()).thenReturn(Collections.singletonList(new TimeWindow(0, 10)));
        when(shipment.getDeliveryTimeWindows()).thenReturn(Collections.singletonList(new TimeWindow(10, 20)));

        TourActivity pickupActivity = mock(TourActivity.class);
        TourActivity deliveryActivity = mock(TourActivity.class);
        when(jobActivityFactory.createActivities(shipment)).thenReturn(Arrays.asList(pickupActivity, deliveryActivity));

        // Mock the transport costs and activity costs
        when(transportCosts.getTransportTime(any(), any(), anyDouble(), any(), any())).thenReturn(1.0);
        when(activityCosts.getActivityDuration(any(), anyDouble(), any(), any())).thenReturn(1.0);

        // Mock the costs calculator
        when(costsCalculator.getCosts(any(), any(), any(), any(), anyDouble())).thenReturn(10.0);

        InsertionData result = calculator.getInsertionData(currentRoute, shipment, newVehicle, 0.0, newDriver, bestKnownCosts);
        
        assertNotNull(result);
        assertFalse(result instanceof InsertionData.NoInsertionFound);
        assertEquals(10.0, result.getCosts(), 0.01);
    }
}
