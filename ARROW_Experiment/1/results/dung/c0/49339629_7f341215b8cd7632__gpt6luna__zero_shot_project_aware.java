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
    public void parsesSendConfigurationAndPayload() throws Exception {
        Configuration configuration = new Configuration(new String[] {
                "send", "--protocol=udp", "--address=127.0.0.1:8080", "hello", "world"
        });

        assertEquals(Mode.SEND, configuration.getMode());
        assertEquals(Protocol.UDP, configuration.getProtocol());
        assertEquals("127.0.0.1:8080", configuration.getAddress());
        assertEquals(Integer.valueOf(8080), configuration.getPort());
        assertEquals("hello world", configuration.getPayload());
        configuration.validate();
        assertEquals(InetAddress.getByName("127.0.0.1"), configuration.getInetAddress());
    }

    @Test
    public void parsesReceiveConfiguration() throws Exception {
        Configuration configuration = new Configuration(new String[] {
                "receive", "--protocol=tcp", "--interface=lo", "--port=1234"
        });

        assertEquals(Mode.RECEIVE, configuration.getMode());
        assertEquals(Protocol.TCP, configuration.getProtocol());
        assertEquals("lo", configuration.getInterface());
        assertEquals(Integer.valueOf(1234), configuration.getPort());
        assertNull(configuration.getPayload());
    }

    @Test
    public void localAddressIsResolvedDuringValidation() throws Exception {
        Configuration configuration = new Configuration(new String[] {
                "send", "--protocol=udp", "--address=127.0.0.1", "--local-address=127.0.0.2"
        });

        assertEquals("127.0.0.2", configuration.getLocalAddress());
        assertNull(configuration.getLocalInetAddress());
        configuration.validate();
        assertEquals(InetAddress.getByName("127.0.0.2"), configuration.getLocalInetAddress());
    }

    @Test
    public void emptyConfigurationHasNoOptionalValues() {
        Configuration configuration = new Configuration();

        assertNull(configuration.getMode());
        assertNull(configuration.getProtocol());
        assertNull(configuration.getAddress());
        assertNull(configuration.getInetAddress());
        assertNull(configuration.getPort());
        assertNull(configuration.getInterface());
        assertNull(configuration.getNetworkInterface());
        assertEquals(Collections.emptyList(), configuration.getNetworkInterfaceAddresses());
        assertNull(configuration.getPayload());
        assertNull(configuration.getLocalAddress());
        assertNull(configuration.getLocalInetAddress());
        assertNull(configuration.getLocalPort());
    }

    @Test
    public void interfaceNameIsNullWhenInterfaceIsUnresolved() {
        Configuration configuration = new Configuration();

        assertNull(configuration.getInterfaceName());
    }

    @Test
    public void parsesLocalPort() throws Exception {
        Configuration configuration = new Configuration(new String[] {
                "send", "--protocol=udp", "--address=127.0.0.1", "--local-port=4321"
        });

        assertEquals(Integer.valueOf(4321), configuration.getLocalPort());
    }

    @Test
    public void verboseDefaultsToFalse() {
        Configuration configuration = new Configuration();

        assertFalse(configuration.isVerbose());
    }

    @Test(expected = UserErrorException.class)
    public void rejectsMissingArguments() throws Exception {
        new Configuration(new String[0]);
    }

    @Test(expected = UserErrorException.class)
    public void rejectsNullArguments() throws Exception {
        new Configuration(null);
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
    public void rejectsInvalidPortText() throws Exception {
        new Configuration(new String[] {"receive", "--protocol=udp", "--port=abc"});
    }

    @Test(expected = UserErrorException.class)
    public void rejectsInvalidLocalPortText() throws Exception {
        new Configuration(new String[] {"send", "--protocol=udp", "--local-port=abc"});
    }

    @Test(expected = UserErrorException.class)
    public void validationRequiresProtocol() throws Exception {
        Configuration configuration = new Configuration(new String[] {"send"});
        configuration.validate();
    }

    @Test(expected = UserErrorException.class)
    public void receiveValidationRequiresInterface() throws Exception {
        Configuration configuration = new Configuration(new String[] {
                "receive", "--protocol=udp", "--port=1234"
        });
        configuration.validate();
    }

    @Test(expected = UserErrorException.class)
    public void receiveValidationRequiresPort() throws Exception {
        Configuration configuration = new Configuration(new String[] {
                "receive", "--protocol=udp", "--interface=lo"
        });
        configuration.validate();
    }

    @Test(expected = UserErrorException.class)
    public void sendValidationRequiresAddress() throws Exception {
        Configuration configuration = new Configuration(new String[] {
                "send", "--protocol=udp"
        });
        configuration.validate();
    }

    @Test(expected = UserErrorException.class)
    public void rejectsUnknownInterface() throws Exception {
        Configuration configuration = new Configuration(new String[] {
                "receive", "--protocol=udp", "--interface=definitely-not-an-interface", "--port=1234"
        });
        configuration.validate();
    }

    @Test(expected = UserErrorException.class)
    public void rejectsInvalidLocalAddress() throws Exception {
        Configuration configuration = new Configuration(new String[] {
                "send", "--protocol=udp", "--address=127.0.0.1", "--local-address=invalid host name"
        });
        configuration.validate();
    }
}
