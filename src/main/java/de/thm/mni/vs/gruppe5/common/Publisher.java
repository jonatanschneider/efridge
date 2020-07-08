package de.thm.mni.vs.gruppe5.common;

import org.apache.activemq.ActiveMQConnectionFactory;

import javax.jms.*;
import java.io.Serializable;

/**
 * Instances of this class are used to publish object messages to a queue
 */
public class Publisher implements AutoCloseable {
    private final Connection connection;
    private Queue queue;
    private MessageProducer producer;
    private final Session session;

    /**
     * Create an instance to publish messages later
     *
     * @param queueName Name of the channel to publish to
     * @throws JMSException Thrown in case of internal server error
     */
    public Publisher(String queueName) throws JMSException {
        var connectionFactory = new ActiveMQConnectionFactory(ActiveMQConnectionFactory.DEFAULT_BROKER_URL);
        connectionFactory.setTrustAllPackages(true);
        connection = connectionFactory.createConnection();
        connection.start();

        session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
        queue = session.createQueue(queueName);
        producer = session.createProducer(queue);
        producer.setTimeToLive(1000);
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

    /**
     * Close all resources
     */
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
