package pl.zankowski.iextrading4j.client.sse.request.marketdata;

import org.junit.Test;
import pl.zankowski.iextrading4j.api.marketdata.TOPS;
import pl.zankowski.iextrading4j.client.sse.manager.SseRequest;

import javax.ws.rs.core.GenericType;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class TopsSseRequestBuilder_ArrowTest {

    @Test
    public void testBuild() {
        final TopsSseRequestBuilder builder = new TopsSseRequestBuilder();
        final SseRequest<List<TOPS>> request = builder.build();

        assertThat(request.getPath()).isEqualTo("/tops");
        assertThat(request.getResponseType()).isInstanceOf(GenericType.class);
        assertThat(request.getQueryParams()).isEmpty();
    }

    @Test
    public void testBuildWithSymbol() {
        final TopsSseRequestBuilder builder = new TopsSseRequestBuilder();
        builder.withSymbol("AAPL");
        final SseRequest<List<TOPS>> request = builder.build();

        assertThat(request.getPath()).isEqualTo("/tops");
        assertThat(request.getResponseType()).isInstanceOf(GenericType.class);
        assertThat(request.getQueryParams()).containsEntry("symbols", "AAPL");
    }

    @Test
    public void testBuildWithNoSnapshot() {
        final TopsSseRequestBuilder builder = new TopsSseRequestBuilder();
        builder.withNoSnapshot(true);
        final SseRequest<List<TOPS>> request = builder.build();

        assertThat(request.getPath()).isEqualTo("/tops");
        assertThat(request.getResponseType()).isInstanceOf(GenericType.class);
        assertThat(request.getQueryParams()).containsEntry("noSnapshot", "true");
    }

    @Test
    public void testBuildWithSymbolAndNoSnapshot() {
        final TopsSseRequestBuilder builder = new TopsSseRequestBuilder();
        builder.withSymbol("AAPL");
        builder.withNoSnapshot(true);
        final SseRequest<List<TOPS>> request = builder.build();

        assertThat(request.getPath()).isEqualTo("/tops");
        assertThat(request.getResponseType()).isInstanceOf(GenericType.class);
        assertThat(request.getQueryParams()).containsEntry("symbols", "AAPL");
        assertThat(request.getQueryParams()).containsEntry("noSnapshot", "true");
    }

}
