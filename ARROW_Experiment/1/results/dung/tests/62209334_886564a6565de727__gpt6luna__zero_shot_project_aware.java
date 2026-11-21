package il.org.hasadna.siri_client.gtfs.analysis;

import il.org.hasadna.siri_client.gtfs.crud.GtfsCrud;
import org.junit.Test;

import static org.junit.Assert.assertSame;

public class GtfsDataManipulations_ArrowTest {

    @Test
    public void constructorStoresGtfsCrud() {
        GtfsCrud gtfsCrud = org.mockito.Mockito.mock(GtfsCrud.class);

        GtfsDataManipulations manipulations = new GtfsDataManipulations(gtfsCrud);

        assertSame(gtfsCrud, manipulations.getGtfsCrud());
    }
}
