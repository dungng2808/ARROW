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

        Candle(int high, int low, int close, int spread) {
            this.high = new Price(high);
            this.low = new Price(low);
            this.close = new Price(close);
            this.spread = new Price(spread);
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
        private final Optional<Price> value;

        FixedIndicator(int value) {
            this.value = Optional.of(new Price(value));
        }

        FixedIndicator() {
            this.value = Optional.empty();
        }

        @Override
        public Optional<Price> indicate(Candle candle) {
            return value;
        }
    }

    @Test
    public void constructorRejectsNoFastMovingAverages() {
        try {
            new NMovingAveragesExpertAdvisor<Candle>(new FixedIndicator());
            throw new AssertionError("Expected IllegalArgumentException");
        } catch (IllegalArgumentException expected) {
            assertEquals("No fast moving average was passed in the constructor. At least on is requiered.",
                    expected.getMessage());
        }
    }

    @Test
    public void initiallyHasNoSignalsOrPricesAndUsesStopCondition() {
        NMovingAveragesExpertAdvisor<Candle> advisor =
                new NMovingAveragesExpertAdvisor<Candle>(new FixedIndicator(), new FixedIndicator());

        assertFalse(advisor.getEntrySignal().isPresent());
        assertFalse(advisor.getTrend().isPresent());
        assertFalse(advisor.getEntryPrice().isPresent());
        assertFalse(advisor.getStopLoose().isPresent());
        assertEquals(Optional.of(new Price(0)), advisor.getTakeProfit());
        assertEquals(ExecutionCondition.STOP, advisor.getEntryCondition());
    }

    @Test
    public void alignedAscendingAveragesProduceUpSignalAndEntryPrice() {
        NMovingAveragesExpertAdvisor<Candle> advisor =
                new NMovingAveragesExpertAdvisor<Candle>(
                        new FixedIndicator(100), new FixedIndicator(110), new FixedIndicator(120));

        advisor.update(new Candle(130, 90, 100, 2));

        assertEquals(Optional.of(MarketDirection.UP), advisor.getEntrySignal());
        assertEquals(Optional.of(new Price(117, PriceUnit.PIPETTE)), advisor.getEntryPrice());
        assertFalse(advisor.getTrend().isPresent());
        assertFalse(advisor.getStopLoose().isPresent());
    }

    @Test
    public void alignedDescendingAveragesProduceDownSignalAndTrendStop() {
        NMovingAveragesExpertAdvisor<Candle> advisor =
                new NMovingAveragesExpertAdvisor<Candle>(
                        new FixedIndicator(140), new FixedIndicator(130), new FixedIndicator(120));

        advisor.update(new Candle(130, 100, 150, 2));

        assertEquals(Optional.of(MarketDirection.DOWN), advisor.getEntrySignal());
        assertEquals(Optional.of(new Price(135, PriceUnit.PIPETTE)), advisor.getEntryPrice());
        assertEquals(Optional.of(MarketDirection.DOWN), advisor.getTrend());
        assertEquals(Optional.of(new Price(142)), advisor.getStopLoose());
    }

    @Test
    public void repeatedSameAlignmentDoesNotRepeatSignal() {
        NMovingAveragesExpertAdvisor<Candle> advisor =
                new NMovingAveragesExpertAdvisor<Candle>(
                        new FixedIndicator(100), new FixedIndicator(110));

        advisor.update(new Candle(130, 90, 100, 2));
        assertEquals(Optional.of(MarketDirection.UP), advisor.getEntrySignal());

        advisor.update(new Candle(130, 90, 101, 2));
        assertFalse(advisor.getEntrySignal().isPresent());
    }

    @Test
    public void missingIndicatorValuePreventsSignalButSlowValueStillSetsTrend() {
        NMovingAveragesExpertAdvisor<Candle> advisor =
                new NMovingAveragesExpertAdvisor<Candle>(new FixedIndicator(140), new FixedIndicator());

        advisor.update(new Candle(130, 100, 150, 2));

        assertFalse(advisor.getEntrySignal().isPresent());
        assertEquals(Optional.of(MarketDirection.DOWN), advisor.getTrend());
        assertEquals(Optional.of(new Price(142)), advisor.getStopLoose());
    }

    @Test
    public void absentSlowValueLeavesPreviousTrendButClearsStopPrice() {
        NMovingAveragesExpertAdvisor<Candle> advisor =
                new NMovingAveragesExpertAdvisor<Candle>(new FixedIndicator(140), new FixedIndicator(130));

        advisor.update(new Candle(130, 100, 150, 2));
        assertEquals(Optional.of(MarketDirection.DOWN), advisor.getTrend());

        NMovingAveragesExpertAdvisor<Candle> noSlowAdvisor =
                new NMovingAveragesExpertAdvisor<Candle>(new FixedIndicator(), new FixedIndicator(130));
        noSlowAdvisor.update(new Candle(130, 100, 150, 2));

        assertFalse(noSlowAdvisor.getTrend().isPresent());
        assertFalse(noSlowAdvisor.getStopLoose().isPresent());
    }

    @Test
    public void unalignedAveragesClearSignal() {
        NMovingAveragesExpertAdvisor<Candle> advisor =
                new NMovingAveragesExpertAdvisor<Candle>(
                        new FixedIndicator(100), new FixedIndicator(110), new FixedIndicator(105));

        advisor.update(new Candle(130, 90, 100, 2));

        assertFalse(advisor.getEntrySignal().isPresent());
        assertFalse(advisor.getEntryPrice().isPresent());
    }

    @Test
    public void ascendingAlignmentWithUpTrendUsesSpreadAndPipetteForStopEntry() {
        NMovingAveragesExpertAdvisor<Candle> advisor =
                new NMovingAveragesExpertAdvisor<Candle>(
                        new FixedIndicator(80), new FixedIndicator(90));

        advisor.update(new Candle(100, 90, 100, 2));

        assertEquals(Optional.of(MarketDirection.UP), advisor.getTrend());
        assertEquals(Optional.of(MarketDirection.UP), advisor.getEntrySignal());
        assertEquals(Optional.of(new Price(117, PriceUnit.PIPETTE)), advisor.getEntryPrice());
        assertEquals(Optional.of(new Price(80)), advisor.getStopLoose());
    }
}
