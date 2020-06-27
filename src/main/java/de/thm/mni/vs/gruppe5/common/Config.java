package de.thm.mni.vs.gruppe5.common;

public class Config {
    public static final int SERVER_PORT = 7000;
    public static final String SERVER_URL = "http://localhost:" + SERVER_PORT;
    public static final String ORDER_PATH  = "/orders";
    public static final String ORDER_URL = SERVER_URL + ORDER_PATH;
    public static final String TICKET_PATH  = "/tickets";
    public static final String TICKET_URL = SERVER_URL + TICKET_PATH;
    public static final String PARTS_PATH = "/parts";
    public static final String PARTS_URL = SERVER_URL + PARTS_PATH;
    public static final String PERFORMANCE_PATH  = "/performance";
    public static final String PERFORMANCE_URL = SERVER_URL + PERFORMANCE_PATH;
    public static final String FINISHED_ORDER_QUEUE = "finishedOrderQueue";
    public static final String FINISHED_TICKET_QUEUE = "finishedTicketQueue";
    public static final String ORDER_QUEUE = "orderQueue";
    public static final String TICKET_QUEUE = "ticketQueue";
    public static final String REPORT_QUEUE = "reportQueue";
    public static final String DEAD_LETTER_QUEUE = "ActiveMQ.DLQ";
    public static final String UPDATE_PARTS_COST_TOPIC_US = "updatePartsTopic-us";
    public static final String UPDATE_PARTS_COST_TOPIC_CN = "updatePartsTopic-cn";
    public static final float PRODUCTION_COST_PER_SECOND = 0.5f;
    public static final long DAY_DURATION_IN_SECONDS = 60;
    public static final long REPORTS_PER_DAY = 4;
    public static final float CHANCE_OF_CLOSING_TICKET = 0.4f;
}
