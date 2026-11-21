package org.opensmartgridplatform.core.infra.jms.protocol.outbound;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import javax.jms.ObjectMessage;
import javax.jms.Session;

import org.junit.jupiter.api.Test;
import org.opensmartgridplatform.core.infra.messaging.CoreLogItemRequestMessageSender;
import org.opensmartgridplatform.domain.core.entities.ProtocolInfo;
import org.opensmartgridplatform.shared.infra.jms.Constants;
import org.opensmartgridplatform.shared.infra.jms.ProtocolRequestMessage;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.core.MessageCreator;

public class ProtocolRequestMessageSender_ArrowTest {

    @Test
    public void isSupportedReturnsTrueWhenTemplateExists() {
        ProtocolRequestMessageJmsTemplateFactory factory = mock(ProtocolRequestMessageJmsTemplateFactory.class);
        ProtocolInfo protocolInfo = mock(ProtocolInfo.class);
        when(factory.getJmsTemplate(protocolInfo)).thenReturn(mock(JmsTemplate.class));

        ProtocolRequestMessageSender sender = new ProtocolRequestMessageSender();
        org.springframework.test.util.ReflectionTestUtils.setField(sender, "protocolRequestMessageJmsTemplateFactory",
                factory);

        assertTrue(sender.isSupported(protocolInfo));
    }

    @Test
    public void isSupportedReturnsFalseWhenTemplateIsMissing() {
        ProtocolRequestMessageJmsTemplateFactory factory = mock(ProtocolRequestMessageJmsTemplateFactory.class);
        ProtocolInfo protocolInfo = mock(ProtocolInfo.class);
        when(factory.getJmsTemplate(protocolInfo)).thenReturn(null);

        ProtocolRequestMessageSender sender = new ProtocolRequestMessageSender();
        org.springframework.test.util.ReflectionTestUtils.setField(sender, "protocolRequestMessageJmsTemplateFactory",
                factory);

        assertFalse(sender.isSupported(protocolInfo));
    }

    @Test
    public void sendCreatesMessageWithRequestPropertiesAndMessageGroup() throws Exception {
        ProtocolRequestMessageJmsTemplateFactory factory = mock(ProtocolRequestMessageJmsTemplateFactory.class);
        CoreLogItemRequestMessageSender logSender = mock(CoreLogItemRequestMessageSender.class);
        ProtocolInfo protocolInfo = mock(ProtocolInfo.class);
        JmsTemplate template = mock(JmsTemplate.class);
        ProtocolRequestMessage request = mock(ProtocolRequestMessage.class);
        Session session = mock(Session.class);
        ObjectMessage objectMessage = mock(ObjectMessage.class);

        when(factory.getJmsTemplate(protocolInfo)).thenReturn(template);
        when(template.getDefaultDestinationName()).thenReturn("requests");
        when(template.getTimeToLive()).thenReturn(100L);
        when(protocolInfo.getProtocol()).thenReturn("protocol");
        when(protocolInfo.getProtocolVersion()).thenReturn("1");
        when(protocolInfo.isParallelRequestsAllowed()).thenReturn(false);
        when(request.getDeviceIdentification()).thenReturn("device-1");
        when(request.getMessageType()).thenReturn("OTHER");
        when(request.getRequest()).thenReturn("payload");
        when(request.getCorrelationUid()).thenReturn("correlation");
        when(request.getMessagePriority()).thenReturn(4);
        when(request.getDomain()).thenReturn("domain");
        when(request.getDomainVersion()).thenReturn("1");
        when(request.getOrganisationIdentification()).thenReturn("organisation");
        when(request.getIpAddress()).thenReturn("127.0.0.1");
        when(request.isScheduled()).thenReturn(true);
        when(request.getRetryCount()).thenReturn(0);
        when(request.bypassRetry()).thenReturn(false);
        when(session.createObjectMessage("payload")).thenReturn(objectMessage);
        when(template.send(any(MessageCreator.class))).thenAnswer(invocation -> {
            MessageCreator creator = invocation.getArgument(0);
            creator.createMessage(session);
            return null;
        });

        ProtocolRequestMessageSender sender = new ProtocolRequestMessageSender();
        org.springframework.test.util.ReflectionTestUtils.setField(sender, "protocolRequestMessageJmsTemplateFactory",
                factory);
        org.springframework.test.util.ReflectionTestUtils.setField(sender, "coreLogItemRequestMessageSender",
                logSender);
        org.springframework.test.util.ReflectionTestUtils.setField(sender, "messageGroupCacheSize", 10);

        sender.send(request, protocolInfo);

        verify(objectMessage).setJMSCorrelationID("correlation");
        verify(objectMessage).setJMSType("OTHER");
        verify(objectMessage).setJMSPriority(4);
        verify(objectMessage).setStringProperty(Constants.DOMAIN, "domain");
        verify(objectMessage).setStringProperty(Constants.DOMAIN_VERSION, "1");
        verify(objectMessage).setStringProperty(Constants.ORGANISATION_IDENTIFICATION, "organisation");
        verify(objectMessage).setStringProperty(Constants.DEVICE_IDENTIFICATION, "device-1");
        verify(objectMessage).setStringProperty(Constants.IP_ADDRESS, "127.0.0.1");
        verify(objectMessage).setBooleanProperty(Constants.IS_SCHEDULED, true);
        verify(objectMessage).setIntProperty(Constants.RETRY_COUNT, 0);
        verify(objectMessage).setBooleanProperty(Constants.BYPASS_RETRY, false);
        verify(objectMessage).setStringProperty(Constants.MESSAGE_GROUP,
                String.valueOf(Math.abs("device-1".hashCode() % 10)));
        verify(logSender, never()).send(any());
    }

    @Test
    public void sendLogsRetryCount() throws Exception {
        ProtocolRequestMessageJmsTemplateFactory factory = mock(ProtocolRequestMessageJmsTemplateFactory.class);
        CoreLogItemRequestMessageSender logSender = mock(CoreLogItemRequestMessageSender.class);
        ProtocolInfo protocolInfo = mock(ProtocolInfo.class);
        JmsTemplate template = mock(JmsTemplate.class);
        ProtocolRequestMessage request = mock(ProtocolRequestMessage.class);
        Session session = mock(Session.class);
        ObjectMessage objectMessage = mock(ObjectMessage.class);

        when(factory.getJmsTemplate(protocolInfo)).thenReturn(template);
        when(template.getDefaultDestinationName()).thenReturn("requests");
        when(template.getTimeToLive()).thenReturn(100L);
        when(protocolInfo.getProtocol()).thenReturn("protocol");
        when(protocolInfo.getProtocolVersion()).thenReturn("1");
        when(protocolInfo.isParallelRequestsAllowed()).thenReturn(true);
        when(request.getDeviceIdentification()).thenReturn("device");
        when(request.getMessageType()).thenReturn("OTHER");
        when(request.getRequest()).thenReturn("payload");
        when(request.getCorrelationUid()).thenReturn("correlation");
        when(request.getMessagePriority()).thenReturn(4);
        when(request.getDomain()).thenReturn("domain");
        when(request.getDomainVersion()).thenReturn("1");
        when(request.getOrganisationIdentification()).thenReturn("organisation");
        when(request.getIpAddress()).thenReturn("127.0.0.1");
        when(request.isScheduled()).thenReturn(false);
        when(request.getRetryCount()).thenReturn(2);
        when(request.bypassRetry()).thenReturn(false);
        when(session.createObjectMessage("payload")).thenReturn(objectMessage);
        when(template.send(any(MessageCreator.class))).thenAnswer(invocation -> {
            MessageCreator creator = invocation.getArgument(0);
            creator.createMessage(session);
            return null;
        });

        ProtocolRequestMessageSender sender = new ProtocolRequestMessageSender();
        org.springframework.test.util.ReflectionTestUtils.setField(sender, "protocolRequestMessageJmsTemplateFactory",
                factory);
        org.springframework.test.util.ReflectionTestUtils.setField(sender, "coreLogItemRequestMessageSender",
                logSender);

        sender.send(request, protocolInfo);

        verify(objectMessage, never()).setStringProperty(Constants.MESSAGE_GROUP, "anything");
        verify(logSender).send(any());
    }

    @Test
    public void getMessageGroupIdReturnsAbsoluteRemainder() {
        ProtocolRequestMessageSender sender = new ProtocolRequestMessageSender();
        org.springframework.test.util.ReflectionTestUtils.setField(sender, "messageGroupCacheSize", 7);

        String result = sender.getMessageGroupId("device");

        assertEquals(String.valueOf(Math.abs("device".hashCode() % 7)), result);
        assertNotNull(result);
    }
}
