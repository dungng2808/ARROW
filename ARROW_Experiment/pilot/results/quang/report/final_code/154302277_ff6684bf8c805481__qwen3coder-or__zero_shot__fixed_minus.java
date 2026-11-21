package org.opensmartgridplatform.core.infra.jms.protocol.outbound;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.opensmartgridplatform.shared.infra.jms.MessageType.GET_POWER_USAGE_HISTORY;

import java.util.HashMap;
import java.util.Map;

import javax.jms.JMSException;
import javax.jms.ObjectMessage;
import javax.jms.Session;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.opensmartgridplatform.core.domain.model.protocol.ProtocolInfo;
import org.opensmartgridplatform.core.infra.messaging.CoreLogItemRequestMessage;
import org.opensmartgridplatform.core.infra.messaging.CoreLogItemRequestMessageSender;
import org.opensmartgridplatform.domain.core.entities.ProtocolInfo;
import org.opensmartgridplatform.shared.infra.jms.Constants;
import org.opensmartgridplatform.shared.infra.jms.ProtocolRequestMessage;
import org.springframework.jms.core.JmsTemplate;

public class ProtocolRequestMessageSender_ArrowTest {

    @InjectMocks
    private ProtocolRequestMessageSender protocolRequestMessageSender;

    @Mock
    private CoreLogItemRequestMessageSender coreLogItemRequestMessageSender;

    @Mock
    private ProtocolRequestMessageJmsTemplateFactory protocolRequestMessageJmsTemplateFactory;

    @Mock
    private JmsTemplate jmsTemplate;

    @Mock
    private Session session;

    @Mock
    private ObjectMessage objectMessage;

    @Captor
    private ArgumentCaptor<javax.jms.MessageCreator> messageCreatorCaptor;

    private org.opensmartgridplatform.domain.core.entities.ProtocolInfo protocolInfo;
    private ProtocolRequestMessage protocolRequestMessage;
    private Long timeToLive;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        this.timeToLive = 1000L;
        
        this.protocolInfo = new org.opensmartgridplatform.domain.core.entities.ProtocolInfo("Protocol", "1.0");
        this.protocolRequestMessage = new ProtocolRequestMessage("device123", "org123", "domain", "version", "type",
                "correlationId", 1, false, 0, false, "ipAddress", 10);
    }

    @Test
    public void testIsSupportedReturnsTrueWhenJmsTemplateExists() {
        when(this.protocolRequestMessageJmsTemplateFactory.getJmsTemplate(any(org.opensmartgridplatform.domain.core.entities.ProtocolInfo.class)))
                .thenReturn(this.jmsTemplate);

        final boolean result = this.protocolRequestMessageSender.isSupported(this.protocolInfo);

        org.junit.Assert.assertTrue(result);
    }

    @Test
    public void testIsSupportedReturnsFalseWhenJmsTemplateIsNull() {
        when(this.protocolRequestMessageJmsTemplateFactory.getJmsTemplate(any(org.opensmartgridplatform.domain.core.entities.ProtocolInfo.class)))
                .thenReturn(null);

        final boolean result = this.protocolRequestMessageSender.isSupported(this.protocolInfo);

        org.junit.Assert.assertFalse(result);
    }

    @Test
    public void testSendSendsMessageWithCorrectProperties() throws JMSException {
        when(this.protocolRequestMessageJmsTemplateFactory.getJmsTemplate(any(org.opensmartgridplatform.domain.core.entities.ProtocolInfo.class)))
                .thenReturn(this.jmsTemplate);
        when(this.jmsTemplate.getDefaultDestinationName()).thenReturn("destinationQueue");
        when(this.jmsTemplate.getTimeToLive()).thenReturn(2000L);
        when(this.session.createObjectMessage(any(Object.class))).thenReturn(this.objectMessage);
        doNothing().when(this.jmsTemplate).send(any(javax.jms.MessageCreator.class));

        this.protocolRequestMessageSender.send(this.protocolRequestMessage, this.protocolInfo);

        verify(this.jmsTemplate).send(messageCreatorCaptor.capture());
        messageCreatorCaptor.getValue().createMessage(this.session);

        verify(this.objectMessage).setJMSCorrelationID("correlationId");
        verify(this.objectMessage).setJMSType("type");
        verify(this.objectMessage).setJMSPriority(10);
        verify(this.objectMessage).setStringProperty(Constants.DOMAIN, "domain");
        verify(this.objectMessage).setStringProperty(Constants.DOMAIN_VERSION, "version");
        verify(this.objectMessage).setStringProperty(Constants.ORGANISATION_IDENTIFICATION, "org123");
        verify(this.objectMessage).setStringProperty(Constants.DEVICE_IDENTIFICATION, "device123");
        verify(this.objectMessage).setStringProperty(Constants.IP_ADDRESS, "ipAddress");
        verify(this.objectMessage).setBooleanProperty(Constants.IS_SCHEDULED, false);
        verify(this.objectMessage).setIntProperty(Constants.RETRY_COUNT, 0);
        verify(this.objectMessage).setBooleanProperty(Constants.BYPASS_RETRY, false);
    }

    @Test
    public void testSendSetsCustomTimeToLiveForGetPowerUsageHistory() throws JMSException {
        this.protocolRequestMessage = new ProtocolRequestMessage("device123", "org123", "domain", "version",
                GET_POWER_USAGE_HISTORY.toString(), "correlationId", 1, false, 0, false, "ipAddress", 10);

        when(this.protocolRequestMessageJmsTemplateFactory.getJmsTemplate(any(org.opensmartgridplatform.domain.core.entities.ProtocolInfo.class)))
                .thenReturn(this.jmsTemplate);
        when(this.jmsTemplate.getDefaultDestinationName()).thenReturn("destinationQueue");
        when(this.jmsTemplate.getTimeToLive()).thenReturn(2000L);
        when(this.session.createObjectMessage(any(Object.class))).thenReturn(this.objectMessage);
        doNothing().when(this.jmsTemplate).send(any(javax.jms.MessageCreator.class));

        this.protocolRequestMessageSender.send(this.protocolRequestMessage, this.protocolInfo);

        verify(this.jmsTemplate).setTimeToLive(timeToLive);
    }

    @Test
    public void testSendDoesNotSetCustomTimeToLiveForOtherMessageTypes() throws JMSException {
        this.protocolRequestMessage = new ProtocolRequestMessage("device123", "org123", "domain", "version",
                "OTHER_TYPE", "correlationId", 1, false, 0, false, "ipAddress", 10);

        when(this.protocolRequestMessageJmsTemplateFactory.getJmsTemplate(any(org.opensmartgridplatform.domain.core.entities.ProtocolInfo.class)))
                .thenReturn(this.jmsTemplate);
        when(this.jmsTemplate.getDefaultDestinationName()).thenReturn("destinationQueue");
        when(this.jmsTemplate.getTimeToLive()).thenReturn(2000L);
        when(this.session.createObjectMessage(any(Object.class))).thenReturn(this.objectMessage);
        doNothing().when(this.jmsTemplate).send(any(javax.jms.MessageCreator.class));

        this.protocolRequestMessageSender.send(this.protocolRequestMessage, this.protocolInfo);

        verify(this.jmsTemplate).setTimeToLive(2000L);
    }

    @Test
    public void testSendMessageWithParallelRequestsAllowed() throws JMSException {
        this.protocolInfo = new org.opensmartgridplatform.domain.core.entities.ProtocolInfo("Protocol", "1.0");
        when(this.protocolRequestMessageJmsTemplateFactory.getJmsTemplate(any(org.opensmartgridplatform.domain.core.entities.ProtocolInfo.class)))
                .thenReturn(this.jmsTemplate);
        when(this.jmsTemplate.getDefaultDestinationName()).thenReturn("destinationQueue");
        when(this.jmsTemplate.getTimeToLive()).thenReturn(2000L);
        when(this.session.createObjectMessage(any(Object.class))).thenReturn(this.objectMessage);
        doNothing().when(this.jmsTemplate).send(any(javax.jms.MessageCreator.class));

        this.protocolRequestMessageSender.send(this.protocolRequestMessage, this.protocolInfo);

        verify(this.objectMessage).setStringProperty(Constants.MESSAGE_GROUP, "78");
    }

    @Test
    public void testSendMessageWithRetryCountZero() throws JMSException {
        this.protocolRequestMessage = new ProtocolRequestMessage("device123", "org123", "domain", "version", "type",
                "correlationId", 1, false, 0, false, "ipAddress", 0);

        when(this.protocolRequestMessageJmsTemplateFactory.getJmsTemplate(any(org.opensmartgridplatform.domain.core.entities.ProtocolInfo.class)))
                .thenReturn(this.jmsTemplate);
        when(this.jmsTemplate.getDefaultDestinationName()).thenReturn("destinationQueue");
        when(this.jmsTemplate.getTimeToLive()).thenReturn(2000L);
        when(this.session.createObjectMessage(any(Object.class))).thenReturn(this.objectMessage);
        doNothing().when(this.jmsTemplate).send(any(javax.jms.MessageCreator.class));

        this.protocolRequestMessageSender.send(this.protocolRequestMessage, this.protocolInfo);

        verify(this.coreLogItemRequestMessageSender).send(any(CoreLogItemRequestMessage.class));
    }

    @Test
    public void testGetMessageGroupId() {
        final String groupId = this.protocolRequestMessageSender.getMessageGroupId("device123");
        org.junit.Assert.assertEquals("78", groupId);
    }
}
