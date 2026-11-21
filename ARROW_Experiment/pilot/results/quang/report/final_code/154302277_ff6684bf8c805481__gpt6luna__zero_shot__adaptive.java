package org.opensmartgridplatform.core.infra.jms.protocol.outbound;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.test.util.ReflectionTestUtils;

public class ProtocolRequestMessageSender_ArrowTest {

    @Test
    public void isSupportedThrowsWhenNoTemplateFactoryIsAvailable() {
        ProtocolRequestMessageSender sender = new ProtocolRequestMessageSender();

        assertThrows(NullPointerException.class, () -> sender.isSupported(null));
    }

    @Test
    public void messageGroupIdIsDeterministic() {
        ProtocolRequestMessageSender sender = new ProtocolRequestMessageSender();
        ReflectionTestUtils.setField(sender, "messageGroupCacheSize", 10);

        String first = sender.getMessageGroupId("device-1");
        String second = sender.getMessageGroupId("device-1");

        assertEquals(first, second);
        assertEquals(String.valueOf(Math.abs("device-1".hashCode() % 10)), first);
    }
}
