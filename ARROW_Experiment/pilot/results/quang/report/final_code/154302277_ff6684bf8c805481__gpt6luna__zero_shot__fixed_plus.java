package org.opensmartgridplatform.core.infra.jms.protocol.outbound;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

import org.junit.jupiter.api.Test;
import org.opensmartgridplatform.domain.core.entities.ProtocolInfo;
import org.springframework.jms.core.JmsTemplate;

public class ProtocolRequestMessageSender_ArrowTest {

    @Test
    public void isSupportedReturnsFalseWhenNoTemplateIsAvailable() {
        ProtocolRequestMessageJmsTemplateFactory factory =
                org.mockito.Mockito.mock(ProtocolRequestMessageJmsTemplateFactory.class);
        ProtocolInfo protocolInfo = org.mockito.Mockito.mock(ProtocolInfo.class);
        org.mockito.Mockito.when(factory.getJmsTemplate(protocolInfo)).thenReturn(null);

        ProtocolRequestMessageSender sender = new ProtocolRequestMessageSender();
        org.springframework.test.util.ReflectionTestUtils.setField(
                sender, "protocolRequestMessageJmsTemplateFactory", factory);

        assertFalse(sender.isSupported(protocolInfo));
    }

    @Test
    public void messageGroupIdIsDeterministic() {
        ProtocolRequestMessageSender sender = new ProtocolRequestMessageSender();
        org.springframework.test.util.ReflectionTestUtils.setField(sender, "messageGroupCacheSize", 100);

        String first = sender.getMessageGroupId("device-1");
        String second = sender.getMessageGroupId("device-1");

        assertEquals(first, second);
        assertEquals(String.valueOf(Math.abs("device-1".hashCode() % 100)), first);
    }
}
