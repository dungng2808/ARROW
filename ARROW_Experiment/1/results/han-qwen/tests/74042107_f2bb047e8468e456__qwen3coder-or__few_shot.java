package edu.kit.pse.osip.core.model.base;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import org.junit.Test;

public class MixTank_ArrowTest {

    @Test
    public void testMixTankConstructor() {
        // Arrange
        float capacity = 1000f;
        Liquid liquid = new Liquid(100f, 200f, 300f);
        Pipe outPipe = mock(Pipe.class);

        // Act
        MixTank mixTank = new MixTank(capacity, liquid, outPipe);

        // Assert
        assertNotNull(mixTank);
        assertEquals(capacity, mixTank.getCapacity(), 0.0f);
        assertEquals(TankSelector.MIX, mixTank.getType());
        assertEquals(liquid, mixTank.getContent());
        assertEquals(outPipe, mixTank.getOutPipe());
    }

    @Test
    public void testGetMotor() {
        // Arrange
        float capacity = 1000f;
        Liquid liquid = new Liquid(100f, 200f, 300f);
        Pipe outPipe = mock(Pipe.class);
        MixTank mixTank = new MixTank(capacity, liquid, outPipe);

        // Act
        Motor motor = mixTank.getMotor();

        // Assert
        assertNotNull(motor);
    }

    @Test
    public void testReset() {
        // Arrange
        float capacity = 1000f;
        Liquid liquid = new Liquid(100f, 200f, 300f);
        Pipe outPipe = mock(Pipe.class);
        MixTank mixTank = new MixTank(capacity, liquid, outPipe);
        Motor motor = mixTank.getMotor();

        // Act
        mixTank.reset();

        // Assert
        verify(motor).reset();
    }
}
