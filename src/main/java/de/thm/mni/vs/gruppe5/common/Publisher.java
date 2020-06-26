package de.thm.mni.vs.gruppe5.common;

import org.apache.activemq.ActiveMQConnectionFactory;

import javax.jms.*;
import java.io.Serializable;

public class Publisher implements AutoCloseable {
    private final Connection connection;
    private final MessageProducer producer;
    private final Session session;
    private Destination destination;

    /**
     * Create an instance to publish messages later
     *
     * @param destinationName Name of the destination to publish to
     * @throws JMSException Thrown in case of internal server error
     */
    public Publisher(DestinationType type, String destinationName) throws JMSException {
        var connectionFactory = new ActiveMQConnectionFactory(ActiveMQConnectionFactory.DEFAULT_BROKER_URL);
        connectionFactory.setTrustAllPackages(true);
        connection = connectionFactory.createConnection();
        connection.start();

        session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
        switch (type) {
            case QUEUE -> destination = session.createQueue(destinationName);
            case TOPIC -> destination = session.createTopic(destinationName);
        }

        producer = session.createProducer(destination);
    }

    /**
     * Publish a serializable object
     *
     * @param object serializable object to publish
     * @throws JMSException Thrown in case of internal server error
     */
    public void publish(Serializable object) throws JMSException {
        var msg = session.createObjectMessage();
        msg.setObject(object);
        producer.send(msg);
    }

    @Override
    public void close() {
        try {
            connection.close();
            producer.close();
            session.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
