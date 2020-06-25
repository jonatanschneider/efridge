package de.thm.mni.vs.gruppe5.common.model;

import java.util.Date;
import java.util.Random;

public abstract class Completable {
    protected Date completedAt;

    protected void completeAfter(long seconds) {
        completedAt = new Date(System.currentTimeMillis() + seconds * 1000);
    }

    protected void waitCompletition() {
        while (completedAt.after(new Date(System.currentTimeMillis()))) {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                System.out.println("Manually interrupted waiting");
            }
        }
    }

    public void waitCompletition(long seconds) {
        completeAfter(seconds);
        waitCompletition();
    }

    public void waitCompletitionRandom(int seconds) {
        completeAfter(new Random().nextInt(seconds) + 1);
        waitCompletition();
    }
}
