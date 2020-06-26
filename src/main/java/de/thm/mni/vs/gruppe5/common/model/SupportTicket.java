package de.thm.mni.vs.gruppe5.common.model;

import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Date;

@Entity
public class SupportTicket implements Serializable {
    @Id @GeneratedValue(generator="system-uuid")
    @GenericGenerator(name="system-uuid", strategy = "uuid")
    private String id;

    private String customerId;

    private boolean isClosed;

    @Temporal(TemporalType.TIMESTAMP)
    private Date creationTime;

    @Temporal(TemporalType.TIMESTAMP)
    private Date closingTime;

    // Use length of 5000 characters, as jpa doesnt allow to simply set the number to the maximum
    @Column(length = 5000)
    private String text;

    public SupportTicket() {

    }

    public SupportTicket(String customerId, boolean isClosed, Date creationTime, Date closingTime, String text) {
        this.customerId = customerId;
        this.isClosed = isClosed;
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

    public boolean isClosed() {
        return isClosed;
    }

    public void setClosed(boolean closed) {
        isClosed = closed;
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

    public void appendText(String text) {
        this.text += "\n--------------\n" + text;
    }

    @Override
    public String toString() {
        return "SupportTicket{" +
                "id='" + id + '\'' +
                ", customerId='" + customerId + '\'' +
                ", isClosed=" + isClosed +
                ", creationTime=" + creationTime +
                ", closingTime=" + closingTime +
                ", text='" + text + '\'' +
                '}';
    }
}
