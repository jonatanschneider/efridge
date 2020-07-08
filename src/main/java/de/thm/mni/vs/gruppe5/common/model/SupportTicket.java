package de.thm.mni.vs.gruppe5.common.model;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Date;
import java.util.Random;
import java.util.UUID;

/**
 * Representation of a support ticket
 */
@Entity
public class SupportTicket implements Serializable, Completable {
    @Id
    private String id = UUID.randomUUID().toString();

    private String customerId;

    private TicketStatus status;

    @Temporal(TemporalType.TIMESTAMP)
    private Date creationTime;

    @Temporal(TemporalType.TIMESTAMP)
    private Date closingTime;

    @Temporal(TemporalType.TIMESTAMP)
    private Date completedAt;

    // Use length of 5000 characters, as jpa doesnt allow to simply set the number to the maximum
    @Column(length = 5000)
    private String text;

    public SupportTicket() {

    }

    public SupportTicket(String customerId, TicketStatus status, Date creationTime, Date closingTime, String text) {
        this.customerId = customerId;
        this.status = status;
        this.creationTime = creationTime;
        this.closingTime = closingTime;
        this.text = text;
    }

    public String getId() {
        return id;
    }

    public String getCustomerId() {
        return customerId;
    }

    public void setCustomerId(String customerId) {
        this.customerId = customerId;
    }

    public TicketStatus getStatus() {
        return status;
    }

    public void setStatus(TicketStatus status) {
        this.status = status;
    }

    public Date getCreationTime() {
        return creationTime;
    }

    public void setCreationTime(Date creationTime) {
        this.creationTime = creationTime;
    }

    public Date getClosingTime() {
        return closingTime;
    }

    public void setClosingTime(Date closingTime) {
        this.closingTime = closingTime;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    /**
     * Append a text to the current content of the ticket
     * Added text will be separated by a dashed line from the old content
     *
     * @param text text to be added
     */
    public void appendText(String text) {
        this.text += "\n--------------\n" + text;
    }

    @Override
    public String toString() {
        return "SupportTicket{" +
                "id='" + id + '\'' +
                ", customerId='" + customerId + '\'' +
                ", status=" + status +
                ", creationTime=" + creationTime +
                ", closingTime=" + closingTime +
                ", completedAt=" + completedAt +
                ", text='" + text + '\'' +
                '}';
    }

    /**
     * Initialise waiting time by storing a Date in the future
     *
     * @param seconds waiting time in seconds
     */
    @Override
    public void init(int seconds) {
        completedAt = new Date(System.currentTimeMillis() + seconds * 1000);
    }

    /**
     * Initialise waiting time by storing a Date in the future. Pick a random waiting time from 1 to parameter.
     *
     * @param seconds maximum waiting time in seconds
     */
    @Override
    public void initRandom(int seconds) {
        init(new Random().nextInt(seconds) + 1);
    }

    /**
     * @return whether or not a completion date has been set
     */
    @Override
    public boolean hasInit() {
        return completedAt != null;
    }

    /**
     * Uses Thread.sleep to wait until completion date is in the past.
     *
     * @throws InterruptedException in case of manual interruption
     */
    @Override
    public void complete() throws InterruptedException {
        while (completedAt.after(new Date(System.currentTimeMillis())))
            Thread.sleep(1000);
    }
}
