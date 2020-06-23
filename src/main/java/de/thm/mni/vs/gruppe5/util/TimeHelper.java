package de.thm.mni.vs.gruppe5.util;

import java.util.Random;

public class TimeHelper {
    public static void waitRandom(int maxSeconds) {
        var r = new Random();
        var seconds = r.nextInt(maxSeconds);
        waitTime(seconds);
    }

    public static void waitTime(long seconds) {
        try {
            System.out.println("Waiting " + seconds + " seconds");
            Thread.sleep(seconds * 1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
