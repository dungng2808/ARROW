package de.voidnode.trading4j.examples;

import de.voidnode.trading4j.api.Indicator;
import de.voidnode.trading4j.domain.MarketDirection;
import de.voidnode.trading4j.domain.marketdata.MarketData;
import de.voidnode.trading4j.domain.marketdata.WithOhlc;
import de.voidnode.trading4j.domain.marketdata.WithSpread;
import de.voidnode.trading4j.domain.monetary.Price;
import de.voidnode.trading4j.domain.monetary.PriceUnit;
import de.voidnode.trading4j.domain.orders.ExecutionCondition;
import java.util.Optional;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class NMovingAveragesExpertAdvisor_ArrowTest {

    private static class Candle implements MarketData, WithOhlc, WithSpread {
        private final Price high;
        private final Price low;
        private final Price close;
        private final Price spread;

        Candle(Price high, Price low, Price close, Price spread) {
            this.high = high;
            this.low = low;
            this.close = close;
            this.spread = spread;
        }

        @Override
        public Price getHigh() {
            return high;
        }

        @Override
        public Price getLow() {
            return low;
        }

        @Override
        public Price getClose() {
            return close;
        }

        @Override
        public Price getSpread() {
            return spread;
        }
    }

    private static class FixedIndicator implements Indicator<Price, Candle> {
        private final Price value;

        FixedIndicator(Price value) {
            this.value = value;
        }

        @Override
        public Optional<Price> indicate(Candle candle) {
            return Optional.of(value);
        }
    }

    @Test
    public void constructorRequiresAtLeastOneFastAverage() {
        try {
            new NMovingAveragesExpertAdvisor<Candle>(new FixedIndicator(new Price(100)));
            throw new AssertionError("Expected IllegalArgumentException");
        } catch (IllegalArgumentException expected) {
            assertEquals("No fast moving average was passed in the constructor. At least on is requiered.",
                    expected.getMessage());
        }
    }

    @Test
    public void alignedUpProducesSignalAndEntryPrice() {
        NMovingAveragesExpertAdvisor<Candle> advisor = new NMovingAveragesExpertAdvisor<>(
                new FixedIndicator(new Price(100)),
                new FixedIndicator(new Price(110)));
        Candle candle = new Candle(new Price(120), new Price(90), new Price(105), new Price(2));

        advisor.update(candle);

        assertEquals(Optional.of(MarketDirection.UP), advisor.getEntrySignal());
        assertEquals(Optional.of(new Price(122, PriceUnit.PIPETTE)), advisor.getEntryPrice());
        assertEquals(Optional.of(new Price(0)), advisor.getTakeProfit());
        assertEquals(ExecutionCondition.STOP, advisor.getEntryCondition());
    }

    @Test
    public void alignedDownProducesSignalAndEntryPrice() {
        NMovingAveragesExpertAdvisor<Candle> advisor = new NMovingAveragesExpertAdvisor<>(
                new FixedIndicator(new Price(110)),
                new FixedIndicator(new Price(100)));
        Candle candle = new Candle(new Price(120), new Price(90), new Price(105), new Price(2));

        advisor.update(candle);

        assertEquals(Optional.of(MarketDirection.DOWN), advisor.getEntrySignal());
        assertEquals(Optional.of(new Price(90, PriceUnit.PIPETTE)), advisor.getEntryPrice());
    }

    @Test
    public void trendAndStopLooseFollowSlowAverageAndSpread() {
        NMovingAveragesExpertAdvisor<Candle> advisor = new NMovingAveragesExpertAdvisor<>(
                new FixedIndicator(new Price(80)),
                new FixedIndicator(new Price(90)));
        advisor.update(new Candle(new Price(100), new Price(85), new Price(95), new Price(3)));

        assertEquals(Optional.of(MarketDirection.UP), advisor.getTrend());
        assertEquals(Optional.of(new Price(80)), advisor.getStopLoose());
    }

    @Test
    public void repeatedAlignmentDoesNotProduceAnotherSignal() {
        NMovingAveragesExpertAdvisor<Candle> advisor = new NMovingAveragesExpertAdvisor<>(
                new FixedIndicator(new Price(100)),
                new FixedIndicator(new Price(110)));
        Candle candle = new Candle(new Price(120), new Price(90), new Price(105), new Price(2));

        advisor.update(candle);
        advisor.update(candle);

        assertFalse(advisor.getEntrySignal().isPresent());
        assertFalse(advisor.getEntryPrice().isPresent());
    }

    @Test
    public void unalignedAveragesProduceNoSignal() {
        NMovingAveragesExpertAdvisor<Candle> advisor = new NMovingAveragesExpertAdvisor<>(
                new FixedIndicator(new Price(100)),
                new FixedIndicator(new Price(90)));
        advisor.update(new Candle(new Price(120), new Price(80), new Price(105), new Price(2)));

        assertTrue(advisor.getEntrySignal().isPresent());
        assertEquals(Optional.of(MarketDirection.DOWN), advisor.getEntrySignal());
    }
}
