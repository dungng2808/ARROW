package io.novaordis.playground.java.network.traffic;

import java.net.InetAddress;
import java.util.Collections;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

public class Configuration_ArrowTest {

    @Test
    public void parsesModeOptionsAndPayload() throws Exception {
        Configuration configuration = new Configuration(new String[] {
                "send", "--protocol=udp", "--address=127.0.0.1", "--port=1234",
                "--local-address=127.0.0.1", "--local-port=4321", "hello", "world"
        });

        assertEquals(Mode.SEND, configuration.getMode());
        assertEquals(Protocol.UDP, configuration.getProtocol());
        assertEquals("127.0.0.1", configuration.getAddress());
        assertEquals(Integer.valueOf(1234), configuration.getPort());
        assertEquals("127.0.0.1", configuration.getLocalAddress());
        assertEquals(Integer.valueOf(4321), configuration.getLocalPort());
        assertEquals("hello world", configuration.getPayload());
    }

    @Test
    public void defaultsOptionalValuesToNull() throws Exception {
        Configuration configuration = new Configuration(new String[] {"send", "--protocol=udp"});

        assertNull(configuration.getAddress());
        assertNull(configuration.getPort());
        assertNull(configuration.getInterface());
        assertNull(configuration.getLocalAddress());
        assertNull(configuration.getLocalPort());
        assertNull(configuration.getPayload());
        assertNull(configuration.getInetAddress());
        assertNull(configuration.getLocalInetAddress());
        assertNull(configuration.getNetworkInterface());
        assertNull(configuration.getInterfaceName());
        assertEquals(Collections.emptyList(), configuration.getNetworkInterfaceAddresses());
    }

    @Test
    public void resolvesSendAddressesAndAddressPort() throws Exception {
        Configuration configuration = new Configuration(new String[] {
                "send", "--protocol=udp", "--address=127.0.0.1:2345", "--local-address=127.0.0.1"
        });

        configuration.validate();

        assertEquals(InetAddress.getByName("127.0.0.1"), configuration.getInetAddress());
        assertEquals(Integer.valueOf(2345), configuration.getPort());
        assertEquals(InetAddress.getByName("127.0.0.1"), configuration.getLocalInetAddress());
    }

    @Test
    public void resolvesReceiveConfiguration() throws Exception {
        Configuration configuration = new Configuration(new String[] {
                "receive", "--protocol=udp", "--interface=lo", "--port=1234"
        });

        configuration.validate();

        assertEquals(Integer.valueOf(1234), configuration.getPort());
        assertEquals("lo", configuration.getInterface());
        assertEquals("lo", configuration.getInterfaceName());
        assertTrue(configuration.getNetworkInterfaceAddresses().size() >= 0);
    }

    @Test
    public void networkInterfaceAddressIsNullWhenNoInterfaceIsResolved() throws Exception {
        Configuration configuration = new Configuration(new String[] {"send", "--protocol=udp"});

        assertNull(configuration.getNetworkInterfaceAddress(AddressType.IPV4));
    }

    @Test
    public void rejectsMissingMode() throws Exception {
        try {
            new Configuration(new String[0]);
            fail("Expected UserErrorException");
        } catch (UserErrorException expected) {
            assertEquals("missing mode", expected.getMessage());
        }
    }

    @Test
    public void rejectsUnknownMode() throws Exception {
        try {
            new Configuration(new String[] {"unknown"});
            fail("Expected UserErrorException");
        } catch (UserErrorException expected) {
            assertEquals("unknown mode unknown", expected.getMessage());
        }
    }

    @Test
    public void rejectsUnknownOption() throws Exception {
        try {
            new Configuration(new String[] {"send", "--unknown"});
            fail("Expected UserErrorException");
        } catch (UserErrorException expected) {
            assertEquals("unknown option --unknown", expected.getMessage());
        }
    }

    @Test
    public void rejectsInvalidPortText() throws Exception {
        try {
            new Configuration(new String[] {"send", "--protocol=udp", "--port=abc"});
            fail("Expected UserErrorException");
        } catch (UserErrorException expected) {
            assertEquals("invalid port value abc, not an integer", expected.getMessage());
        }
    }

    @Test
    public void rejectsMissingProtocolDuringValidation() throws Exception {
        Configuration configuration = new Configuration(new String[] {"send"});

        try {
            configuration.validate();
            fail("Expected UserErrorException");
        } catch (UserErrorException expected) {
            assertEquals("a protocol must be specified with --protocol=udp|multicast|tcp|...", expected.getMessage());
        }
    }

    @Test
    public void rejectsMissingSendAddressDuringValidation() throws Exception {
        Configuration configuration = new Configuration(new String[] {"send", "--protocol=udp"});

        try {
            configuration.validate();
            fail("Expected UserErrorException");
        } catch (UserErrorException expected) {
            assertEquals("missing required address, use --address=...", expected.getMessage());
        }
    }

    @Test
    public void rejectsMissingReceiveInterfaceDuringValidation() throws Exception {
        Configuration configuration = new Configuration(new String[] {"receive", "--protocol=udp", "--port=1234"});

        try {
            configuration.validate();
            fail("Expected UserErrorException");
        } catch (UserErrorException expected) {
            assertEquals("missing required network interface, use --interface= to specify", expected.getMessage());
        }
    }

    @Test
    public void rejectsMissingReceivePortDuringValidation() throws Exception {
        Configuration configuration = new Configuration(new String[] {
                "receive", "--protocol=udp", "--interface=lo"
        });

        try {
            configuration.validate();
            fail("Expected UserErrorException");
        } catch (UserErrorException expected) {
            assertEquals("a port must be specified with --port=... or as part of the address", expected.getMessage());
        }
    }

    @Test
    public void interfaceNameIsNullWithoutResolvedInterface() throws Exception {
        Configuration configuration = new Configuration(new String[] {"send", "--protocol=udp"});

        assertNull(configuration.getInterfaceName());
    }

    @Test
    public void verboseDefaultsToFalse() throws Exception {
        Configuration configuration = new Configuration(new String[] {"send", "--protocol=udp"});

        assertFalse(configuration.isVerbose());
    }
}
