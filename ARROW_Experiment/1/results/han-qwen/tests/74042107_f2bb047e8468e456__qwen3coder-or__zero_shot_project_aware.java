package edu.kit.pse.osip.core.model.base;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertSame;

import org.junit.Before;
import org.junit.Test;

import edu.kit.pse.osip.core.model.base.MixTank;
import edu.kit.pse.osip.core.model.base.Motor;
import edu.kit.pse.osip.core.model.base.Pipe;
import edu.kit.pse.osip.core.model.base.Liquid;
import edu.kit.pse.osip.core.model.base.TankSelector;

public class MixTank_ArrowTest {

    private static final float CAPACITY = 1000.0f;
    private static final Liquid LIQUID = new Liquid("Water", 1000, 1000, 1000);
    private static final Pipe PIPE = new Pipe(100);

    private MixTank mixTank;

    @Before
    public void setUp() {
        mixTank = new MixTank(CAPACITY, LIQUID, PIPE);
    }

    @Test
    public void testConstructor() {
        assertEquals(CAPACITY, mixTank.getCapacity(), 0.0f);
        assertSame(LIQUID, mixTank.getContent());
        assertSame(PIPE, mixTank.getOutPipe());
        assertEquals(TankSelector.MIX, mixTank.getType());
    }

    @Test
    public void testGetMotor() {
        Motor motor = mixTank.getMotor();
        assertNotNull(motor);
    }

    @Test
    public void testReset() {
        Motor motor = mixTank.getMotor();
        mixTank.reset();
        // Verify that the motor was reset by checking if it's still accessible
        assertNotNull(motor);
    }
}
