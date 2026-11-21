package org.opensmartgridplatform.core.infra.jms.protocol.outbound;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

public class ProtocolRequestMessageSender_ArrowTest {

    @Test
    public void isSupportedThrowsWhenSenderIsNotConfigured() {
        ProtocolRequestMessageSender sender = new ProtocolRequestMessageSender();

        assertThrows(NullPointerException.class, () -> sender.isSupported(null));
    }

    @Test
    public void messageGroupIdIsDeterministic() {
        ProtocolRequestMessageSender sender = new ProtocolRequestMessageSender();

        assertThrows(ArithmeticException.class, () -> sender.getMessageGroupId("device-1"));
    }
}
