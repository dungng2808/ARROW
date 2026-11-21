package org.opensmartgridplatform.core.infra.jms.protocol.outbound;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.Test;
import org.opensmartgridplatform.domain.core.entities.ProtocolInfo;

public class ProtocolRequestMessageSender_ArrowTest {

    @Test
    public void isSupportedReturnsFalseWhenNoTemplateIsAvailable() {
        ProtocolRequestMessageSender sender = new ProtocolRequestMessageSender();
        ProtocolRequestMessageJmsTemplateFactory factory = mock(ProtocolRequestMessageJmsTemplateFactory.class);
        ProtocolInfo protocolInfo = mock(ProtocolInfo.class);
        setField(sender, "protocolRequestMessageJmsTemplateFactory", factory);
        when(factory.getJmsTemplate(protocolInfo)).thenReturn(null);

        assertFalse(sender.isSupported(protocolInfo));
    }

    @Test
    public void isSupportedReturnsTrueWhenTemplateIsAvailable() {
        ProtocolRequestMessageSender sender = new ProtocolRequestMessageSender();
        ProtocolRequestMessageJmsTemplateFactory factory = mock(ProtocolRequestMessageJmsTemplateFactory.class);
        ProtocolInfo protocolInfo = mock(ProtocolInfo.class);
        setField(sender, "protocolRequestMessageJmsTemplateFactory", factory);
        when(factory.getJmsTemplate(protocolInfo)).thenReturn(mock(org.springframework.jms.core.JmsTemplate.class));

        assertTrue(sender.isSupported(protocolInfo));
    }

    @Test
    public void messageGroupIdUsesAbsoluteRemainderOfDeviceHash() {
        ProtocolRequestMessageSender sender = new ProtocolRequestMessageSender();
        setField(sender, "messageGroupCacheSize", 7);

        assertEquals(String.valueOf(Math.abs("device-1".hashCode() % 7)),
                sender.getMessageGroupId("device-1"));
    }

    private static void setField(Object target, String name, Object value) {
        try {
            java.lang.reflect.Field field = target.getClass().getDeclaredField(name);
            field.setAccessible(true);
            field.set(target, value);
        } catch (Exception e) {
            throw new AssertionError(e);
        }
    }
}
