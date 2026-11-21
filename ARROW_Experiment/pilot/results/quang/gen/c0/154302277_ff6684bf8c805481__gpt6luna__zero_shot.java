package org.opensmartgridplatform.core.infra.jms.protocol.outbound;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;
import org.opensmartgridplatform.domain.core.entities.ProtocolInfo;

public class ProtocolRequestMessageSender_ArrowTest {

    @Test
    public void isSupportedReturnsFalseWhenNoTemplateIsAvailable() {
        ProtocolRequestMessageSender sender = new ProtocolRequestMessageSender();

        assertFalse(sender.isSupported(null));
    }

    @Test
    public void messageGroupIdIsDeterministic() {
        ProtocolRequestMessageSender sender = new ProtocolRequestMessageSender();

        String first = sender.getMessageGroupId("device-1");
        String second = sender.getMessageGroupId("device-1");

        assertTrue(first.equals(second));
    }
}
