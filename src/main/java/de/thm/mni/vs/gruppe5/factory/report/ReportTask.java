package de.thm.mni.vs.gruppe5.factory.report;

import de.thm.mni.vs.gruppe5.common.Config;
import de.thm.mni.vs.gruppe5.common.Location;
import de.thm.mni.vs.gruppe5.common.PerformanceTracker;
import de.thm.mni.vs.gruppe5.common.Publisher;
import de.thm.mni.vs.gruppe5.common.model.Performance;

import javax.jms.JMSException;
import java.util.TimerTask;

public class ReportTask extends TimerTask {
    private int reportCount;
    private final Location location;
    private final Publisher publisher;

    public ReportTask(Location location, Publisher publisher) {
        this.reportCount = 0;
        this.location = location;
        this.publisher = publisher;
    }

    @Override
    public void run() {
        PerformanceTracker performanceTracker = PerformanceTracker.getInstance();
        System.out.println("Send new report: " + performanceTracker.getPerformance());

        reportCount++;

        try {
            Performance performance = performanceTracker.getPerformance();
            performance.setLocation(location);
            publisher.publish(performance);
        } catch (JMSException e) {
            e.printStackTrace();
        }

        // Reset data when the last report for today was sent
        if (isTodaysLastReport()) {
            System.out.println("Day passed: reset report data.");
            performanceTracker.reset();
        }
    }

    public long getPeriod() {
        return Config.DAY_DURATION_IN_SECONDS / Config.REPORTS_PER_DAY * 1000;
    }

    private boolean isTodaysLastReport() {
        return reportCount % Config.REPORTS_PER_DAY == 0;
    }
}
