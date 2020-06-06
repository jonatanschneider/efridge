package de.thm.mni.vs.gruppe5.factory.report;

import de.thm.mni.vs.gruppe5.common.PerformanceTracker;
import de.thm.mni.vs.gruppe5.common.Publisher;

import javax.jms.JMSException;
import java.util.TimerTask;

public class ReportTask extends TimerTask {
    private final Publisher publisher;

    public ReportTask(Publisher publisher) {
        this.publisher = publisher;
    }

    @Override
    public void run() {
        PerformanceTracker performanceTracker = PerformanceTracker.getInstance();
        System.out.println("Send new report: " + performanceTracker);

        try {
            publisher.publish(performanceTracker);
            performanceTracker.reset();
        } catch (JMSException e) {
            e.printStackTrace();
        }
    }
}
