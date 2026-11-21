package io.novaordis.playground.java.network.traffic;

import org.junit.Test;

import static org.junit.Assert.*;

public class Configuration_ArrowTest {

    @Test
    public void testConstructorWithValidArgs() throws Exception {
        String[] args = {"send", "--protocol=udp", "--interface=eth0", "--address=192.168.1.1", "--port=8080"};
        Configuration config = new Configuration(args);
        
        assertEquals(Mode.SEND, config.getMode());
        assertEquals(Protocol.UDP, config.getProtocol());
        assertEquals("eth0", config.getInterface());
        assertEquals("192.168.1.1", config.getAddress());
        assertEquals(Integer.valueOf(8080), config.getPort());
    }

    @Test(expected = UserErrorException.class)
    public void testConstructorMissingMode() throws Exception {
        String[] args = {};
        new Configuration(args);
    }

    @Test(expected = UserErrorException.class)
    public void testConstructorUnknownMode() throws Exception {
        String[] args = {"unknown"};
        new Configuration(args);
    }

    @Test(expected = UserErrorException.class)
    public void testConstructorInvalidPort() throws Exception {
        String[] args = {"send", "--port=abc"};
        new Configuration(args);
    }

    @Test(expected = UserErrorException.class)
    public void testConstructorUnknownOption() throws Exception {
        String[] args = {"send", "--unknown=option"};
        new Configuration(args);
    }

    @Test
    public void testConstructorWithPayload() throws Exception {
        String[] args = {"send", "hello world"};
        Configuration config = new Configuration(args);
        assertEquals("hello world", config.getPayload());
    }

    @Test
    public void testConstructorWithMultiplePayloadWords() throws Exception {
        String[] args = {"send", "hello", "world", "test"};
        Configuration config = new Configuration(args);
        assertEquals("hello world test", config.getPayload());
    }

    @Test
    public void testGetMode() throws Exception {
        String[] args = {"receive"};
        Configuration config = new Configuration(args);
        assertEquals(Mode.RECEIVE, config.getMode());
    }

    @Test
    public void testGetProtocol() throws Exception {
        String[] args = {"send", "--protocol=tcp"};
        Configuration config = new Configuration(args);
        assertEquals(Protocol.TCP, config.getProtocol());
    }

    @Test
    public void testGetLocalAddress() throws Exception {
        String[] args = {"send", "--local-address=127.0.0.1"};
        Configuration config = new Configuration(args);
        assertEquals("127.0.0.1", config.getLocalAddress());
    }

    @Test
    public void testGetLocalInetAddress() throws Exception {
        String[] args = {"send", "--local-address=127.0.0.1"};
        Configuration config = new Configuration(args);
        assertNotNull(config.getLocalInetAddress());
    }

    @Test
    public void testGetLocalPort() throws Exception {
        String[] args = {"send", "--local-port=9090"};
        Configuration config = new Configuration(args);
        assertEquals(Integer.valueOf(9090), config.getLocalPort());
    }

    @Test
    public void testGetAddress() throws Exception {
        String[] args = {"send", "--address=192.168.1.1"};
        Configuration config = new Configuration(args);
        assertEquals("192.168.1.1", config.getAddress());
    }

    @Test
    public void testGetInetAddress() throws Exception {
        String[] args = {"send", "--address=192.168.1.1"};
        Configuration config = new Configuration(args);
        assertNotNull(config.getInetAddress());
    }

    @Test
    public void testGetPort() throws Exception {
        String[] args = {"send", "--port=8080"};
        Configuration config = new Configuration(args);
        assertEquals(Integer.valueOf(8080), config.getPort());
    }

    @Test
    public void testGetInterface() throws Exception {
        String[] args = {"send", "--interface=eth0"};
        Configuration config = new Configuration(args);
        assertEquals("eth0", config.getInterface());
    }

    @Test
    public void testGetNetworkInterface() throws Exception {
        String[] args = {"send", "--interface=eth0"};
        Configuration config = new Configuration(args);
        assertNull(config.getNetworkInterface());
    }

    @Test
    public void testGetInterfaceName() throws Exception {
        String[] args = {"send", "--interface=eth0"};
        Configuration config = new Configuration(args);
        assertEquals(null, config.getInterfaceName());
    }

    @Test
    public void testGetNetworkInterfaceAddresses() throws Exception {
        String[] args = {"send", "--interface=eth0"};
        Configuration config = new Configuration(args);
        assertNotNull(config.getNetworkInterfaceAddresses());
    }

    @Test
    public void testGetNetworkInterfaceAddress() throws Exception {
        String[] args = {"send", "--interface=eth0"};
        Configuration config = new Configuration(args);
        assertNull(config.getNetworkInterfaceAddress(AddressType.IPV4));
    }

    @Test
    public void testGetPayload() throws Exception {
        String[] args = {"send", "payload text"};
        Configuration config = new Configuration(args);
        assertEquals("payload text", config.getPayload());
    }

    @Test
    public void testToString() throws Exception {
        String[] args = {"send", "--protocol=udp"};
        Configuration config = new Configuration(args);
        assertNotNull(config.toString());
    }

    @Test
    public void testSetInterface() throws Exception {
        Configuration config = new Configuration();
        config.setInterface("eth0");
        assertEquals("eth0", config.getInterface());
    }

    @Test
    public void testSetPort() throws Exception {
        Configuration config = new Configuration();
        config.setPort(8080);
        assertEquals(Integer.valueOf(8080), config.getPort());
    }

    @Test
    public void testSetAddress() throws Exception {
        Configuration config = new Configuration();
        config.setAddress("192.168.1.1");
        assertEquals("192.168.1.1", config.getAddress());
    }

    @Test
    public void testSetLocalPort() throws Exception {
        Configuration config = new Configuration();
        config.setLocalPort(9090);
        assertEquals(Integer.valueOf(9090), config.getLocalPort());
    }

    @Test
    public void testSetLocalAddress() throws Exception {
        Configuration config = new Configuration();
        config.setLocalAddress("127.0.0.1");
        assertEquals("127.0.0.1", config.getLocalAddress());
    }

    @Test
    public void testValidateProtocol() throws Exception {
        String[] args = {"send", "--protocol=udp"};
        Configuration config = new Configuration(args);
        config.validateProtocol();
    }

    @Test(expected = UserErrorException.class)
    public void testValidateProtocolMissing() throws Exception {
        String[] args = {"send"};
        Configuration config = new Configuration(args);
        config.validateProtocol();
    }

    @Test
    public void testValidatePort() throws Exception {
        String[] args = {"receive", "--port=8080"};
        Configuration config = new Configuration(args);
        config.validatePort();
    }

    @Test(expected = UserErrorException.class)
    public void testValidatePortMissing() throws Exception {
        String[] args = {"receive"};
        Configuration config = new Configuration(args);
        config.validatePort();
    }

    @Test
    public void testInitializeAndValidateNetworkInterface() throws Exception {
        String[] args = {"send", "--interface=eth0"};
        Configuration config = new Configuration(args);
        try {
            config.initializeAndValidateNetworkInterface(true);
        } catch (UserErrorException ignored) {
        }
    }

    @Test
    public void testInitializeAndValidateAddress() throws Exception {
        String[] args = {"send", "--address=192.168.1.1"};
        Configuration config = new Configuration(args);
        try {
            config.initializeAndValidateAddress(true);
        } catch (UserErrorException ignored) {
        }
    }

    @Test
    public void testInitializeAndValidateLocalAddress() throws Exception {
        String[] args = {"send", "--local-address=127.0.0.1"};
        Configuration config = new Configuration(args);
        try {
            config.initializeAndValidateLocalAddress();
        } catch (UserErrorException ignored) {
        }
    }
}
