package de.thm.mni.vs.gruppe5.common;

import org.apache.activemq.ActiveMQConnectionFactory;

import javax.jms.*;

/**
 * Instances of this class are used to receive object messages from a queue
 */
public class Subscriber implements AutoCloseable {
    private Connection connection;
    private Queue queue;
    private MessageConsumer consumer;
    private final Session session;
    private volatile boolean isRunning = true;

    /**
     * Subscribes to a channel asynchronously
     *
     * @param queueName       Name of the channel to subscribe to
     * @param messageListener Callback function reacting to messages
     * @throws JMSException Thrown in case of internal server error
     */
    public Subscriber(String queueName, MessageListener messageListener) throws JMSException {
        var connectionFactory = new ActiveMQConnectionFactory(ActiveMQConnectionFactory.DEFAULT_BROKER_URL);
        connectionFactory.setTrustAllPackages(true);
        connection = connectionFactory.createConnection();
        connection.start();

        session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
        queue = session.createQueue(queueName);
        consumer = session.createConsumer(queue);
        consumer.setMessageListener(messageListener);
    }

    /**
     * Close all resources
     */
    @Override
    public void close() {
        try {
            connection.close();
            consumer.close();
            session.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Temporarily pause queue subscription, do nothing if already paused
     */
    public synchronized void pause() {
        if (isRunning) {
            try {
                connection.stop();
            } catch (JMSException e) {
                e.printStackTrace();
            }
            isRunning = false;
        }
    }

    /**
     * Restart queue subscription, do nothing if already running
     */
    public synchronized void restart() {
        if (!isRunning) {
            try {
                connection.start();
            } catch (JMSException e) {
                e.printStackTrace();
            }
            isRunning = true;
        }
    }
}
