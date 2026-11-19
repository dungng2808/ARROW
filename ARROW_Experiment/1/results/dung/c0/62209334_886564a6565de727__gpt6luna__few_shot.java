package il.org.hasadna.siri_client.gtfs.analysis;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import il.org.hasadna.siri_client.gtfs.crud.GtfsCrud;
import java.util.Collection;
import org.junit.Test;

public class GtfsDataManipulations_ArrowTest {

    @Test
    public void constructorStoresGtfsCrud() {
        GtfsDataManipulations manipulations = new GtfsDataManipulations(null);
        assertEquals(null, manipulations.getGtfsCrud());
    }

    @Test
    public void combineReturnsEmptyCollectionWhenCrudHasNoRecords() throws Exception {
        GtfsCrud crud = new GtfsCrud();
        GtfsDataManipulations manipulations = new GtfsDataManipulations(crud);

        Collection<?> records = manipulations.combine(java.time.LocalDate.of(2024, 1, 1));

        assertNotNull(records);
        assertTrue(records.isEmpty());
    }

    @Test
    public void combineForSpecificRouteReturnsEmptyCollectionWhenCrudHasNoRecords() throws Exception {
        GtfsCrud crud = new GtfsCrud();
        GtfsDataManipulations manipulations = new GtfsDataManipulations(crud);

        Collection<?> records =
                manipulations.combineForSpecificRoute(java.time.LocalDate.of(2024, 1, 1), "route");

        assertNotNull(records);
        assertTrue(records.isEmpty());
    }
}
