package de.voidnode.trading4j.examples;

import org.junit.Test;

import java.util.List;
import java.util.Optional;

import de.voidnode.trading4j.api.Indicator;
import de.voidnode.trading4j.domain.MarketDirection;
import de.voidnode.trading4j.domain.marketdata.MarketData;
import de.voidnode.trading4j.domain.marketdata.WithOhlc;
import de.voidnode.trading4j.domain.marketdata.WithSpread;
import de.voidnode.trading4j.domain.monetary.Price;
import de.voidnode.trading4j.domain.monetary.PriceUnit;

import static java.util.Arrays.asList;
import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class NMovingAveragesExpertAdvisor_ArrowTest {

    @Test(expected = IllegalArgumentException.class)
    public void throwsExceptionIfNoFastMovingAverageIsProvided() {
        final Indicator<Price, MarketData> slow = mock(Indicator.class);
        new NMovingAveragesExpertAdvisor<>(slow);
    }

    @Test
    public void returnsEmptyEntrySignalIfNoTrendIsDetected() {
        final Indicator<Price, MarketData> slow = mock(Indicator.class);
        final Indicator<Price, MarketData> fast1 = mock(Indicator.class);
        final Indicator<Price, MarketData> fast2 = mock(Indicator.class);
        final NMovingAveragesExpertAdvisor<MarketData> advisor = new NMovingAveragesExpertAdvisor<>(slow, fast1, fast2);

        final MarketData candle = mock(MarketData.class);
        when(slow.indicate(candle)).thenReturn(Optional.of(new Price(100, PriceUnit.PIPETTE)));
        when(fast1.indicate(candle)).thenReturn(Optional.of(new Price(90, PriceUnit.PIPETTE)));
        when(fast2.indicate(candle)).thenReturn(Optional.of(new Price(80, PriceUnit.PIPETTE)));

        advisor.update(candle);

        assertThat(advisor.getEntrySignal(), is(Optional.<MarketDirection>empty()));
    }

    @Test
    public void returnsUpSignalWhenTrendIsUpAndMovingAveragesAreOrderedAscending() {
        final Indicator<Price, MarketData> slow = mock(Indicator.class);
        final Indicator<Price, MarketData> fast1 = mock(Indicator.class);
        final Indicator<Price, MarketData> fast2 = mock(Indicator.class);
        final NMovingAveragesExpertAdvisor<MarketData> advisor = new NMovingAveragesExpertAdvisor<>(slow, fast1, fast2);

        final MarketData candle = mock(MarketData.class);
        when(slow.indicate(candle)).thenReturn(Optional.of(new Price(100, PriceUnit.PIPETTE)));
        when(fast1.indicate(candle)).thenReturn(Optional.of(new Price(110, PriceUnit.PIPETTE)));
        when(fast2.indicate(candle)).thenReturn(Optional.of(new Price(120, PriceUnit.PIPETTE)));

        advisor.update(candle);

        assertThat(advisor.getEntrySignal(), is(Optional.of(MarketDirection.UP)));
    }

    @Test
    public void returnsDownSignalWhenTrendIsDownAndMovingAveragesAreOrderedDescending() {
        final Indicator<Price, MarketData> slow = mock(Indicator.class);
        final Indicator<Price, MarketData> fast1 = mock(Indicator.class);
        final Indicator<Price, MarketData> fast2 = mock(Indicator.class);
        final NMovingAveragesExpertAdvisor<MarketData> advisor = new NMovingAveragesExpertAdvisor<>(slow, fast1, fast2);

        final MarketData candle = mock(MarketData.class);
        when(slow.indicate(candle)).thenReturn(Optional.of(new Price(100, PriceUnit.PIPETTE)));
        when(fast1.indicate(candle)).thenReturn(Optional.of(new Price(90, PriceUnit.PIPETTE)));
        when(fast2.indicate(candle)).thenReturn(Optional.of(new Price(80, PriceUnit.PIPETTE)));

        advisor.update(candle);

        assertThat(advisor.getEntrySignal(), is(Optional.of(MarketDirection.DOWN)));
    }

    @Test
    public void returnsEmptyEntrySignalWhenMovingAveragesAreUnaligned() {
        final Indicator<Price, MarketData> slow = mock(Indicator.class);
        final Indicator<Price, MarketData> fast1 = mock(Indicator.class);
        final Indicator<Price, MarketData> fast2 = mock(Indicator.class);
        final NMovingAveragesExpertAdvisor<MarketData> advisor = new NMovingAveragesExpertAdvisor<>(slow, fast1, fast2);

        final MarketData candle = mock(MarketData.class);
        when(slow.indicate(candle)).thenReturn(Optional.of(new Price(100, PriceUnit.PIPETTE)));
        when(fast1.indicate(candle)).thenReturn(Optional.of(new Price(90, PriceUnit.PIPETTE)));
        when(fast2.indicate(candle)).thenReturn(Optional.of(new Price(95, PriceUnit.PIPETTE)));

        advisor.update(candle);

        assertThat(advisor.getEntrySignal(), is(Optional.<MarketDirection>empty()));
    }

    @Test
    public void returnsCorrectEntryPriceForUpSignal() {
        final Indicator<Price, MarketData> slow = mock(Indicator.class);
        final Indicator<Price, MarketData> fast1 = mock(Indicator.class);
        final Indicator<Price, MarketData> fast2 = mock(Indicator.class);
        final NMovingAveragesExpertAdvisor<MarketData> advisor = new NMovingAveragesExpertAdvisor<>(slow, fast1, fast2);

        final MarketData candle = mock(MarketData.class);
        when(candle.getClose()).thenReturn(new Price(100, PriceUnit.PIPETTE));
        when(candle.getSpread()).thenReturn(new Price(5, PriceUnit.PIPETTE));
        when(slow.indicate(candle)).thenReturn(Optional.of(new Price(100, PriceUnit.PIPETTE)));
        when(fast1.indicate(candle)).thenReturn(Optional.of(new Price(110, PriceUnit.PIPETTE)));
        when(fast2.indicate(candle)).thenReturn(Optional.of(new Price(120, PriceUnit.PIPETTE)));

        advisor.update(candle);

        final Optional<Price> entryPrice = advisor.getEntryPrice();
        assertThat(entryPrice.isPresent(), is(true));
        assertThat(entryPrice.get(), is(new Price(110, PriceUnit.PIPETTE)));
    }

    @Test
    public void returnsCorrectEntryPriceForDownSignal() {
        final Indicator<Price, MarketData> slow = mock(Indicator.class);
        final Indicator<Price, MarketData> fast1 = mock(Indicator.class);
        final Indicator<Price, MarketData> fast2 = mock(Indicator.class);
        final NMovingAveragesExpertAdvisor<MarketData> advisor = new NMovingAveragesExpertAdvisor<>(slow, fast1, fast2);

        final MarketData candle = mock(MarketData.class);
        when(candle.getClose()).thenReturn(new Price(100, PriceUnit.PIPETTE));
        when(candle.getSpread()).thenReturn(new Price(5, PriceUnit.PIPETTE));
        when(slow.indicate(candle)).thenReturn(Optional.of(new Price(100, PriceUnit.PIPETTE)));
        when(fast1.indicate(candle)).thenReturn(Optional.of(new Price(90, PriceUnit.PIPETTE)));
        when(fast2.indicate(candle)).thenReturn(Optional.of(new Price(80, PriceUnit.PIPETTE)));

        advisor.update(candle);

        final Optional<Price> entryPrice = advisor.getEntryPrice();
        assertThat(entryPrice.isPresent(), is(true));
        assertThat(entryPrice.get(), is(new Price(95, PriceUnit.PIPETTE)));
    }

    @Test
    public void returnsNoTakeProfit() {
        final Indicator<Price, MarketData> slow = mock(Indicator.class);
        final Indicator<Price, MarketData> fast1 = mock(Indicator.class);
        final Indicator<Price, MarketData> fast2 = mock(Indicator.class);
        final NMovingAveragesExpertAdvisor<MarketData> advisor = new NMovingAveragesExpertAdvisor<>(slow, fast1, fast2);

        final Optional<Price> takeProfit = advisor.getTakeProfit();
        assertThat(takeProfit.isPresent(), is(true));
        assertThat(takeProfit.get(), is(new Price(0, PriceUnit.PIPETTE)));
    }

    @Test
    public void returnsStopExecutionCondition() {
        final Indicator<Price, MarketData> slow = mock(Indicator.class);
        final Indicator<Price, MarketData> fast1 = mock(Indicator.class);
        final Indicator<Price, MarketData> fast2 = mock(Indicator.class);
        final NMovingAveragesExpertAdvisor<MarketData> advisor = new NMovingAveragesExpertAdvisor<>(slow, fast1, fast2);

        assertThat(advisor.getEntryCondition(), is(ExecutionCondition.STOP));
    }

    @Test
    public void returnsCorrectTrendForUpTrend() {
        final Indicator<Price, MarketData> slow = mock(Indicator.class);
        final Indicator<Price, MarketData> fast1 = mock(Indicator.class);
        final Indicator<Price, MarketData> fast2 = mock(Indicator.class);
        final NMovingAveragesExpertAdvisor<MarketData> advisor = new NMovingAveragesExpertAdvisor<>(slow, fast1, fast2);

        final MarketData candle = mock(MarketData.class);
        when(candle.getHigh()).thenReturn(new Price(110, PriceUnit.PIPETTE));
        when(slow.indicate(candle)).thenReturn(Optional.of(new Price(100, PriceUnit.PIPETTE)));

        advisor.update(candle);

        assertThat(advisor.getTrend(), is(Optional.of(MarketDirection.UP)));
    }

    @Test
    public void returnsCorrectTrendForDownTrend() {
        final Indicator<Price, MarketData> slow = mock(Indicator.class);
        final Indicator<Price, MarketData> fast1 = mock(Indicator.class);
        final Indicator<Price, MarketData> fast2 = mock(Indicator.class);
        final NMovingAveragesExpertAdvisor<MarketData> advisor = new NMovingAveragesExpertAdvisor<>(slow, fast1, fast2);

        final MarketData candle = mock(MarketData.class);
        when(candle.getLow()).thenReturn(new Price(90, PriceUnit.PIPETTE));
        when(slow.indicate(candle)).thenReturn(Optional.of(new Price(100, PriceUnit.PIPETTE)));

        advisor.update(candle);

        assertThat(advisor.getTrend(), is(Optional.of(MarketDirection.DOWN)));
    }

    @Test
    public void returnsEmptyTrendWhenPriceIsWithinMovingAverage() {
        final Indicator<Price, MarketData> slow = mock(Indicator.class);
        final Indicator<Price, MarketData> fast1 = mock(Indicator.class);
        final Indicator<Price, MarketData> fast2 = mock(Indicator.class);
        final NMovingAveragesExpertAdvisor<MarketData> advisor = new NMovingAveragesExpertAdvisor<>(slow, fast1, fast2);

        final MarketData candle = mock(MarketData.class);
        when(candle.getHigh()).thenReturn(new Price(110, PriceUnit.PIPETTE));
        when(candle.getLow()).thenReturn(new Price(90, PriceUnit.PIPETTE));
        when(slow.indicate(candle)).thenReturn(Optional.of(new Price(100, PriceUnit.PIPETTE)));

        advisor.update(candle);

        assertThat(advisor.getTrend(), is(Optional.<MarketDirection>empty()));
    }

    @Test
    public void returnsCorrectStopLossForUpTrend() {
        final Indicator<Price, MarketData> slow = mock(Indicator.class);
        final Indicator<Price, MarketData> fast1 = mock(Indicator.class);
        final Indicator<Price, MarketData> fast2 = mock(Indicator.class);
        final NMovingAveragesExpertAdvisor<MarketData> advisor = new NMovingAveragesExpertAdvisor<>(slow, fast1, fast2);

        final MarketData candle = mock(MarketData.class);
        when(candle.getSpread()).thenReturn(new Price(5, PriceUnit.PIPETTE));
        when(slow.indicate(candle)).thenReturn(Optional.of(new Price(100, PriceUnit.PIPETTE)));

        advisor.update(candle);

        final Optional<Price> stopLoss = advisor.getStopLoose();
        assertThat(stopLoss.isPresent(), is(true));
        assertThat(stopLoss.get(), is(new Price(105, PriceUnit.PIPETTE)));
    }

    @Test
    public void returnsCorrectStopLossForDownTrend() {
        final Indicator<Price, MarketData> slow = mock(Indicator.class);
        final Indicator<Price, MarketData> fast1 = mock(Indicator.class);
        final Indicator<Price, MarketData> fast2 = mock(Indicator.class);
        final NMovingAveragesExpertAdvisor<MarketData> advisor = new NMovingAveragesExpertAdvisor<>(slow, fast1, fast2);

        final MarketData candle = mock(MarketData.class);
        when(candle.getSpread()).thenReturn(new Price(5, PriceUnit.PIPETTE));
        when(slow.indicate(candle)).thenReturn(Optional.of(new Price(100, PriceUnit.PIPETTE)));

        advisor.update(candle);

        final Optional<Price> stopLoss = advisor.getStopLoose();
        assertThat(stopLoss.isPresent(), is(true));
        assertThat(stopLoss.get(), is(new Price(100, PriceUnit.PIPETTE)));
    }

    @Test
    public void returnsEmptyStopLossWhenNoTrendExists() {
        final Indicator<Price, MarketData> slow = mock(Indicator.class);
        final Indicator<Price, MarketData> fast1 = mock(Indicator.class);
        final Indicator<Price, MarketData> fast2 = mock(Indicator.class);
        final NMovingAveragesExpertAdvisor<MarketData> advisor = new NMovingAveragesExpertAdvisor<>(slow, fast1, fast2);

        final MarketData candle = mock(MarketData.class);
        when(slow.indicate(candle)).thenReturn(Optional.of(new Price(100, PriceUnit.PIPETTE)));

        advisor.update(candle);

        assertThat(advisor.getStopLoose(), is(Optional.<Price>empty()));
    }
}
