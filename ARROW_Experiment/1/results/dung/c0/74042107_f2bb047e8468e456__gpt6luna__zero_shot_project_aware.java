package edu.kit.pse.osip.core.model.base;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertSame;

public class MixTank_ArrowTest {

    @Test
    public void constructorCreatesMotor() {
        MixTank tank = new MixTank(10.0f, null, null);

        assertNotNull(tank.getMotor());
        assertEquals(0, tank.getMotor().getSpeed());
    }

    @Test
    public void getMotorReturnsSameMotor() {
        MixTank tank = new MixTank(10.0f, null, null);
        Motor motor = tank.getMotor();

        assertSame(motor, tank.getMotor());
    }

    @Test
    public void resetResetsMotor() {
        MixTank tank = new MixTank(10.0f, null, null);
        Motor motor = tank.getMotor();

        tank.reset();

        assertEquals(0, motor.getSpeed());
    }
}
