package org.opensmartgridplatform.core.infra.jms.protocol.outbound;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import org.opensmartgridplatform.domain.core.entities.ProtocolInfo;
import org.springframework.jms.core.JmsTemplate;

public class ProtocolRequestMessageSender_ArrowTest {

    @Test
    public void isSupportedReturnsFalseWhenNoTemplateIsAvailable() {
        ProtocolRequestMessageSender sender = new ProtocolRequestMessageSender();
        ProtocolRequestMessageJmsTemplateFactory factory = mock(ProtocolRequestMessageJmsTemplateFactory.class);
        ProtocolInfo protocolInfo = mock(ProtocolInfo.class);
        setField(sender, "protocolRequestMessageJmsTemplateFactory", factory);
        when(factory.getJmsTemplate(protocolInfo)).thenReturn(null);

        assertThat(sender.isSupported(protocolInfo)).isFalse();
    }

    @Test
    public void isSupportedReturnsTrueWhenTemplateIsAvailable() {
        ProtocolRequestMessageSender sender = new ProtocolRequestMessageSender();
        ProtocolRequestMessageJmsTemplateFactory factory = mock(ProtocolRequestMessageJmsTemplateFactory.class);
        ProtocolInfo protocolInfo = mock(ProtocolInfo.class);
        setField(sender, "protocolRequestMessageJmsTemplateFactory", factory);
        when(factory.getJmsTemplate(protocolInfo)).thenReturn(mock(JmsTemplate.class));

        assertThat(sender.isSupported(protocolInfo)).isTrue();
    }

    @Test
    public void messageGroupIdUsesAbsoluteRemainderOfDeviceHash() {
        ProtocolRequestMessageSender sender = new ProtocolRequestMessageSender();
        setField(sender, "messageGroupCacheSize", 7);

        assertThat(sender.getMessageGroupId("device-1"))
                .isEqualTo(String.valueOf(Math.abs("device-1".hashCode() % 7)));
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
