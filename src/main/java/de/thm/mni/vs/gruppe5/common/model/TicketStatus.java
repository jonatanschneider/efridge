package de.thm.mni.vs.gruppe5.common.model;

import java.io.Serializable;

/**
 * Represents the current status of a ticket
 */
public enum TicketStatus implements Serializable {
    RECEIVED, WAITING, CLOSED
}
