package com.gazbert.bxbot.core.config.exchange;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import com.gazbert.bxbot.domain.exchange.ExchangeConfig;
import com.gazbert.bxbot.domain.exchange.NetworkConfig;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import org.junit.Test;

public class ExchangeApiConfigBuilder_ArrowTest {

  @Test
  public void buildsConfigWithNetworkAuthenticationAndOtherSettings() {
    ExchangeConfig source = new ExchangeConfig();
    source.setName("Arrow");
    source.setAdapter("ArrowAdapter");

    NetworkConfig network = new NetworkConfig();
    network.setConnectionTimeout(2500);
    network.setNonFatalErrorCodes(Arrays.asList(429, 503));
    network.setNonFatalErrorMessages(Arrays.asList("busy", "unavailable"));
    source.setNetworkConfig(network);

    Map<String, String> authentication = new HashMap<String, String>();
    authentication.put("key", "secret");
    source.setAuthenticationConfig(authentication);

    Map<String, String> other = new HashMap<String, String>();
    other.put("mode", "live");
    source.setOtherConfig(other);

    ExchangeConfigImpl result = ExchangeApiConfigBuilder.buildConfig(source);

    assertEquals("Arrow", result.getExchangeName());
    assertEquals("ArrowAdapter", result.getExchangeAdapter());
    assertNotNull(result.getNetworkConfig());
    assertEquals(Integer.valueOf(2500), result.getNetworkConfig().getConnectionTimeout());
    assertEquals(Arrays.asList(429, 503), result.getNetworkConfig().getNonFatalErrorCodes());
    assertEquals(Arrays.asList("busy", "unavailable"),
        result.getNetworkConfig().getNonFatalErrorMessages());
    assertNotNull(result.getAuthenticationConfig());
    assertEquals(authentication, result.getAuthenticationConfig().getItems());
    assertNotNull(result.getOtherConfig());
    assertEquals(other, result.getOtherConfig().getItems());
  }

  @Test
  public void buildsConfigWhenOptionalSettingsAreAbsent() {
    ExchangeConfig source = new ExchangeConfig();
    source.setName("Arrow");
    source.setAdapter("ArrowAdapter");

    ExchangeConfigImpl result = ExchangeApiConfigBuilder.buildConfig(source);

    assertEquals("Arrow", result.getExchangeName());
    assertEquals("ArrowAdapter", result.getExchangeAdapter());
    assertNull(result.getNetworkConfig());
    assertNull(result.getAuthenticationConfig());
    assertNull(result.getOtherConfig());
  }

  @Test
  public void buildsNetworkConfigWithEmptyOptionalErrorLists() {
    ExchangeConfig source = new ExchangeConfig();
    source.setName("Arrow");
    source.setAdapter("ArrowAdapter");

    NetworkConfig network = new NetworkConfig();
    network.setConnectionTimeout(1000);
    network.setNonFatalErrorCodes(Arrays.<Integer>asList());
    network.setNonFatalErrorMessages(Arrays.<String>asList());
    source.setNetworkConfig(network);

    ExchangeConfigImpl result = ExchangeApiConfigBuilder.buildConfig(source);

    assertNotNull(result.getNetworkConfig());
    assertEquals(Integer.valueOf(1000), result.getNetworkConfig().getConnectionTimeout());
    assertNull(result.getNetworkConfig().getNonFatalErrorCodes());
    assertNull(result.getNetworkConfig().getNonFatalErrorMessages());
  }
}
