package il.org.hasadna.siri_client.gtfs.analysis;

import il.org.hasadna.siri_client.gtfs.crud.*;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.io.IOException;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.*;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

public class GtfsDataManipulations_ArrowTest {

    @Mock
    private GtfsCrud gtfsCrud;

    private GtfsDataManipulations gtfsDataManipulations;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        gtfsDataManipulations = new GtfsDataManipulations(gtfsCrud);
    }

    @Test
    public void testFilterCalendars() throws IOException {
        LocalDate date = LocalDate.of(2023, 1, 1);
        DayOfWeek dayOfWeek = date.getDayOfWeek();

        Calendar calendar1 = new Calendar();
        calendar1.setServiceId(new ServiceId("service1"));
        calendar1.setStartDate(LocalDate.of(2022, 1, 1));
        calendar1.setEndDate(LocalDate.of(2024, 1, 1));
        calendar1.setValidDays(Arrays.asList(dayOfWeek));

        Calendar calendar2 = new Calendar();
        calendar2.setServiceId(new ServiceId("service2"));
        calendar2.setStartDate(LocalDate.of(2022, 1, 1));
        calendar2.setEndDate(LocalDate.of(2024, 1, 1));
        calendar2.setValidDays(Arrays.asList(DayOfWeek.MONDAY));

        when(gtfsCrud.getCalendars()).thenReturn(Arrays.asList(calendar1, calendar2).stream());

        Collection<Calendar> result = gtfsDataManipulations.filterCalendars(date);

        assertEquals(1, result.size());
        assertTrue(result.contains(calendar1));
    }

    @Test
    public void testFilterTrips() throws IOException {
        Set<ServiceId> serviceIds = new HashSet<>();
        ServiceId serviceId1 = new ServiceId("service1");
        ServiceId serviceId2 = new ServiceId("service2");
        serviceIds.add(serviceId1);
        serviceIds.add(serviceId2);

        Trip trip1 = new Trip();
        trip1.setTripId("trip1");
        trip1.setServiceId(serviceId1);

        Trip trip2 = new Trip();
        trip2.setTripId("trip2");
        trip2.setServiceId(serviceId2);

        Trip trip3 = new Trip();
        trip3.setTripId("trip3");
        trip3.setServiceId(new ServiceId("service3"));

        when(gtfsCrud.getTrips()).thenReturn(Arrays.asList(trip1, trip2, trip3).stream());

        Collection<Trip> result = gtfsDataManipulations.filterTrips(serviceIds);

        assertEquals(2, result.size());
        assertTrue(result.contains(trip1));
        assertTrue(result.contains(trip2));
    }

    @Test
    public void testFilterStopTimes() throws IOException {
        Set<String> tripIds = new HashSet<>();
        tripIds.add("trip1");
        tripIds.add("trip2");

        StopTime stopTime1 = new StopTime();
        stopTime1.setTripId("trip1");
        stopTime1.setStopSequence(1);
        stopTime1.setStopId(101);

        StopTime stopTime2 = new StopTime();
        stopTime2.setTripId("trip1");
        stopTime2.setStopSequence(2);
        stopTime2.setStopId(102);

        StopTime stopTime3 = new StopTime();
        stopTime3.setTripId("trip2");
        stopTime3.setStopSequence(1);
        stopTime3.setStopId(201);

        StopTime stopTime4 = new StopTime();
        stopTime4.setTripId("trip2");
        stopTime4.setStopSequence(3);
        stopTime4.setStopId(203);

        when(gtfsCrud.getStopTimes()).thenReturn(Arrays.asList(stopTime1, stopTime2, stopTime3, stopTime4).stream());

        Collection<StopTime> result = gtfsDataManipulations.filterStopTimes(tripIds);

        assertEquals(4, result.size());
        assertTrue(result.contains(stopTime1));
        assertTrue(result.contains(stopTime2));
        assertTrue(result.contains(stopTime3));
        assertTrue(result.contains(stopTime4));
    }

    @Test
    public void testFilterStops() throws IOException {
        Set<Integer> stopIDs = new HashSet<>();
        stopIDs.add(101);
        stopIDs.add(102);
        stopIDs.add(201);

        Stop stop1 = new Stop();
        stop1.setStopId(101);
        stop1.setStopName("Stop 1");

        Stop stop2 = new Stop();
        stop2.setStopId(102);
        stop2.setStopName("Stop 2");

        Stop stop3 = new Stop();
        stop3.setStopId(201);
        stop3.setStopName("Stop 3");

        Stop stop4 = new Stop();
        stop4.setStopId(202);
        stop4.setStopName("Stop 4");

        when(gtfsCrud.getStops()).thenReturn(Arrays.asList(stop1, stop2, stop3, stop4).stream());

        Collection<Stop> result = gtfsDataManipulations.filterStops(stopIDs);

        assertEquals(3, result.size());
        assertTrue(result.contains(stop1));
        assertTrue(result.contains(stop2));
        assertTrue(result.contains(stop3));
    }

    @Test
    public void testFilterGtfs() throws IOException {
        LocalDate date = LocalDate.of(2023, 1, 1);
        DayOfWeek dayOfWeek = date.getDayOfWeek();

        Calendar calendar1 = new Calendar();
        calendar1.setServiceId(new ServiceId("service1"));
        calendar1.setStartDate(LocalDate.of(2022, 1, 1));
        calendar1.setEndDate(LocalDate.of(2024, 1, 1));
        calendar1.setValidDays(Arrays.asList(dayOfWeek));

        Trip trip1 = new Trip();
        trip1.setTripId("trip1");
        trip1.setServiceId(new ServiceId("service1"));

        StopTime stopTime1 = new StopTime();
        stopTime1.setTripId("trip1");
        stopTime1.setStopSequence(1);
        stopTime1.setStopId(101);

        StopTime stopTime2 = new StopTime();
        stopTime2.setTripId("trip1");
        stopTime2.setStopSequence(2);
        stopTime2.setStopId(102);

        Stop stop1 = new Stop();
        stop1.setStopId(101);
        stop1.setStopName("Stop 1");

        Stop stop2 = new Stop();
        stop2.setStopId(102);
        stop2.setStopName("Stop 2");

        Route route1 = new Route();
        route1.setRouteId("route1");
        route1.setRouteLongName("Route 1");

        when(gtfsCrud.getCalendars()).thenReturn(Arrays.asList(calendar1).stream());
        when(gtfsCrud.getTrips()).thenReturn(Arrays.asList(trip1).stream());
        when(gtfsCrud.getStopTimes()).thenReturn(Arrays.asList(stopTime1, stopTime2).stream());
        when(gtfsCrud.getStops()).thenReturn(Arrays.asList(stop1, stop2).stream());
        when(gtfsCrud.getRoutes()).thenReturn(Arrays.asList(route1).stream());

        gtfsDataManipulations.filterGtfs(date);

        assertNotNull(gtfsDataManipulations.getCalendars());
        assertNotNull(gtfsDataManipulations.getTrips());
        assertNotNull(gtfsDataManipulations.getStopTimes());
        assertNotNull(gtfsDataManipulations.getStops());
        assertNotNull(gtfsDataManipulations.getRoutes());

        assertEquals(1, gtfsDataManipulations.getCalendars().size());
        assertEquals(1, gtfsDataManipulations.getTrips().size());
        assertEquals(1, gtfsDataManipulations.getStopTimes().size());
        assertEquals(2, gtfsDataManipulations.getStops().size());
        assertEquals(1, gtfsDataManipulations.getRoutes().size());
    }

    @Test
    public void testCombine() throws IOException {
        LocalDate date = LocalDate.of(2023, 1, 1);
        DayOfWeek dayOfWeek = date.getDayOfWeek();

        Calendar calendar1 = new Calendar();
        calendar1.setServiceId(new ServiceId("service1"));
        calendar1.setStartDate(LocalDate.of(2022, 1, 1));
        calendar1.setEndDate(LocalDate.of(2024, 1, 1));
        calendar1.setValidDays(Arrays.asList(dayOfWeek));

        Trip trip1 = new Trip();
        trip1.setTripId("trip1");
        trip1.setServiceId(new ServiceId("service1"));
        trip1.setRouteId("route1");

        StopTime stopTime1 = new StopTime();
        stopTime1.setTripId("trip1");
        stopTime1.setStopSequence(1);
        stopTime1.setStopId(101);

        StopTime stopTime2 = new StopTime();
        stopTime2.setTripId("trip1");
        stopTime2.setStopSequence(2);
        stopTime2.setStopId(102);

        Stop stop1 = new Stop();
        stop1.setStopId(101);
        stop1.setStopName("Stop 1");

        Stop stop2 = new Stop();
        stop2.setStopId(102);
        stop2.setStopName("Stop 2");

        Route route1 = new Route();
        route1.setRouteId("route1");
        route1.setRouteLongName("Route 1");

        when(gtfsCrud.getCalendars()).thenReturn(Arrays.asList(calendar1).stream());
        when(gtfsCrud.getTrips()).thenReturn(Arrays.asList(trip1).stream());
        when(gtfsCrud.getStopTimes()).thenReturn(Arrays.asList(stopTime1, stopTime2).stream());
        when(gtfsCrud.getStops()).thenReturn(Arrays.asList(stop1, stop2).stream());
        when(gtfsCrud.getRoutes()).thenReturn(Arrays.asList(route1).stream());

        Collection<GtfsRecord> result = gtfsDataManipulations.combine(date);

        assertEquals(1, result.size());
    }

    @Test
    public void testCombineForSpecificRoute() throws IOException {
        LocalDate date = LocalDate.of(2023, 1, 1);
        DayOfWeek dayOfWeek = date.getDayOfWeek();

        Calendar calendar1 = new Calendar();
        calendar1.setServiceId(new ServiceId("service1"));
        calendar1.setStartDate(LocalDate.of(2022, 1, 1));
        calendar1.setEndDate(LocalDate.of(2024, 1, 1));
        calendar1.setValidDays(Arrays.asList(dayOfWeek));

        Trip trip1 = new Trip();
        trip1.setTripId("trip1");
        trip1.setServiceId(new ServiceId("service1"));
        trip1.setRouteId("route1");

        Trip trip2 = new Trip();
        trip2.setTripId("trip2");
        trip2.setServiceId(new ServiceId("service2"));
        trip2.setRouteId("route2");

        StopTime stopTime1 = new StopTime();
        stopTime1.setTripId("trip1");
        stopTime1.setStopSequence(1);
        stopTime1.setStopId(101);

        StopTime stopTime2 = new StopTime();
        stopTime2.setTripId("trip1");
        stopTime2.setStopSequence(2);
        stopTime2.setStopId(102);

        Stop stop1 = new Stop();
        stop1.setStopId(101);
        stop1.setStopName("Stop 1");

        Stop stop2 = new Stop();
        stop2.setStopId(102);
        stop2.setStopName("Stop 2");

        Route route1 = new Route();
        route1.setRouteId("route1");
        route1.setRouteLongName("Route 1");

        Route route2 = new Route();
        route2.setRouteId("route2");
        route2.setRouteLongName("Route 2");

        when(gtfsCrud.getCalendars()).thenReturn(Arrays.asList(calendar1).stream());
        when(gtfsCrud.getTrips()).thenReturn(Arrays.asList(trip1, trip2).stream());
        when(gtfsCrud.getStopTimes()).thenReturn(Arrays.asList(stopTime1, stopTime2).stream());
        when(gtfsCrud.getStops()).thenReturn(Arrays.asList(stop1, stop2).stream());
        when(gtfsCrud.getRoutes()).thenReturn(Arrays.asList(route1, route2).stream());

        Collection<GtfsRecord> result = gtfsDataManipulations.combineForSpecificRoute(date, "route1");

        assertEquals(1, result.size());
    }
}
