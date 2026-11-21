package com.graphhopper.jsprit.core.algorithm.recreate;

import com.graphhopper.jsprit.core.problem.constraint.ConstraintManager;
import com.graphhopper.jsprit.core.problem.cost.VehicleRoutingActivityCosts;
import com.graphhopper.jsprit.core.problem.cost.VehicleRoutingTransportCosts;
import com.graphhopper.jsprit.core.problem.driver.Driver;
import com.graphhopper.jsprit.core.problem.job.Shipment;
import com.graphhopper.jsprit.core.problem.job.Job;
import com.graphhopper.jsprit.core.problem.misc.JobInsertionContext;
import com.graphhopper.jsprit.core.problem.solution.route.VehicleRoute;
import com.graphhopper.jsprit.core.problem.solution.route.activity.End;
import com.graphhopper.jsprit.core.problem.solution.route.activity.Start;
import com.graphhopper.jsprit.core.problem.solution.route.activity.TourActivity;
import com.graphhopper.jsprit.core.problem.vehicle.Vehicle;
import com.graphhopper.jsprit.core.problem.job.JobActivityFactory;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

public class ShipmentInsertionCalculator_ArrowTest {

    @Mock
    private VehicleRoutingTransportCosts transportCosts;

    @Mock
    private VehicleRoutingActivityCosts activityCosts;

    @Mock
    private ActivityInsertionCostsCalculator activityInsertionCostsCalculator;

    @Mock
    private ConstraintManager constraintManager;

    @Mock
    private JobActivityFactory activityFactory;

    @Mock
    private VehicleRoute route;

    @Mock
    private Shipment shipment;

    @Mock
    private Vehicle vehicle;

    @Mock
    private Driver driver;

    private ShipmentInsertionCalculator calculator;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        calculator = new ShipmentInsertionCalculator(transportCosts, activityCosts, activityInsertionCostsCalculator, constraintManager, activityFactory);
    }

    @Test
    public void testToString() {
        assertEquals("[name=calculatesShipmentInsertion]", calculator.toString());
    }

    @Test
    public void testGetInsertionDataReturnsNoInsertionFoundWhenRouteConstraintsFail() {
        JobInsertionContext context = new JobInsertionContext(route, shipment, vehicle, driver, 0.0);
        when(constraintManager.checkRouteConstraints(context)).thenReturn(new InsertionData.NoInsertionFound());

        InsertionData result = calculator.getInsertionData(route, shipment, vehicle, 0.0, driver, Double.MAX_VALUE);

        assertNotNull(result);
        assertTrue(result instanceof InsertionData.NoInsertionFound);
    }

    @Test
    public void testGetInsertionDataReturnsNoInsertionFoundWhenNoValidInsertionPoint() {
        when(constraintManager.checkRouteConstraints(any(JobInsertionContext.class))).thenReturn(null);
        when(activityFactory.createActivities(shipment)).thenReturn(new ArrayList<TourActivity>());
        when(shipment.getPickupTimeWindows()).thenReturn(new ArrayList<>());
        when(shipment.getDeliveryTimeWindows()).thenReturn(new ArrayList<>());

        InsertionData result = calculator.getInsertionData(route, shipment, vehicle, 0.0, driver, Double.MAX_VALUE);

        assertNotNull(result);
        assertTrue(result instanceof InsertionData.NoInsertionFound);
    }

    @Test
    public void testGetInsertionDataReturnsInsertionDataWithValidInsertionPoints() {
        // Setup mocks for successful insertion
        TourActivity pickupAct = mock(TourActivity.class);
        TourActivity deliverAct = mock(TourActivity.class);
        TourActivity startAct = mock(Start.class);
        TourActivity endAct = mock(End.class);
        List<TourActivity> activities = new ArrayList<>();
        activities.add(startAct);
        activities.add(endAct);

        when(constraintManager.checkRouteConstraints(any(JobInsertionContext.class))).thenReturn(null);
        when(activityFactory.createActivities(shipment)).thenReturn(new ArrayList<TourActivity>() {{
            add(pickupAct);
            add(deliverAct);
        }});
        when(shipment.getPickupTimeWindows()).thenReturn(new ArrayList<>());
        when(shipment.getDeliveryTimeWindows()).thenReturn(new ArrayList<>());
        when(route.getTourActivities().getActivities()).thenReturn(activities);
        when(activityInsertionCostsCalculator.getCosts(any(JobInsertionContext.class), any(TourActivity.class),
                any(TourActivity.class), any(TourActivity.class), anyDouble())).thenReturn(10.0);
        when(activityCosts.getActivityDuration(any(TourActivity.class), anyDouble(), any(Driver.class), any(Vehicle.class)))
                .thenReturn(5.0);
        when(transportCosts.getTransportTime(any(), any(), anyDouble(), any(Driver.class), any(Vehicle.class)))
                .thenReturn(2.0);

        InsertionData result = calculator.getInsertionData(route, shipment, vehicle, 0.0, driver, Double.MAX_VALUE);

        assertNotNull(result);
        assertFalse(result instanceof InsertionData.NoInsertionFound);
        assertEquals(10.0, result.getCosts(), 0.0);
    }
}
