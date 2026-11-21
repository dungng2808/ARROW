package org.opensmartgridplatform.core.infra.jms.protocol.outbound;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
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

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.opensmartgridplatform.core.domain.model.protocol.ProtocolInfo;
import org.opensmartgridplatform.core.infra.messaging.CoreLogItemRequestMessage;
import org.opensmartgridplatform.core.infra.messaging.CoreLogItemRequestMessageSender;
import org.opensmartgridplatform.domain.core.entities.ProtocolInfo;
import org.opensmartgridplatform.shared.infra.jms.Constants;
import org.opensmartgridplatform.shared.infra.jms.ProtocolRequestMessage;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.core.MessageCreator;

@ExtendWith(MockitoExtension.class)
public class ProtocolRequestMessageSender_ArrowTest {

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

    @InjectMocks
    private ProtocolRequestMessageSender protocolRequestMessageSender;

    @Captor
    private ArgumentCaptor<MessageCreator> messageCreatorCaptor;

    private org.opensmartgridplatform.domain.core.entities.ProtocolInfo protocolInfo;
    private ProtocolRequestMessage protocolRequestMessage;
    private String deviceIdentification;
    private String organisationIdentification;
    private String correlationUid;
    private String domain;
    private String domainVersion;
    private String ipAddress;
    private int retryCount;
    private boolean bypassRetry;
    private boolean isScheduled;
    private int messagePriority;
    private String messageType;
    private Long timeToLive;

    @BeforeEach
    public void setUp() {
        this.deviceIdentification = "device123";
        this.organisationIdentification = "organisation123";
        this.correlationUid = "correlation123";
        this.domain = "domain123";
        this.domainVersion = "version123";
        this.ipAddress = "192.168.0.1";
        this.retryCount = 0;
        this.bypassRetry = false;
        this.isScheduled = false;
        this.messagePriority = 5;
        this.messageType = "TEST_MESSAGE_TYPE";
        this.timeToLive = 10000L;

        this.protocolInfo = mock(org.opensmartgridplatform.domain.core.entities.ProtocolInfo.class);
        when(this.protocolInfo.getProtocol()).thenReturn("protocol");
        when(this.protocolInfo.getProtocolVersion()).thenReturn("version");
        when(this.protocolInfo.isParallelRequestsAllowed()).thenReturn(true);

        this.protocolRequestMessage = mock(ProtocolRequestMessage.class);
        when(this.protocolRequestMessage.getDeviceIdentification()).thenReturn(this.deviceIdentification);
        when(this.protocolRequestMessage.getOrganisationIdentification()).thenReturn(this.organisationIdentification);
        when(this.protocolRequestMessage.getCorrelationUid()).thenReturn(this.correlationUid);
        when(this.protocolRequestMessage.getDomain()).thenReturn(this.domain);
        when(this.protocolRequestMessage.getDomainVersion()).thenReturn(this.domainVersion);
        when(this.protocolRequestMessage.getIpAddress()).thenReturn(this.ipAddress);
        when(this.protocolRequestMessage.getRetryCount()).thenReturn(this.retryCount);
        when(this.protocolRequestMessage.bypassRetry()).thenReturn(this.bypassRetry);
        when(this.protocolRequestMessage.isScheduled()).thenReturn(this.isScheduled);
        when(this.protocolRequestMessage.getMessagePriority()).thenReturn(this.messagePriority);
        when(this.protocolRequestMessage.getMessageType()).thenReturn(this.messageType);
        when(this.protocolRequestMessage.getRequest()).thenReturn(new HashMap<>());

        when(this.protocolRequestMessageJmsTemplateFactory.getJmsTemplate(any(org.opensmartgridplatform.domain.core.entities.ProtocolInfo.class)))
                .thenReturn(this.jmsTemplate);
        when(this.jmsTemplate.getDefaultDestinationName()).thenReturn("destinationQueue");
        when(this.jmsTemplate.getTimeToLive()).thenReturn(this.timeToLive);
    }

    @Test
    public void testIsSupported_ReturnsTrue_WhenJmsTemplateExists() {
        // Arrange
        when(this.protocolRequestMessageJmsTemplateFactory.getJmsTemplate(any(org.opensmartgridplatform.domain.core.entities.ProtocolInfo.class)))
                .thenReturn(this.jmsTemplate);

        // Act
        final boolean result = protocolRequestMessageSender.isSupported(this.protocolInfo);

        // Assert
        assertTrue(result);
    }

    @Test
    public void testIsSupported_ReturnsFalse_WhenJmsTemplateIsNull() {
        // Arrange
        when(this.protocolRequestMessageJmsTemplateFactory.getJmsTemplate(any(org.opensmartgridplatform.domain.core.entities.ProtocolInfo.class)))
                .thenReturn(null);

        // Act
        final boolean result = protocolRequestMessageSender.isSupported(this.protocolInfo);

        // Assert
        assertFalse(result);
    }

    @Test
    public void testSend_SendsMessageSuccessfully() throws JMSException {
        // Arrange
        doNothing().when(this.jmsTemplate).send(any(MessageCreator.class));

        // Act
        protocolRequestMessageSender.send(this.protocolRequestMessage, this.protocolInfo);

        // Assert
        verify(this.jmsTemplate).send(messageCreatorCaptor.capture());
        final MessageCreator creator = messageCreatorCaptor.getValue();
        final ObjectMessage resultMessage = (ObjectMessage) creator.createMessage(this.session);
        assertEquals(this.correlationUid, resultMessage.getJMSCorrelationID());
        assertEquals(this.messageType, resultMessage.getJMSType());
        assertEquals(this.messagePriority, resultMessage.getJMSPriority());
        assertEquals(this.domain, resultMessage.getStringProperty(Constants.DOMAIN));
        assertEquals(this.domainVersion, resultMessage.getStringProperty(Constants.DOMAIN_VERSION));
        assertEquals(this.organisationIdentification, resultMessage.getStringProperty(Constants.ORGANISATION_IDENTIFICATION));
        assertEquals(this.deviceIdentification, resultMessage.getStringProperty(Constants.DEVICE_IDENTIFICATION));
        assertEquals(this.ipAddress, resultMessage.getStringProperty(Constants.IP_ADDRESS));
        assertEquals(this.isScheduled, resultMessage.getBooleanProperty(Constants.IS_SCHEDULED));
        assertEquals(this.retryCount, resultMessage.getIntProperty(Constants.RETRY_COUNT));
        assertEquals(this.bypassRetry, resultMessage.getBooleanProperty(Constants.BYPASS_RETRY));
    }

    @Test
    public void testSend_WithGetPowerUsageHistoryMessageType_SetsCustomTimeToLive() throws JMSException {
        // Arrange
        when(this.protocolRequestMessage.getMessageType()).thenReturn(GET_POWER_USAGE_HISTORY.toString());
        when(this.protocolRequestMessageJmsTemplateFactory.getJmsTemplate(any(org.opensmartgridplatform.domain.core.entities.ProtocolInfo.class)))
                .thenReturn(this.jmsTemplate);
        doNothing().when(this.jmsTemplate).send(any(MessageCreator.class));

        // Act
        protocolRequestMessageSender.send(this.protocolRequestMessage, this.protocolInfo);

        // Assert
        verify(this.jmsTemplate).setTimeToLive(this.timeToLive);
        verify(this.jmsTemplate).send(messageCreatorCaptor.capture());
        verify(this.jmsTemplate).setTimeToLive(this.timeToLive);
    }

    @Test
    public void testSend_WithNonParallelRequestsAllowed_SetsMessageGroup() throws JMSException {
        // Arrange
        when(this.protocolInfo.isParallelRequestsAllowed()).thenReturn(false);
        doNothing().when(this.jmsTemplate).send(any(MessageCreator.class));

        // Act
        protocolRequestMessageSender.send(this.protocolRequestMessage, this.protocolInfo);

        // Assert
        verify(this.jmsTemplate).send(messageCreatorCaptor.capture());
        final MessageCreator creator = messageCreatorCaptor.getValue();
        final ObjectMessage resultMessage = (ObjectMessage) creator.createMessage(this.session);
        assertEquals(this.deviceIdentification, resultMessage.getStringProperty(Constants.DEVICE_IDENTIFICATION));
    }

    @Test
    public void testSend_WithRetryCountNotZero_SendsCoreLogItemRequestMessage() {
        // Arrange
        when(this.protocolRequestMessage.getRetryCount()).thenReturn(1);
        doNothing().when(this.jmsTemplate).send(any(MessageCreator.class));

        // Act
        protocolRequestMessageSender.send(this.protocolRequestMessage, this.protocolInfo);

        // Assert
        verify(this.coreLogItemRequestMessageSender).send(any(CoreLogItemRequestMessage.class));
    }

    @Test
    public void testGetMessageGroupId_CalculatesCorrectGroupId() {
        // Arrange
        final String deviceIdentification = "device123";

        // Act
        final String groupId = protocolRequestMessageSender.getMessageGroupId(deviceIdentification);

        // Assert
        final int expectedGroupId = Math.abs(deviceIdentification.hashCode() % 100);
        assertEquals(String.valueOf(expectedGroupId), groupId);
    }
}
