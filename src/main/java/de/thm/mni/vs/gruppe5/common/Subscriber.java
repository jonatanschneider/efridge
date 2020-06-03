package de.thm.mni.vs.gruppe5.common;

import org.apache.activemq.ActiveMQConnectionFactory;

import javax.jms.*;

public class Subscriber {
    private Queue queue;
    private MessageConsumer consumer;
    private final Session session;

    /**
     * Subscribes to a channel asynchronously
     *
     * @param queueName Name of the channel to subscribe to
     * @param messageListener Callback function reacting to messages
     * @throws JMSException Thrown in case of internal server error
     */
    public Subscriber(String queueName, MessageListener messageListener) throws JMSException {
        var connectionFactory = new ActiveMQConnectionFactory(ActiveMQConnectionFactory.DEFAULT_BROKER_URL);
        connectionFactory.setTrustAllPackages(true);
        var connection = connectionFactory.createConnection();
        connection.start();

        session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
        queue = session.createQueue(queueName);
        consumer = session.createConsumer(queue);
        consumer.setMessageListener(messageListener);
    }
}
