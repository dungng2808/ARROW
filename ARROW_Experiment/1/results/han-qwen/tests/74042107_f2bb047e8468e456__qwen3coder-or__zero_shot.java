package edu.kit.pse.osip.core.model.base;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertSame;

import org.junit.Test;

public class MixTank_ArrowTest {

    @Test
    public void testConstructor() {
        // Arrange
        float capacity = 1000.0f;
        Liquid liquid = new Liquid("Water", 1000, 1000, 1000);
        Pipe outPipe = new Pipe(100);

        // Act
        MixTank mixTank = new MixTank(capacity, liquid, outPipe);

        // Assert
        assertNotNull(mixTank);
        assertEquals(capacity, mixTank.getCapacity(), 0.0f);
        assertSame(liquid, mixTank.getContent());
        assertSame(outPipe, mixTank.getOutPipe());
        assertNotNull(mixTank.getMotor());
    }

    @Test
    public void testGetMotor() {
        // Arrange
        float capacity = 500.0f;
        Liquid liquid = new Liquid("Ethanol", 500, 500, 500);
        Pipe outPipe = new Pipe(50);
        MixTank mixTank = new MixTank(capacity, liquid, outPipe);

        // Act
        Motor motor = mixTank.getMotor();

        // Assert
        assertNotNull(motor);
        assertSame(mixTank.getMotor(), motor);
    }

    @Test
    public void testReset() {
        // Arrange
        float capacity = 2000.0f;
        Liquid liquid = new Liquid("Milk", 2000, 2000, 2000);
        Pipe outPipe = new Pipe(200);
        MixTank mixTank = new MixTank(capacity, liquid, outPipe);
        Motor motor = mixTank.getMotor();
        
        // Simulate some state changes
        motor.setSpeed(100);
        mixTank.setContent(new Liquid("Coffee", 1000, 1000, 1000));

        // Act
        mixTank.reset();

        // Assert
        assertEquals(capacity, mixTank.getCapacity(), 0.0f);
        assertSame(liquid, mixTank.getContent());
        assertSame(outPipe, mixTank.getOutPipe());
        assertEquals(0, motor.getSpeed());
    }
}
