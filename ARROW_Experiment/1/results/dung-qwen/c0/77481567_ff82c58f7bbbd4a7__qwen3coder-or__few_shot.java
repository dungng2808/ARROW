package de.voidnode.trading4j.examples;

import de.voidnode.trading4j.api.Indicator;
import de.voidnode.trading4j.domain.MarketDirection;
import de.voidnode.trading4j.domain.marketdata.MarketData;
import de.voidnode.trading4j.domain.marketdata.WithOhlc;
import de.voidnode.trading4j.domain.marketdata.WithSpread;
import de.voidnode.trading4j.domain.monetary.Price;
import de.voidnode.trading4j.domain.monetary.PriceUnit;
import org.junit.Test;

import java.util.Optional;

import static org.junit.Assert.*;

public class NMovingAveragesExpertAdvisor_ArrowTest {

    @Test
    public void should_throw_exception_when_no_fast_ma_is_passed() {
        try {
            new NMovingAveragesExpertAdvisor<>(null);
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            assertEquals("No fast moving average was passed in the constructor. At least on is requiered.", e.getMessage());
        }
    }

    @Test
    public void should_detect_up_trend_when_slow_ma_above_high() {
        // Setup
        Indicator<Price, TestMarketData> slow = new TestIndicator(new Price(100, PriceUnit.PIPETTE));
        NMovingAveragesExpertAdvisor<TestMarketData> advisor = new NMovingAveragesExpertAdvisor<>(slow);
        
        // Execute
        TestMarketData data = new TestMarketData(new Price(90, PriceUnit.PIPETTE), new Price(110, PriceUnit.PIPETTE),
                new Price(100, PriceUnit.PIPETTE), new Price(5, PriceUnit.PIPETTE));
        advisor.update(data);
        
        // Verify
        assertEquals(Optional.of(MarketDirection.UP), advisor.getTrend());
    }

    @Test
    public void should_detect_down_trend_when_slow_ma_below_low() {
        // Setup
        Indicator<Price, TestMarketData> slow = new TestIndicator(new Price(100, PriceUnit.PIPETTE));
        NMovingAveragesExpertAdvisor<TestMarketData> advisor = new NMovingAveragesExpertAdvisor<>(slow);
        
        // Execute
        TestMarketData data = new TestMarketData(new Price(110, PriceUnit.PIPETTE), new Price(90, PriceUnit.PIPETTE),
                new Price(100, PriceUnit.PIPETTE), new Price(5, PriceUnit.PIPETTE));
        advisor.update(data);
        
        // Verify
        assertEquals(Optional.of(MarketDirection.DOWN), advisor.getTrend());
    }

    @Test
    public void should_not_detect_trend_when_slow_ma_between_high_and_low() {
        // Setup
        Indicator<Price, TestMarketData> slow = new TestIndicator(new Price(100, PriceUnit.PIPETTE));
        NMovingAveragesExpertAdvisor<TestMarketData> advisor = new NMovingAveragesExpertAdvisor<>(slow);
        
        // Execute
        TestMarketData data = new TestMarketData(new Price(95, PriceUnit.PIPETTE), new Price(105, PriceUnit.PIPETTE),
                new Price(100, PriceUnit.PIPETTE), new Price(5, PriceUnit.PIPETTE));
        advisor.update(data);
        
        // Verify
        assertEquals(Optional.empty(), advisor.getTrend());
    }

    @Test
    public void should_detect_up_signal_when_all_moving_averages_aligned_up() {
        // Setup
        Indicator<Price, TestMarketData> slow = new TestIndicator(new Price(100, PriceUnit.PIPETTE));
        Indicator<Price, TestMarketData> fast1 = new TestIndicator(new Price(95, PriceUnit.PIPETTE));
        Indicator<Price, TestMarketData> fast2 = new TestIndicator(new Price(90, PriceUnit.PIPETTE));
        NMovingAveragesExpertAdvisor<TestMarketData> advisor = new NMovingAveragesExpertAdvisor<>(slow, fast1, fast2);
        
        // Execute
        TestMarketData data = new TestMarketData(new Price(90, PriceUnit.PIPETTE), new Price(110, PriceUnit.PIPETTE),
                new Price(100, PriceUnit.PIPETTE), new Price(5, PriceUnit.PIPETTE));
        advisor.update(data);
        
        // Verify
        assertEquals(Optional.of(MarketDirection.UP), advisor.getEntrySignal());
    }

    @Test
    public void should_detect_down_signal_when_all_moving_averages_aligned_down() {
        // Setup
        Indicator<Price, TestMarketData> slow = new TestIndicator(new Price(100, PriceUnit.PIPETTE));
        Indicator<Price, TestMarketData> fast1 = new TestIndicator(new Price(105, PriceUnit.PIPETTE));
        Indicator<Price, TestMarketData> fast2 = new TestIndicator(new Price(110, PriceUnit.PIPETTE));
        NMovingAveragesExpertAdvisor<TestMarketData> advisor = new NMovingAveragesExpertAdvisor<>(slow, fast1, fast2);
        
        // Execute
        TestMarketData data = new TestMarketData(new Price(90, PriceUnit.PIPETTE), new Price(110, PriceUnit.PIPETTE),
                new Price(100, PriceUnit.PIPETTE), new Price(5, PriceUnit.PIPETTE));
        advisor.update(data);
        
        // Verify
        assertEquals(Optional.of(MarketDirection.DOWN), advisor.getEntrySignal());
    }

    @Test
    public void should_not_detect_signal_when_moving_averages_unaligned() {
        // Setup
        Indicator<Price, TestMarketData> slow = new TestIndicator(new Price(100, PriceUnit.PIPETTE));
        Indicator<Price, TestMarketData> fast1 = new TestIndicator(new Price(95, PriceUnit.PIPETTE));
        Indicator<Price, TestMarketData> fast2 = new TestIndicator(new Price(105, PriceUnit.PIPETTE));
        NMovingAveragesExpertAdvisor<TestMarketData> advisor = new NMovingAveragesExpertAdvisor<>(slow, fast1, fast2);
        
        // Execute
        TestMarketData data = new TestMarketData(new Price(90, PriceUnit.PIPETTE), new Price(110, PriceUnit.PIPETTE),
                new Price(100, PriceUnit.PIPETTE), new Price(5, PriceUnit.PIPETTE));
        advisor.update(data);
        
        // Verify
        assertEquals(Optional.empty(), advisor.getEntrySignal());
    }

    @Test
    public void should_calculate_entry_price_for_up_signal() {
        // Setup
        Indicator<Price, TestMarketData> slow = new TestIndicator(new Price(100, PriceUnit.PIPETTE));
        Indicator<Price, TestMarketData> fast1 = new TestIndicator(new Price(95, PriceUnit.PIPETTE));
        Indicator<Price, TestMarketData> fast2 = new TestIndicator(new Price(90, PriceUnit.PIPETTE));
        NMovingAveragesExpertAdvisor<TestMarketData> advisor = new NMovingAveragesExpertAdvisor<>(slow, fast1, fast2);
        
        // Execute
        TestMarketData data = new TestMarketData(new Price(90, PriceUnit.PIPETTE), new Price(110, PriceUnit.PIPETTE),
                new Price(100, PriceUnit.PIPETTE), new Price(5, PriceUnit.PIPETTE));
        advisor.update(data);
        
        // Verify
        Optional<Price> entryPrice = advisor.getEntryPrice();
        assertTrue(entryPrice.isPresent());
        assertEquals(new Price(105, PriceUnit.PIPETTE), entryPrice.get()); // close + spread + distance
    }

    @Test
    public void should_calculate_entry_price_for_down_signal() {
        // Setup
        Indicator<Price, TestMarketData> slow = new TestIndicator(new Price(100, PriceUnit.PIPETTE));
        Indicator<Price, TestMarketData> fast1 = new TestIndicator(new Price(105, PriceUnit.PIPETTE));
        Indicator<Price, TestMarketData> fast2 = new TestIndicator(new Price(110, PriceUnit.PIPETTE));
        NMovingAveragesExpertAdvisor<TestMarketData> advisor = new NMovingAveragesExpertAdvisor<>(slow, fast1, fast2);
        
        // Execute
        TestMarketData data = new TestMarketData(new Price(90, PriceUnit.PIPETTE), new Price(110, PriceUnit.PIPETTE),
                new Price(100, PriceUnit.PIPETTE), new Price(5, PriceUnit.PIPETTE));
        advisor.update(data);
        
        // Verify
        Optional<Price> entryPrice = advisor.getEntryPrice();
        assertTrue(entryPrice.isPresent());
        assertEquals(new Price(95, PriceUnit.PIPETTE), entryPrice.get()); // close - distance
    }

    @Test
    public void should_return_no_take_profit() {
        // Setup
        Indicator<Price, TestMarketData> slow = new TestIndicator(new Price(100, PriceUnit.PIPETTE));
        Indicator<Price, TestMarketData> fast1 = new TestIndicator(new Price(95, PriceUnit.PIPETTE));
        Indicator<Price, TestMarketData> fast2 = new TestIndicator(new Price(90, PriceUnit.PIPETTE));
        NMovingAveragesExpertAdvisor<TestMarketData> advisor = new NMovingAveragesExpertAdvisor<>(slow, fast1, fast2);
        
        // Execute & Verify
        assertEquals(Optional.of(new Price(0)), advisor.getTakeProfit());
    }

    @Test
    public void should_calculate_stop_loss_for_up_trend() {
        // Setup
        Indicator<Price, TestMarketData> slow = new TestIndicator(new Price(100, PriceUnit.PIPETTE));
        Indicator<Price, TestMarketData> fast1 = new TestIndicator(new Price(95, PriceUnit.PIPETTE));
        Indicator<Price, TestMarketData> fast2 = new TestIndicator(new Price(90, PriceUnit.PIPETTE));
        NMovingAveragesExpertAdvisor<TestMarketData> advisor = new NMovingAveragesExpertAdvisor<>(slow, fast1, fast2);
        
        // Execute
        TestMarketData data = new TestMarketData(new Price(90, PriceUnit.PIPETTE), new Price(110, PriceUnit.PIPETTE),
                new Price(100, PriceUnit.PIPETTE), new Price(5, PriceUnit.PIPETTE));
        advisor.update(data);
        
        // Verify
        Optional<Price> stopLoss = advisor.getStopLoose();
        assertTrue(stopLoss.isPresent());
        assertEquals(new Price(105, PriceUnit.PIPETTE), stopLoss.get()); // slow ma + spread
    }

    @Test
    public void should_calculate_stop_loss_for_down_trend() {
        // Setup
        Indicator<Price, TestMarketData> slow = new TestIndicator(new Price(100, PriceUnit.PIPETTE));
        Indicator<Price, TestMarketData> fast1 = new TestIndicator(new Price(105, PriceUnit.PIPETTE));
        Indicator<Price, TestMarketData> fast2 = new TestIndicator(new Price(110, PriceUnit.PIPETTE));
        NMovingAveragesExpertAdvisor<TestMarketData> advisor = new NMovingAveragesExpertAdvisor<>(slow, fast1, fast2);
        
        // Execute
        TestMarketData data = new TestMarketData(new Price(90, PriceUnit.PIPETTE), new Price(110, PriceUnit.PIPETTE),
                new Price(100, PriceUnit.PIPETTE), new Price(5, PriceUnit.PIPETTE));
        advisor.update(data);
        
        // Verify
        Optional<Price> stopLoss = advisor.getStopLoose();
        assertTrue(stopLoss.isPresent());
        assertEquals(new Price(100, PriceUnit.PIPETTE), stopLoss.get()); // slow ma
    }

    @Test
    public void should_return_stop_execution_condition() {
        // Setup
        Indicator<Price, TestMarketData> slow = new TestIndicator(new Price(100, PriceUnit.PIPETTE));
        Indicator<Price, TestMarketData> fast1 = new TestIndicator(new Price(95, PriceUnit.PIPETTE));
        Indicator<Price, TestMarketData> fast2 = new TestIndicator(new Price(90, PriceUnit.PIPETTE));
        NMovingAveragesExpertAdvisor<TestMarketData> advisor = new NMovingAveragesExpertAdvisor<>(slow, fast1, fast2);
        
        // Execute & Verify
        assertEquals(de.voidnode.trading4j.domain.orders.ExecutionCondition.STOP, advisor.getEntryCondition());
    }

    private static class TestMarketData implements MarketData, WithOhlc, WithSpread {
        private final Price open;
        private final Price high;
        private final Price low;
        private final Price close;
        private final Price spread;

        public TestMarketData(Price open, Price high, Price low, Price spread) {
            this.open = open;
            this.high = high;
            this.low = low;
            this.close = close;
            this.spread = spread;
        }

        @Override
        public Price getOpen() {
            return open;
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

    private static class TestIndicator implements Indicator<Price, TestMarketData> {
        private final Price value;

        public TestIndicator(Price value) {
            this.value = value;
        }

        @Override
        public Optional<Price> indicate(TestMarketData input) {
            return Optional.of(value);
        }
    }
}
