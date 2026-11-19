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
    public void testFilterCalendars() throws IOException {
        LocalDate date = LocalDate.of(2023, 1, 1);
        DayOfWeek dayOfWeek = date.getDayOfWeek();

        Calendar calendar1 = new Calendar(new ServiceId("service1"), LocalDate.of(2022, 1, 1),
                LocalDate.of(2024, 1, 1), Collections.singleton(dayOfWeek));
        Calendar calendar2 = new Calendar(new ServiceId("service2"), LocalDate.of(2022, 1, 1),
                LocalDate.of(2024, 1, 1), Collections.singleton(DayOfWeek.MONDAY));

        when(mockGtfsCrud.getCalendars()).thenReturn(Stream.of(calendar1, calendar2));

        Collection<Calendar> result = gtfsDataManipulations.filterCalendars(date);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertTrue(result.contains(calendar1));
    }

    @Test
    public void testFilterTrips() throws IOException {
        Set<ServiceId> serviceIds = new HashSet<>();
        serviceIds.add(new ServiceId("service1"));

        Trip trip1 = new Trip(new ServiceId("service1"), "trip1", "route1");
        Trip trip2 = new Trip(new ServiceId("service2"), "trip2", "route2");

        when(mockGtfsCrud.getTrips()).thenReturn(Stream.of(trip1, trip2));

        Collection<Trip> result = gtfsDataManipulations.filterTrips(serviceIds);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertTrue(result.contains(trip1));
    }

    @Test
    public void testFilterStopTimes() throws IOException {
        Set<String> tripIds = new HashSet<>();
        tripIds.add("trip1");

        StopTime stopTime1 = new StopTime("trip1", 1, 100);
        StopTime stopTime2 = new StopTime("trip1", 2, 200);
        StopTime stopTime3 = new StopTime("trip2", 1, 300);

        when(mockGtfsCrud.getStopTimes()).thenReturn(Stream.of(stopTime1, stopTime2, stopTime3));

        Collection<StopTime> result = gtfsDataManipulations.filterStopTimes(tripIds);

        assertNotNull(result);
        assertEquals(2, result.size());
    }

    @Test
    public void testFilterStops() throws IOException {
        Set<Integer> stopIDs = new HashSet<>();
        stopIDs.add(100);
        stopIDs.add(200);

        Stop stop1 = new Stop(100, "stop1");
        Stop stop2 = new Stop(200, "stop2");
        Stop stop3 = new Stop(300, "stop3");

        when(mockGtfsCrud.getStops()).thenReturn(Stream.of(stop1, stop2, stop3));

        Collection<Stop> result = gtfsDataManipulations.filterStops(stopIDs);

        assertNotNull(result);
        assertEquals(2, result.size());
        assertTrue(result.contains(stop1));
        assertTrue(result.contains(stop2));
    }

    @Test
    public void testFilterRoutes() throws IOException {
        Map<String, Trip> trips = new HashMap<>();
        trips.put("trip1", new Trip(new ServiceId("service1"), "trip1", "route1"));
        trips.put("trip2", new Trip(new ServiceId("service2"), "trip2", "route2"));

        Route route1 = new Route("route1", "route1_name");
        Route route2 = new Route("route2", "route2_name");
        Route route3 = new Route("route3", "route3_name");

        when(mockGtfsCrud.getRoutes()).thenReturn(Stream.of(route1, route2, route3));

        Map<String, Route> result = gtfsDataManipulations.filterRoutes(trips);

        assertNotNull(result);
        assertEquals(2, result.size());
        assertTrue(result.containsKey("route1"));
        assertTrue(result.containsKey("route2"));
    }

    @Test
    public void testCombine() throws IOException {
        LocalDate date = LocalDate.of(2023, 1, 1);
        DayOfWeek dayOfWeek = date.getDayOfWeek();

        Calendar calendar = new Calendar(new ServiceId("service1"), LocalDate.of(2022, 1, 1),
                LocalDate.of(2024, 1, 1), Collections.singleton(dayOfWeek));
        Trip trip = new Trip(new ServiceId("service1"), "trip1", "route1");
        StopTime stopTime1 = new StopTime("trip1", 1, 100);
        StopTime stopTime2 = new StopTime("trip1", 2, 200);
        Stop stop1 = new Stop(100, "stop1");
        Stop stop2 = new Stop(200, "stop2");
        Route route = new Route("route1", "route1_name");

        when(mockGtfsCrud.getCalendars()).thenReturn(Stream.of(calendar));
        when(mockGtfsCrud.getTrips()).thenReturn(Stream.of(trip));
        when(mockGtfsCrud.getStopTimes()).thenReturn(Stream.of(stopTime1, stopTime2));
        when(mockGtfsCrud.getStops()).thenReturn(Stream.of(stop1, stop2));
        when(mockGtfsCrud.getRoutes()).thenReturn(Stream.of(route));

        Collection<GtfsRecord> result = gtfsDataManipulations.combine(date);

        assertNotNull(result);
        assertEquals(1, result.size());
    }

    @Test
    public void testCombineForSpecificRoute() throws IOException {
        LocalDate date = LocalDate.of(2023, 1, 1);
        DayOfWeek dayOfWeek = date.getDayOfWeek();

        Calendar calendar = new Calendar(new ServiceId("service1"), LocalDate.of(2022, 1, 1),
                LocalDate.of(2024, 1, 1), Collections.singleton(dayOfWeek));
        Trip trip1 = new Trip(new ServiceId("service1"), "trip1", "route1");
        Trip trip2 = new Trip(new ServiceId("service2"), "trip2", "route2");
        StopTime stopTime1 = new StopTime("trip1", 1, 100);
        StopTime stopTime2 = new StopTime("trip1", 2, 200);
        Stop stop1 = new Stop(100, "stop1");
        Stop stop2 = new Stop(200, "stop2");
        Route route1 = new Route("route1", "route1_name");
        Route route2 = new Route("route2", "route2_name");

        when(mockGtfsCrud.getCalendars()).thenReturn(Stream.of(calendar));
        when(mockGtfsCrud.getTrips()).thenReturn(Stream.of(trip1, trip2));
        when(mockGtfsCrud.getStopTimes()).thenReturn(Stream.of(stopTime1, stopTime2));
        when(mockGtfsCrud.getStops()).thenReturn(Stream.of(stop1, stop2));
        when(mockGtfsCrud.getRoutes()).thenReturn(Stream.of(route1, route2));

        Collection<GtfsRecord> result = gtfsDataManipulations.combineForSpecificRoute(date, "route1");

        assertNotNull(result);
        assertEquals(1, result.size());
    }
}
