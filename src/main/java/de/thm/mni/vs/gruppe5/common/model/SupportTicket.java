package de.thm.mni.vs.gruppe5.common.model;

import org.eclipse.persistence.jpa.jpql.parser.DateTime;

public class SupportTicket {
    private String id;

    private String customerId;

    private boolean isClosed;

    private DateTime creationTime;

    private DateTime closingTime;

    private String text;
}
