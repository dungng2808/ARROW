package de.voidnode.trading4j.examples;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.Test;

import de.voidnode.trading4j.api.Indicator;
import de.voidnode.trading4j.domain.MarketDirection;
import de.voidnode.trading4j.domain.marketdata.MarketData;
import de.voidnode.trading4j.domain.marketdata.WithOhlc;
import de.voidnode.trading4j.domain.marketdata.WithSpread;
import de.voidnode.trading4j.domain.monetary.Price;
import de.voidnode.trading4j.domain.monetary.PriceUnit;
import de.voidnode.trading4j.domain.orders.ExecutionCondition;

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

    @SuppressWarnings("unchecked")
    private Indicator<Price, Candle> indicator(Price value) {
        Indicator<Price, Candle> indicator = mock(Indicator.class);
        when(indicator.indicate(org.mockito.Matchers.<Candle>any())).thenReturn(Optional.of(value));
        return indicator;
    }

    private Candle candle(int high, int low, int close, int spread) {
        return new Candle(new Price(high), new Price(low), new Price(close), new Price(spread));
    }

    @Test
    public void constructorRequiresAtLeastOneFastAverage() {
        Indicator<Price, Candle> slow = indicator(new Price(100));
        try {
            new NMovingAveragesExpertAdvisor<Candle>(slow);
            throw new AssertionError("Expected IllegalArgumentException");
        } catch (IllegalArgumentException expected) {
            assertEquals("No fast moving average was passed in the constructor. At least on is requiered.",
                    expected.getMessage());
        }
    }

    @Test
    public void updateDetectsUpAlignmentAndEntryPrice() {
        NMovingAveragesExpertAdvisor<Candle> advisor = new NMovingAveragesExpertAdvisor<Candle>(
                indicator(new Price(10)), indicator(new Price(20)), indicator(new Price(30)));
        advisor.update(candle(100, 0, 50, 2));

        assertEquals(Optional.of(MarketDirection.UP), advisor.getEntrySignal());
        assertEquals(Optional.of(new Price(67, PriceUnit.PIPETTE)), advisor.getEntryPrice());
    }

    @Test
    public void updateDetectsDownAlignmentAndEntryPrice() {
        NMovingAveragesExpertAdvisor<Candle> advisor = new NMovingAveragesExpertAdvisor<Candle>(
                indicator(new Price(30)), indicator(new Price(20)), indicator(new Price(10)));
        advisor.update(candle(100, 0, 50, 2));

        assertEquals(Optional.of(MarketDirection.DOWN), advisor.getEntrySignal());
        assertEquals(Optional.of(new Price(35, PriceUnit.PIPETTE)), advisor.getEntryPrice());
    }

    @Test
    public void updateSetsTrendAndStopLooseForUpTrend() {
        NMovingAveragesExpertAdvisor<Candle> advisor = new NMovingAveragesExpertAdvisor<Candle>(
                indicator(new Price(10)), indicator(new Price(20)));
        advisor.update(candle(100, 20, 50, 2));

        assertEquals(Optional.of(MarketDirection.UP), advisor.getTrend());
        assertEquals(Optional.of(new Price(10)), advisor.getStopLoose());
    }

    @Test
    public void updateSetsTrendAndStopLooseForDownTrend() {
        NMovingAveragesExpertAdvisor<Candle> advisor = new NMovingAveragesExpertAdvisor<Candle>(
                indicator(new Price(110)), indicator(new Price(100)));
        advisor.update(candle(100, 0, 50, 2));

        assertEquals(Optional.of(MarketDirection.DOWN), advisor.getTrend());
        assertEquals(Optional.of(new Price(112)), advisor.getStopLoose());
    }

    @Test
    public void updateWithSlowAverageInsideCandleHasNoTrend() {
        NMovingAveragesExpertAdvisor<Candle> advisor = new NMovingAveragesExpertAdvisor<Candle>(
                indicator(new Price(50)), indicator(new Price(40)));
        advisor.update(candle(100, 0, 50, 2));

        assertEquals(Optional.empty(), advisor.getTrend());
        assertEquals(Optional.empty(), advisor.getStopLoose());
    }

    @Test
    public void repeatedSameAlignmentDoesNotRepeatSignal() {
        NMovingAveragesExpertAdvisor<Candle> advisor = new NMovingAveragesExpertAdvisor<Candle>(
                indicator(new Price(10)), indicator(new Price(20)));
        Candle candle = candle(100, 0, 50, 2);

        advisor.update(candle);
        advisor.update(candle);

        assertEquals(Optional.empty(), advisor.getEntrySignal());
        assertEquals(Optional.empty(), advisor.getEntryPrice());
    }

    @Test
    public void unalignedUpdateClearsSignal() {
        Indicator<Price, Candle> slow = mock(Indicator.class);
        Indicator<Price, Candle> fast = mock(Indicator.class);
        when(slow.indicate(org.mockito.Matchers.<Candle>any()))
                .thenReturn(Optional.of(new Price(10)), Optional.of(new Price(10)));
        when(fast.indicate(org.mockito.Matchers.<Candle>any()))
                .thenReturn(Optional.of(new Price(20)), Optional.of(new Price(5)));
        NMovingAveragesExpertAdvisor<Candle> advisor = new NMovingAveragesExpertAdvisor<Candle>(slow, fast);

        advisor.update(candle(100, 0, 50, 2));
        advisor.update(candle(100, 0, 50, 2));

        assertEquals(Optional.empty(), advisor.getEntrySignal());
    }

    @Test
    public void gettersReturnDefaultsBeforeUpdate() {
        NMovingAveragesExpertAdvisor<Candle> advisor = new NMovingAveragesExpertAdvisor<Candle>(
                indicator(new Price(10)), indicator(new Price(20)));

        assertEquals(Optional.empty(), advisor.getEntrySignal());
        assertEquals(Optional.empty(), advisor.getTrend());
        assertEquals(Optional.empty(), advisor.getEntryPrice());
        assertEquals(Optional.of(new Price(0)), advisor.getTakeProfit());
        assertEquals(Optional.empty(), advisor.getStopLoose());
        assertEquals(ExecutionCondition.STOP, advisor.getEntryCondition());
    }

    @Test
    public void takeProfitIsAlwaysZero() {
        NMovingAveragesExpertAdvisor<Candle> advisor = new NMovingAveragesExpertAdvisor<Candle>(
                indicator(new Price(10)), indicator(new Price(20)));
        advisor.update(candle(100, 0, 50, 2));

        assertEquals(Optional.of(new Price(0)), advisor.getTakeProfit());
    }
}
