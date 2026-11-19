package edu.kit.pse.osip.core.model.base;

import org.junit.Test;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertSame;

public class MixTank_ArrowTest {

    @Test
    public void constructorCreatesMotor() {
        MixTank tank = new MixTank(10.0f, null, null);

        assertNotNull(tank.getMotor());
    }

    @Test
    public void resetKeepsAttachedMotor() {
        MixTank tank = new MixTank(10.0f, null, null);
        Motor motor = tank.getMotor();

        tank.reset();

        assertSame(motor, tank.getMotor());
    }
}
