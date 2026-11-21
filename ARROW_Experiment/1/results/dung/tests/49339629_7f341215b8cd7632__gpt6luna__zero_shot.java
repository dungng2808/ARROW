package io.novaordis.playground.java.network.traffic;

import java.net.InetAddress;
import java.util.Collections;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

public class Configuration_ArrowTest {

    @Test
    public void parsesModeProtocolAndPayload() throws Exception {
        Configuration configuration = new Configuration(
                new String[] {"send", "--protocol=udp", "hello", "world"});

        assertEquals(Mode.SEND, configuration.getMode());
        assertEquals(Protocol.UDP, configuration.getProtocol());
        assertEquals("hello world", configuration.getPayload());
        assertNull(configuration.getAddress());
        assertNull(configuration.getPort());
    }

    @Test
    public void parsesReceiveOptions() throws Exception {
        Configuration configuration = new Configuration(new String[] {
                "receive", "--protocol=udp", "--interface=lo", "--port=1234",
                "--local-address=127.0.0.1", "--local-port=4321"
        });

        assertEquals(Mode.RECEIVE, configuration.getMode());
        assertEquals(Protocol.UDP, configuration.getProtocol());
        assertEquals("lo", configuration.getInterface());
        assertEquals(Integer.valueOf(1234), configuration.getPort());
        assertEquals("127.0.0.1", configuration.getLocalAddress());
        assertEquals(Integer.valueOf(4321), configuration.getLocalPort());
    }

    @Test
    public void missingOptionalValuesAreNull() throws Exception {
        Configuration configuration = new Configuration(new String[] {"send", "--protocol=tcp"});

        assertNull(configuration.getAddress());
        assertNull(configuration.getInetAddress());
        assertNull(configuration.getInterface());
        assertNull(configuration.getNetworkInterface());
        assertNull(configuration.getLocalAddress());
        assertNull(configuration.getLocalInetAddress());
        assertNull(configuration.getLocalPort());
        assertNull(configuration.getPort());
        assertNull(configuration.getPayload());
        assertEquals(Collections.emptyList(), configuration.getNetworkInterfaceAddresses());
        assertNull(configuration.getInterfaceName());
    }

    @Test
    public void resolvesSendAddressAndEmbeddedPort() throws Exception {
        Configuration configuration = new Configuration(new String[] {
                "send", "--protocol=udp", "--address=127.0.0.1:8080"
        });

        configuration.validate();

        assertEquals("127.0.0.1:8080", configuration.getAddress());
        assertEquals(InetAddress.getByName("127.0.0.1"), configuration.getInetAddress());
        assertEquals(Integer.valueOf(8080), configuration.getPort());
    }

    @Test
    public void resolvesLocalAddress() throws Exception {
        Configuration configuration = new Configuration(new String[] {
                "send", "--protocol=udp", "--address=127.0.0.1", "--local-address=127.0.0.2"
        });

        configuration.validate();

        assertEquals(InetAddress.getByName("127.0.0.2"), configuration.getLocalInetAddress());
    }

    @Test
    public void toStringIncludesConfiguredValues() throws Exception {
        Configuration configuration = new Configuration(new String[] {
                "send", "--protocol=udp", "--address=127.0.0.1", "payload"
        });

        String result = configuration.toString();

        assertTrue(result.contains("mode                        SEND"));
        assertTrue(result.contains("protocol                    UDP"));
        assertTrue(result.contains("address                     127.0.0.1"));
        assertTrue(result.contains("port                        null"));
    }

    @Test(expected = UserErrorException.class)
    public void rejectsMissingMode() throws Exception {
        new Configuration(new String[0]);
    }

    @Test(expected = UserErrorException.class)
    public void rejectsUnknownMode() throws Exception {
        new Configuration(new String[] {"unknown"});
    }

    @Test(expected = UserErrorException.class)
    public void rejectsUnknownOption() throws Exception {
        new Configuration(new String[] {"send", "--unknown"});
    }

    @Test(expected = UserErrorException.class)
    public void rejectsNonIntegerPort() throws Exception {
        new Configuration(new String[] {"receive", "--protocol=udp", "--port=abc"});
    }

    @Test(expected = UserErrorException.class)
    public void rejectsMissingProtocolDuringValidation() throws Exception {
        Configuration configuration = new Configuration(new String[] {"send"});
        configuration.validate();
    }

    @Test(expected = UserErrorException.class)
    public void rejectsMissingReceiveInterfaceDuringValidation() throws Exception {
        Configuration configuration = new Configuration(new String[] {
                "receive", "--protocol=udp", "--port=1234"
        });
        configuration.validate();
    }

    @Test(expected = UserErrorException.class)
    public void rejectsMissingReceivePortDuringValidation() throws Exception {
        Configuration configuration = new Configuration(new String[] {
                "receive", "--protocol=udp", "--interface=lo"
        });
        configuration.validate();
    }

    @Test(expected = UserErrorException.class)
    public void rejectsMissingSendAddressDuringValidation() throws Exception {
        Configuration configuration = new Configuration(new String[] {
                "send", "--protocol=udp"
        });
        configuration.validate();
    }
}
