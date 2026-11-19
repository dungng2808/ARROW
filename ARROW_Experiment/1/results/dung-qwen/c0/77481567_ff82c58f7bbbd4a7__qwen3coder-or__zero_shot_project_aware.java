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

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class NMovingAveragesExpertAdvisor_ArrowTest {

    @Test
    public void shouldThrowExceptionIfNoFastMovingAverageIsPassed() {
        try {
            new NMovingAveragesExpertAdvisor<>(mock(Indicator.class));
            assert false;
        } catch (IllegalArgumentException e) {
            assertThat(e.getMessage()).contains("No fast moving average was passed");
        }
    }

    @Test
    public void shouldReturnEmptyTrendIfSlowMaIsBetweenHighAndLow() {
        final Indicator<Price, CandleStub> slow = mock(Indicator.class);
        final Indicator<Price, CandleStub> fast = mock(Indicator.class);
        final CandleStub candle = new CandleStub();
        candle.setHigh(new Price(100, PriceUnit.PIPETTE));
        candle.setLow(new Price(90, PriceUnit.PIPETTE));
        candle.setClose(new Price(95, PriceUnit.PIPETTE));

        when(slow.indicate(candle)).thenReturn(Optional.of(new Price(97, PriceUnit.PIPETTE)));
        when(fast.indicate(candle)).thenReturn(Optional.of(new Price(96, PriceUnit.PIPETTE)));

        final NMovingAveragesExpertAdvisor<CandleStub> advisor = new NMovingAveragesExpertAdvisor<>(slow, fast);
        advisor.update(candle);

        assertThat(advisor.getTrend()).isEmpty();
    }

    @Test
    public void shouldReturnDownTrendIfSlowMaIsAboveHigh() {
        final Indicator<Price, CandleStub> slow = mock(Indicator.class);
        final Indicator<Price, CandleStub> fast = mock(Indicator.class);
        final CandleStub candle = new CandleStub();
        candle.setHigh(new Price(100, PriceUnit.PIPETTE));
        candle.setLow(new Price(90, PriceUnit.PIPETTE));
        candle.setClose(new Price(95, PriceUnit.PIPETTE));

        when(slow.indicate(candle)).thenReturn(Optional.of(new Price(105, PriceUnit.PIPETTE)));
        when(fast.indicate(candle)).thenReturn(Optional.of(new Price(102, PriceUnit.PIPETTE)));

        final NMovingAveragesExpertAdvisor<CandleStub> advisor = new NMovingAveragesExpertAdvisor<>(slow, fast);
        advisor.update(candle);

        assertThat(advisor.getTrend()).contains(MarketDirection.DOWN);
    }

    @Test
    public void shouldReturnUpTrendIfSlowMaIsBelowLow() {
        final Indicator<Price, CandleStub> slow = mock(Indicator.class);
        final Indicator<Price, CandleStub> fast = mock(Indicator.class);
        final CandleStub candle = new CandleStub();
        candle.setHigh(new Price(100, PriceUnit.PIPETTE));
        candle.setLow(new Price(90, PriceUnit.PIPETTE));
        candle.setClose(new Price(95, PriceUnit.PIPETTE));

        when(slow.indicate(candle)).thenReturn(Optional.of(new Price(85, PriceUnit.PIPETTE)));
        when(fast.indicate(candle)).thenReturn(Optional.of(new Price(88, PriceUnit.PIPETTE)));

        final NMovingAveragesExpertAdvisor<CandleStub> advisor = new NMovingAveragesExpertAdvisor<>(slow, fast);
        advisor.update(candle);

        assertThat(advisor.getTrend()).contains(MarketDirection.UP);
    }

    @Test
    public void shouldReturnEmptySignalIfNotAligned() {
        final Indicator<Price, CandleStub> slow = mock(Indicator.class);
        final Indicator<Price, CandleStub> fast = mock(Indicator.class);
        final CandleStub candle = new CandleStub();
        candle.setSpread(new Price(5, PriceUnit.PIPETTE));
        candle.setHigh(new Price(100, PriceUnit.PIPETTE));
        candle.setLow(new Price(90, PriceUnit.PIPETTE));
        candle.setClose(new Price(95, PriceUnit.PIPETTE));

        when(slow.indicate(candle)).thenReturn(Optional.of(new Price(95, PriceUnit.PIPETTE)));
        when(fast.indicate(candle)).thenReturn(Optional.of(new Price(97, PriceUnit.PIPETTE)));

        final NMovingAveragesExpertAdvisor<CandleStub> advisor = new NMovingAveragesExpertAdvisor<>(slow, fast);
        advisor.update(candle);

        assertThat(advisor.getEntrySignal()).isEmpty();
    }

    @Test
    public void shouldReturnUpSignalWhenAlignmentChangesFromUnalignedToUp() {
        final Indicator<Price, CandleStub> slow = mock(Indicator.class);
        final Indicator<Price, CandleStub> fast = mock(Indicator.class);
        final CandleStub candle = new CandleStub();
        candle.setSpread(new Price(5, PriceUnit.PIPETTE));
        candle.setHigh(new Price(100, PriceUnit.PIPETTE));
        candle.setLow(new Price(90, PriceUnit.PIPETTE));
        candle.setClose(new Price(95, PriceUnit.PIPETTE));

        when(slow.indicate(candle)).thenReturn(Optional.of(new Price(95, PriceUnit.PIPETTE)));
        when(fast.indicate(candle)).thenReturn(Optional.of(new Price(93, PriceUnit.PIPETTE)));

        final NMovingAveragesExpertAdvisor<CandleStub> advisor = new NMovingAveragesExpertAdvisor<>(slow, fast);
        advisor.update(candle);

        assertThat(advisor.getEntrySignal()).contains(MarketDirection.UP);
    }

    @Test
    public void shouldReturnDownSignalWhenAlignmentChangesFromUnalignedToDown() {
        final Indicator<Price, CandleStub> slow = mock(Indicator.class);
        final Indicator<Price, CandleStub> fast = mock(Indicator.class);
        final CandleStub candle = new CandleStub();
        candle.setSpread(new Price(5, PriceUnit.PIPETTE));
        candle.setHigh(new Price(100, PriceUnit.PIPETTE));
        candle.setLow(new Price(90, PriceUnit.PIPETTE));
        candle.setClose(new Price(95, PriceUnit.PIPETTE));

        when(slow.indicate(candle)).thenReturn(Optional.of(new Price(95, PriceUnit.PIPETTE)));
        when(fast.indicate(candle)).thenReturn(Optional.of(new Price(97, PriceUnit.PIPETTE)));

        final NMovingAveragesExpertAdvisor<CandleStub> advisor = new NMovingAveragesExpertAdvisor<>(slow, fast);
        advisor.update(candle);

        assertThat(advisor.getEntrySignal()).contains(MarketDirection.DOWN);
    }

    @Test
    public void shouldReturnEmptySignalWhenAlignmentDoesNotChange() {
        final Indicator<Price, CandleStub> slow = mock(Indicator.class);
        final Indicator<Price, CandleStub> fast = mock(Indicator.class);
        final CandleStub candle = new CandleStub();
        candle.setSpread(new Price(5, PriceUnit.PIPETTE));
        candle.setHigh(new Price(100, PriceUnit.PIPETTE));
        candle.setLow(new Price(90, PriceUnit.PIPETTE));
        candle.setClose(new Price(95, PriceUnit.PIPETTE));

        when(slow.indicate(candle)).thenReturn(Optional.of(new Price(95, PriceUnit.PIPETTE)));
        when(fast.indicate(candle)).thenReturn(Optional.of(new Price(93, PriceUnit.PIPETTE)));

        final NMovingAveragesExpertAdvisor<CandleStub> advisor = new NMovingAveragesExpertAdvisor<>(slow, fast);
        advisor.update(candle); // First update to set oldAlignment
        advisor.update(candle); // Second update to test signal

        assertThat(advisor.getEntrySignal()).isEmpty();
    }

    @Test
    public void shouldReturnEntryPriceForUpSignal() {
        final Indicator<Price, CandleStub> slow = mock(Indicator.class);
        final Indicator<Price, CandleStub> fast = mock(Indicator.class);
        final CandleStub candle = new CandleStub();
        candle.setSpread(new Price(5, PriceUnit.PIPETTE));
        candle.setHigh(new Price(100, PriceUnit.PIPETTE));
        candle.setLow(new Price(90, PriceUnit.PIPETTE));
        candle.setClose(new Price(95, PriceUnit.PIPETTE));

        when(slow.indicate(candle)).thenReturn(Optional.of(new Price(95, PriceUnit.PIPETTE)));
        when(fast.indicate(candle)).thenReturn(Optional.of(new Price(93, PriceUnit.PIPETTE)));

        final NMovingAveragesExpertAdvisor<CandleStub> advisor = new NMovingAveragesExpertAdvisor<>(slow, fast);
        advisor.update(candle);

        assertThat(advisor.getEntryPrice()).contains(new Price(105, PriceUnit.PIPETTE)); // Close + Spread + Distance
    }

    @Test
    public void shouldReturnEntryPriceForDownSignal() {
        final Indicator<Price, CandleStub> slow = mock(Indicator.class);
        final Indicator<Price, CandleStub> fast = mock(Indicator.class);
        final CandleStub candle = new CandleStub();
        candle.setSpread(new Price(5, PriceUnit.PIPETTE));
        candle.setHigh(new Price(100, PriceUnit.PIPETTE));
        candle.setLow(new Price(90, PriceUnit.PIPETTE));
        candle.setClose(new Price(95, PriceUnit.PIPETTE));

        when(slow.indicate(candle)).thenReturn(Optional.of(new Price(95, PriceUnit.PIPETTE)));
        when(fast.indicate(candle)).thenReturn(Optional.of(new Price(97, PriceUnit.PIPETTE)));

        final NMovingAveragesExpertAdvisor<CandleStub> advisor = new NMovingAveragesExpertAdvisor<>(slow, fast);
        advisor.update(candle);

        assertThat(advisor.getEntryPrice()).contains(new Price(85, PriceUnit.PIPETTE)); // Close - Distance
    }

    @Test
    public void shouldReturnNoTakeProfit() {
        final Indicator<Price, CandleStub> slow = mock(Indicator.class);
        final Indicator<Price, CandleStub> fast = mock(Indicator.class);
        final CandleStub candle = new CandleStub();

        when(slow.indicate(candle)).thenReturn(Optional.of(new Price(95, PriceUnit.PIPETTE)));
        when(fast.indicate(candle)).thenReturn(Optional.of(new Price(93, PriceUnit.PIPETTE)));

        final NMovingAveragesExpertAdvisor<CandleStub> advisor = new NMovingAveragesExpertAdvisor<>(slow, fast);
        advisor.update(candle);

        assertThat(advisor.getTakeProfit()).contains(new Price(0, PriceUnit.PIPETTE));
    }

    @Test
    public void shouldReturnStopLossBasedOnTrendAndLastSlow() {
        final Indicator<Price, CandleStub> slow = mock(Indicator.class);
        final Indicator<Price, CandleStub> fast = mock(Indicator.class);
        final CandleStub candle = new CandleStub();
        candle.setSpread(new Price(5, PriceUnit.PIPETTE));
        candle.setHigh(new Price(100, PriceUnit.PIPETTE));
        candle.setLow(new Price(90, PriceUnit.PIPETTE));
        candle.setClose(new Price(95, PriceUnit.PIPETTE));

        when(slow.indicate(candle)).thenReturn(Optional.of(new Price(95, PriceUnit.PIPETTE)));
        when(fast.indicate(candle)).thenReturn(Optional.of(new Price(93, PriceUnit.PIPETTE)));

        final NMovingAveragesExpertAdvisor<CandleStub> advisor = new NMovingAveragesExpertAdvisor<>(slow, fast);
        advisor.update(candle);

        assertThat(advisor.getStopLoose()).contains(new Price(95, PriceUnit.PIPETTE)); // Last slow value
    }

    @Test
    public void shouldReturnStopLossBasedOnTrendAndLastSlowForDownTrend() {
        final Indicator<Price, CandleStub> slow = mock(Indicator.class);
        final Indicator<Price, CandleStub> fast = mock(Indicator.class);
        final CandleStub candle = new CandleStub();
        candle.setSpread(new Price(5, PriceUnit.PIPETTE));
        candle.setHigh(new Price(100, PriceUnit.PIPETTE));
        candle.setLow(new Price(90, PriceUnit.PIPETTE));
        candle.setClose(new Price(95, PriceUnit.PIPETTE));

        when(slow.indicate(candle)).thenReturn(Optional.of(new Price(85, PriceUnit.PIPETTE)));
        when(fast.indicate(candle)).thenReturn(Optional.of(new Price(88, PriceUnit.PIPETTE)));

        final NMovingAveragesExpertAdvisor<CandleStub> advisor = new NMovingAveragesExpertAdvisor<>(slow, fast);
        advisor.update(candle);

        assertThat(advisor.getStopLoose()).contains(new Price(90, PriceUnit.PIPETTE)); // Last slow + Spread
    }

    @Test
    public void shouldReturnStopLossAsEmptyIfNoTrend() {
        final Indicator<Price, CandleStub> slow = mock(Indicator.class);
        final Indicator<Price, CandleStub> fast = mock(Indicator.class);
        final CandleStub candle = new CandleStub();
        candle.setSpread(new Price(5, PriceUnit.PIPETTE));
        candle.setHigh(new Price(100, PriceUnit.PIPETTE));
        candle.setLow(new Price(90, PriceUnit.PIPETTE));
        candle.setClose(new Price(95, PriceUnit.PIPETTE));

        when(slow.indicate(candle)).thenReturn(Optional.of(new Price(95, PriceUnit.PIPETTE)));
        when(fast.indicate(candle)).thenReturn(Optional.of(new Price(93, PriceUnit.PIPETTE)));

        final NMovingAveragesExpertAdvisor<CandleStub> advisor = new NMovingAveragesExpertAdvisor<>(slow, fast);
        advisor.update(candle);

        assertThat(advisor.getStopLoose()).isEmpty();
    }

    @Test
    public void shouldReturnStopExecutionCondition() {
        final Indicator<Price, CandleStub> slow = mock(Indicator.class);
        final Indicator<Price, CandleStub> fast = mock(Indicator.class);
        final CandleStub candle = new CandleStub();

        when(slow.indicate(candle)).thenReturn(Optional.of(new Price(95, PriceUnit.PIPETTE)));
        when(fast.indicate(candle)).thenReturn(Optional.of(new Price(93, PriceUnit.PIPETTE)));

        final NMovingAveragesExpertAdvisor<CandleStub> advisor = new NMovingAveragesExpertAdvisor<>(slow, fast);
        advisor.update(candle);

        assertThat(advisor.getEntryCondition()).isEqualTo(de.voidnode.trading4j.domain.orders.ExecutionCondition.STOP);
    }

    private static class CandleStub implements MarketData, WithOhlc, WithSpread {
        private Price high;
        private Price low;
        private Price close;
        private Price spread;

        @Override
        public Price getHigh() {
            return high;
        }

        public void setHigh(Price high) {
            this.high = high;
        }

        @Override
        public Price getLow() {
            return low;
        }

        public void setLow(Price low) {
            this.low = low;
        }

        @Override
        public Price getClose() {
            return close;
        }

        public void setClose(Price close) {
            this.close = close;
        }

        @Override
        public Price getSpread() {
            return spread;
        }

        public void setSpread(Price spread) {
            this.spread = spread;
        }
    }
}
