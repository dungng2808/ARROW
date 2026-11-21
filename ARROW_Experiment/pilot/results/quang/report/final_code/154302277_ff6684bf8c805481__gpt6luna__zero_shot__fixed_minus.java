package org.opensmartgridplatform.core.infra.jms.protocol.outbound;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

public class ProtocolRequestMessageSender_ArrowTest {

    @Test
    public void messageGroupIdIsDeterministic() {
        ProtocolRequestMessageSender sender = new ProtocolRequestMessageSender();

        String first = sender.getMessageGroupId("device-1");
        String second = sender.getMessageGroupId("device-1");

        assertEquals("0", first);
        assertEquals(first, second);
    }
}
