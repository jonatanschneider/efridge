package de.thm.mni.vs.gruppe5.common;

public class Config {
    public static final String ORDER_URL = "http://localhost:7000/orders";
    public static final String TICKET_URL = "http://localhost:7000/tickets";
    public static final String FINISHED_ORDER_QUEUE = "finishedOrderQueue";
    public static final String FINISHED_TICKET_QUEUE = "finishedTicketQueue";
    public static final String ORDER_QUEUE = "orderQueue";
    public static final String TICKET_QUEUE = "ticketQueue";
    public static final String REPORT_QUEUE = "reportQueue";
    public static final float PRODUCTION_COST_PER_SECOND = 0.5f;
    public static final long DAY_DURATION_IN_SECONDS = 60;
    public static final long REPORTS_PER_DAY = 4;
    public static final float CHANCE_OF_CLOSING_TICKET = 0.4f;
}
