package de.thm.mni.vs.gruppe5.common.model;

import java.io.Serializable;

/**
 * Represents the current status of an order
 */
public enum OrderStatus implements Serializable {
    RECEIVED, PRODUCING, COMPLETED
}
