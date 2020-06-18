package de.thm.mni.vs.gruppe5.common.model;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Date;

@Entity
public class SupportTicket implements Serializable {
    @Id
    @GeneratedValue
    private String id;

    private String customerId;

    private boolean isClosed;

    @Temporal(TemporalType.TIMESTAMP)
    private Date creationTime;

    @Temporal(TemporalType.TIMESTAMP)
    private Date closingTime;

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
}
