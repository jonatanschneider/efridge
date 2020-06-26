package de.thm.mni.vs.gruppe5.common;

import org.apache.activemq.ActiveMQConnectionFactory;

import javax.jms.*;

public class Subscriber implements AutoCloseable {
    private final Connection connection;
    private final MessageConsumer consumer;
    private final Session session;
    private Destination destination;
    private volatile boolean isRunning = true;

    /**
     * Subscribes to a channel asynchronously
     *
     * @param destinationName Name of the destination to subscribe to
     * @param messageListener Callback function reacting to messages
     * @throws JMSException Thrown in case of internal server error
     */
    public Subscriber(DestinationType type, String destinationName, MessageListener messageListener) throws JMSException {
        var connectionFactory = new ActiveMQConnectionFactory(ActiveMQConnectionFactory.DEFAULT_BROKER_URL);
        connectionFactory.setTrustAllPackages(true);
        connection = connectionFactory.createConnection();
        connection.start();

        session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);

        switch (type) {
            case QUEUE -> destination = session.createQueue(destinationName);
            case TOPIC -> destination = session.createTopic(destinationName);
        }
        consumer = session.createConsumer(destination);
        consumer.setMessageListener(messageListener);
    }

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
