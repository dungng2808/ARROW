package il.org.hasadna.siri_client.gtfs.analysis;

import il.org.hasadna.siri_client.gtfs.crud.*;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.*;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

public class GtfsDataManipulations_ArrowTest {

    private GtfsCrud mockGtfsCrud;
    private GtfsDataManipulations gtfsDataManipulations;

    @Before
    public void setUp() {
        mockGtfsCrud = mock(GtfsCrud.class);
        gtfsDataManipulations = new GtfsDataManipulations(mockGtfsCrud);
    }

    @Test
    public void testFilterCalendars_NormalCase() throws IOException {
        LocalDate date = LocalDate.of(2023, 1, 1);
        DayOfWeek dayOfWeek = date.getDayOfWeek();

        Calendar calendar1 = new Calendar(new ServiceId("service1"), LocalDate.of(2022, 1, 1),
                LocalDate.of(2024, 1, 1), Collections.singleton(dayOfWeek));
        Calendar calendar2 = new Calendar(new ServiceId("service2"), LocalDate.of(2022, 1, 1),
                LocalDate.of(2024, 1, 1), Collections.singleton(DayOfWeek.MONDAY));

        Collection<Calendar> calendars = Arrays.asList(calendar1, calendar2);
        when(mockGtfsCrud.getCalendars()).thenReturn(Arrays.asList(calendar1, calendar2).stream());

        Collection<Calendar> result = gtfsDataManipulations.filterCalendars(date);

        assertEquals(1, result.size());
        assertTrue(result.contains(calendar1));
    }

    @Test
    public void testFilterTrips_NormalCase() throws IOException {
        Set<ServiceId> serviceIds = new HashSet<>();
        ServiceId serviceId1 = new ServiceId("service1");
        ServiceId serviceId2 = new ServiceId("service2");
        serviceIds.add(serviceId1);
        serviceIds.add(serviceId2);

        Trip trip1 = new Trip("trip1", serviceId1, "route1");
        Trip trip2 = new Trip("trip2", serviceId2, "route2");
        Trip trip3 = new Trip("trip3", new ServiceId("service3"), "route3");

        when(mockGtfsCrud.getTrips()).thenReturn(Arrays.asList(trip1, trip2, trip3).stream());

        Collection<Trip> result = gtfsDataManipulations.filterTrips(serviceIds);

        assertEquals(2, result.size());
        assertTrue(result.contains(trip1));
        assertTrue(result.contains(trip2));
    }

    @Test
    public void testFilterStopTimes_NormalCase() throws IOException {
        Set<String> tripIds = new HashSet<>();
        tripIds.add("trip1");
        tripIds.add("trip2");

        StopTime stopTime1 = new StopTime("trip1", 1, 101, 10);
        StopTime stopTime2 = new StopTime("trip1", 2, 102, 20);
        StopTime stopTime3 = new StopTime("trip2", 1, 201, 15);
        StopTime stopTime4 = new StopTime("trip2", 3, 203, 30);

        when(mockGtfsCrud.getStopTimes()).thenReturn(Arrays.asList(stopTime1, stopTime2, stopTime3, stopTime4).stream());

        Collection<StopTime> result = gtfsDataManipulations.filterStopTimes(tripIds);

        assertEquals(4, result.size());
        assertTrue(result.contains(stopTime1));
        assertTrue(result.contains(stopTime2));
        assertTrue(result.contains(stopTime3));
        assertTrue(result.contains(stopTime4));
    }

    @Test
    public void testFilterStops_NormalCase() throws IOException {
        Set<Integer> stopIDs = new HashSet<>();
        stopIDs.add(101);
        stopIDs.add(102);
        stopIDs.add(201);

        Stop stop1 = new Stop(101, "stop1", 32.0661, 34.7732);
        Stop stop2 = new Stop(102, "stop2", 32.0662, 34.7733);
        Stop stop3 = new Stop(201, "stop3", 32.0663, 34.7734);

        when(mockGtfsCrud.getStops()).thenReturn(Arrays.asList(stop1, stop2, stop3).stream());

        Collection<Stop> result = gtfsDataManipulations.filterStops(stopIDs);

        assertEquals(3, result.size());
        assertTrue(result.contains(stop1));
        assertTrue(result.contains(stop2));
        assertTrue(result.contains(stop3));
    }

    @Test
    public void testFilterRoutes_NormalCase() throws IOException {
        Map<String, Trip> trips = new HashMap<>();
        trips.put("trip1", new Trip("trip1", new ServiceId("service1"), "route1"));
        trips.put("trip2", new Trip("trip2", new ServiceId("service2"), "route2"));

        Route route1 = new Route("route1", "route1_name", 3, "route1_desc");
        Route route2 = new Route("route2", "route2_name", 3, "route2_desc");
        Route route3 = new Route("route3", "route3_name", 3, "route3_desc");

        when(mockGtfsCrud.getRoutes()).thenReturn(Arrays.asList(route1, route2, route3).stream());

        Map<String, Route> result = gtfsDataManipulations.filterRoutes(trips);

        assertEquals(2, result.size());
        assertTrue(result.containsKey("route1"));
        assertTrue(result.containsKey("route2"));
    }

    @Test
    public void testCombine_NormalCase() throws IOException {
        LocalDate date = LocalDate.of(2023, 1, 1);
        Calendar calendar = new Calendar(new ServiceId("service1"), LocalDate.of(2022, 1, 1),
                LocalDate.of(2024, 1, 1), Collections.singleton(DayOfWeek.SUNDAY));
        Trip trip = new Trip("trip1", new ServiceId("service1"), "route1");
        StopTime stopTime1 = new StopTime("trip1", 1, 101, 10);
        StopTime stopTime2 = new StopTime("trip1", 2, 102, 20);
        Stop stop1 = new Stop(101, "stop1", 32.0661, 34.7732);
        Stop stop2 = new Stop(102, "stop2", 32.0662, 34.7733);
        Route route = new Route("route1", "route1_name", 3, "route1_desc");

        when(mockGtfsCrud.getCalendars()).thenReturn(Arrays.asList(calendar).stream());
        when(mockGtfsCrud.getTrips()).thenReturn(Arrays.asList(trip).stream());
        when(mockGtfsCrud.getStopTimes()).thenReturn(Arrays.asList(stopTime1, stopTime2).stream());
        when(mockGtfsCrud.getStops()).thenReturn(Arrays.asList(stop1, stop2).stream());
        when(mockGtfsCrud.getRoutes()).thenReturn(Arrays.asList(route).stream());

        Collection<GtfsRecord> result = gtfsDataManipulations.combine(date);

        assertEquals(1, result.size());
    }

    @Test
    public void testCombineForSpecificRoute_NormalCase() throws IOException {
        LocalDate date = LocalDate.of(2023, 1, 1);
        Calendar calendar = new Calendar(new ServiceId("service1"), LocalDate.of(2022, 1, 1),
                LocalDate.of(2024, 1, 1), Collections.singleton(DayOfWeek.SUNDAY));
        Trip trip1 = new Trip("trip1", new ServiceId("service1"), "route1");
        Trip trip2 = new Trip("trip2", new ServiceId("service2"), "route2");
        StopTime stopTime1 = new StopTime("trip1", 1, 101, 10);
        StopTime stopTime2 = new StopTime("trip1", 2, 102, 20);
        Stop stop1 = new Stop(101, "stop1", 32.0661, 34.7732);
        Stop stop2 = new Stop(102, "stop2", 32.0662, 34.7733);
        Route route1 = new Route("route1", "route1_name", 3, "route1_desc");
        Route route2 = new Route("route2", "route2_name", 3, "route2_desc");

        when(mockGtfsCrud.getCalendars()).thenReturn(Arrays.asList(calendar).stream());
        when(mockGtfsCrud.getTrips()).thenReturn(Arrays.asList(trip1, trip2).stream());
        when(mockGtfsCrud.getStopTimes()).thenReturn(Arrays.asList(stopTime1, stopTime2).stream());
        when(mockGtfsCrud.getStops()).thenReturn(Arrays.asList(stop1, stop2).stream());
        when(mockGtfsCrud.getRoutes()).thenReturn(Arrays.asList(route1, route2).stream());

        Collection<GtfsRecord> result = gtfsDataManipulations.combineForSpecificRoute(date, "route1");

        assertEquals(1, result.size());
    }
}
