package pl.zankowski.iextrading4j.client.sse.request.marketdata;

import org.junit.Test;
import pl.zankowski.iextrading4j.api.marketdata.TOPS;
import pl.zankowski.iextrading4j.client.sse.manager.SseRequest;
import pl.zankowski.iextrading4j.client.sse.request.AbstractSymbolSseRequestBuilder;

import javax.ws.rs.core.GenericType;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class TopsSseRequestBuilder_ArrowTest {

    @Test
    public void testBuildWithoutSymbol() {
        final TopsSseRequestBuilder builder = new TopsSseRequestBuilder();
        final SseRequest<List<TOPS>> request = builder.build();

        assertThat(request).isNotNull();
        assertThat(request.getPath()).isEqualTo("/tops");
        assertThat(request.getResponseType()).isInstanceOf(GenericType.class);
        assertThat(request.getQueryParams()).isEmpty();
    }

    @Test
    public void testBuildWithSymbol() {
        final TopsSseRequestBuilder builder = new TopsSseRequestBuilder();
        builder.withSymbol("AAPL");
        final SseRequest<List<TOPS>> request = builder.build();

        assertThat(request).isNotNull();
        assertThat(request.getPath()).isEqualTo("/tops");
        assertThat(request.getResponseType()).isInstanceOf(GenericType.class);
        assertThat(request.getQueryParams()).hasSize(1);
        assertThat(request.getQueryParams().get(0).getName()).isEqualTo("symbols");
        assertThat(request.getQueryParams().get(0).getValue()).isEqualTo("AAPL");
    }

    @Test
    public void testBuildWithNoSnapshot() {
        final TopsSseRequestBuilder builder = new TopsSseRequestBuilder();
        builder.withNoSnapshot(true);
        final SseRequest<List<TOPS>> request = builder.build();

        assertThat(request).isNotNull();
        assertThat(request.getPath()).isEqualTo("/tops");
        assertThat(request.getResponseType()).isInstanceOf(GenericType.class);
        assertThat(request.getQueryParams()).hasSize(1);
        assertThat(request.getQueryParams().get(0).getName()).isEqualTo("noSnapshot");
        assertThat(request.getQueryParams().get(0).getValue()).isEqualTo("true");
    }

    @Test
    public void testBuildWithSymbolAndNoSnapshot() {
        final TopsSseRequestBuilder builder = new TopsSseRequestBuilder();
        builder.withSymbol("AAPL");
        builder.withNoSnapshot(true);
        final SseRequest<List<TOPS>> request = builder.build();

        assertThat(request).isNotNull();
        assertThat(request.getPath()).isEqualTo("/tops");
        assertThat(request.getResponseType()).isInstanceOf(GenericType.class);
        assertThat(request.getQueryParams()).hasSize(2);
        assertThat(request.getQueryParams().get(0).getName()).isEqualTo("symbols");
        assertThat(request.getQueryParams().get(0).getValue()).isEqualTo("AAPL");
        assertThat(request.getQueryParams().get(1).getName()).isEqualTo("noSnapshot");
        assertThat(request.getQueryParams().get(1).getValue()).isEqualTo("true");
    }

    @Test
    public void testGenericBuilderMethods() {
        final TopsSseRequestBuilder builder = new TopsSseRequestBuilder();
        final TopsSseRequestBuilder returnedBuilder = builder.withSymbol("AAPL");

        assertThat(returnedBuilder).isSameAs(builder);
    }
}
