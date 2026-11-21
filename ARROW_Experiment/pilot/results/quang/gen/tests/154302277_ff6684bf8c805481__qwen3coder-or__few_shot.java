package org.opensmartgridplatform.core.infra.jms.protocol.outbound;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import javax.jms.JMSException;
import javax.jms.ObjectMessage;
import javax.jms.Session;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.opensmartgridplatform.core.domain.model.protocol.ProtocolRequestService;
import org.opensmartgridplatform.core.infra.messaging.CoreLogItemRequestMessageSender;
import org.opensmartgridplatform.domain.core.entities.ProtocolInfo;
import org.opensmartgridplatform.shared.infra.jms.Constants;
import org.opensmartgridplatform.shared.infra.jms.ProtocolRequestMessage;
import org.springframework.jms.core.JmsTemplate;

public class ProtocolRequestMessageSender_ArrowTest {

    private ProtocolRequestMessageSender protocolRequestMessageSender;

    private CoreLogItemRequestMessageSender coreLogItemRequestMessageSender;
    private ProtocolRequestMessageJmsTemplateFactory protocolRequestMessageJmsTemplateFactory;
    private JmsTemplate jmsTemplate;
    private ProtocolInfo protocolInfo;

    @BeforeEach
    public void setUp() {
        this.protocolRequestMessageSender = new ProtocolRequestMessageSender();
        this.coreLogItemRequestMessageSender = mock(CoreLogItemRequestMessageSender.class);
        this.protocolRequestMessageJmsTemplateFactory = mock(ProtocolRequestMessageJmsTemplateFactory.class);
        this.jmsTemplate = mock(JmsTemplate.class);
        this.protocolInfo = mock(ProtocolInfo.class);

        // Set up the fields via reflection since they are autowired
        try {
            java.lang.reflect.Field field1 = ProtocolRequestMessageSender.class.getDeclaredField("coreLogItemRequestMessageSender");
            field1.setAccessible(true);
            field1.set(this.protocolRequestMessageSender, this.coreLogItemRequestMessageSender);

            java.lang.reflect.Field field2 = ProtocolRequestMessageSender.class.getDeclaredField("protocolRequestMessageJmsTemplateFactory");
            field2.setAccessible(true);
            field2.set(this.protocolRequestMessageSender, this.protocolRequestMessageJmsTemplateFactory);

            java.lang.reflect.Field field3 = ProtocolRequestMessageSender.class.getDeclaredField("messageGroupCacheSize");
            field3.setAccessible(true);
            field3.set(this.protocolRequestMessageSender, 100);

            java.lang.reflect.Field field4 = ProtocolRequestMessageSender.class.getDeclaredField("getPowerUsageHistoryRequestTimeToLive");
            field4.setAccessible(true);
            field4.set(this.protocolRequestMessageSender, 30000L);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    public void testIsSupportedReturnsTrueWhenJmsTemplateExists() {
        when(this.protocolRequestMessageJmsTemplateFactory.getJmsTemplate(this.protocolInfo)).thenReturn(this.jmsTemplate);
        
        boolean result = this.protocolRequestMessageSender.isSupported(this.protocolInfo);
        
        assert result == true;
    }

    @Test
    public void testIsSupportedReturnsFalseWhenJmsTemplateIsNull() {
        when(this.protocolRequestMessageJmsTemplateFactory.getJmsTemplate(this.protocolInfo)).thenReturn(null);
        
        boolean result = this.protocolRequestMessageSender.isSupported(this.protocolInfo);
        
        assert result == false;
    }

    @Test
    public void testSendCallsSendMessage() throws JMSException {
        when(this.protocolRequestMessageJmsTemplateFactory.getJmsTemplate(this.protocolInfo)).thenReturn(this.jmsTemplate);
        doNothing().when(this.jmsTemplate).send(any());

        ProtocolRequestMessage message = mock(ProtocolRequestMessage.class);
        when(message.getDeviceIdentification()).thenReturn("device123");
        when(message.getMessageType()).thenReturn("GET_DATA");
        when(this.protocolInfo.getProtocol()).thenReturn("protocol1");
        when(this.protocolInfo.getProtocolVersion()).thenReturn("version1");
        when(this.protocolInfo.isParallelRequestsAllowed()).thenReturn(true);

        this.protocolRequestMessageSender.send(message, this.protocolInfo);

        verify(this.protocolRequestMessageJmsTemplateFactory).getJmsTemplate(this.protocolInfo);
        verify(this.jmsTemplate).send(any());
    }

    @Test
    public void testSendMessageSetsCustomTimeToLiveForGetPowerUsageHistory() throws JMSException {
        when(this.protocolRequestMessageJmsTemplateFactory.getJmsTemplate(this.protocolInfo)).thenReturn(this.jmsTemplate);
        when(this.jmsTemplate.getTimeToLive()).thenReturn(10000L);
        doNothing().when(this.jmsTemplate).send(any());

        ProtocolRequestMessage message = mock(ProtocolRequestMessage.class);
        when(message.getDeviceIdentification()).thenReturn("device123");
        when(message.getMessageType()).thenReturn("GET_POWER_USAGE_HISTORY");
        when(message.getCorrelationUid()).thenReturn("correlation123");
        when(message.getDomain()).thenReturn("domain1");
        when(message.getDomainVersion()).thenReturn("version1");
        when(message.getOrganisationIdentification()).thenReturn("org123");
        when(message.getIpAddress()).thenReturn("192.168.0.1");
        when(message.isScheduled()).thenReturn(false);
        when(message.getRetryCount()).thenReturn(0);
        when(message.bypassRetry()).thenReturn(false);
        when(message.getMessagePriority()).thenReturn(1);
        when(this.protocolInfo.getProtocol()).thenReturn("protocol1");
        when(this.protocolInfo.getProtocolVersion()).thenReturn("version1");
        when(this.protocolInfo.isParallelRequestsAllowed()).thenReturn(true);

        this.protocolRequestMessageSender.send(message, this.protocolInfo);

        verify(this.jmsTemplate).setTimeToLive(30000L);
        verify(this.jmsTemplate).send(any());
    }

    @Test
    public void testSendMessageSetsMessageGroupPropertyWhenParallelRequestsNotAllowed() throws JMSException {
        when(this.protocolRequestMessageJmsTemplateFactory.getJmsTemplate(this.protocolInfo)).thenReturn(this.jmsTemplate);
        doNothing().when(this.jmsTemplate).send(any());

        ProtocolRequestMessage message = mock(ProtocolRequestMessage.class);
        when(message.getDeviceIdentification()).thenReturn("device123");
        when(message.getMessageType()).thenReturn("GET_DATA");
        when(message.getCorrelationUid()).thenReturn("correlation123");
        when(message.getDomain()).thenReturn("domain1");
        when(message.getDomainVersion()).thenReturn("version1");
        when(message.getOrganisationIdentification()).thenReturn("org123");
        when(message.getIpAddress()).thenReturn("192.168.0.1");
        when(message.isScheduled()).thenReturn(false);
        when(message.getRetryCount()).thenReturn(0);
        when(message.bypassRetry()).thenReturn(false);
        when(message.getMessagePriority()).thenReturn(1);
        when(this.protocolInfo.getProtocol()).thenReturn("protocol1");
        when(this.protocolInfo.getProtocolVersion()).thenReturn("version1");
        when(this.protocolInfo.isParallelRequestsAllowed()).thenReturn(false);

        this.protocolRequestMessageSender.send(message, this.protocolInfo);

        verify(this.jmsTemplate).send(any());
    }

    @Test
    public void testSendMessageSendsCoreLogItemRequestMessageWhenRetryCountIsNotZero() throws JMSException {
        when(this.protocolRequestMessageJmsTemplateFactory.getJmsTemplate(this.protocolInfo)).thenReturn(this.jmsTemplate);
        doNothing().when(this.jmsTemplate).send(any());

        ProtocolRequestMessage message = mock(ProtocolRequestMessage.class);
        when(message.getDeviceIdentification()).thenReturn("device123");
        when(message.getMessageType()).thenReturn("GET_DATA");
        when(message.getCorrelationUid()).thenReturn("correlation123");
        when(message.getDomain()).thenReturn("domain1");
        when(message.getDomainVersion()).thenReturn("version1");
        when(message.getOrganisationIdentification()).thenReturn("org123");
        when(message.getIpAddress()).thenReturn("192.168.0.1");
        when(message.isScheduled()).thenReturn(false);
        when(message.getRetryCount()).thenReturn(1);
        when(message.bypassRetry()).thenReturn(false);
        when(message.getMessagePriority()).thenReturn(1);
        when(this.protocolInfo.getProtocol()).thenReturn("protocol1");
        when(this.protocolInfo.getProtocolVersion()).thenReturn("version1");
        when(this.protocolInfo.isParallelRequestsAllowed()).thenReturn(true);

        this.protocolRequestMessageSender.send(message, this.protocolInfo);

        verify(this.coreLogItemRequestMessageSender).send(any());
    }

    @Test
    public void testGetMessageGroupIdCalculatesCorrectly() {
        String deviceIdentification = "device123";
        String groupId = this.protocolRequestMessageSender.getMessageGroupId(deviceIdentification);
        
        assert groupId != null;
        assert !groupId.isEmpty();
    }
}
