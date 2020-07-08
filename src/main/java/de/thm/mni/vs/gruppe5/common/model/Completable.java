package de.thm.mni.vs.gruppe5.common.model;

/**
 * This interface should be implemented by classes which need to simulate waiting times.
 */
public interface Completable {
    /**
     * Initialise object with a fixed waiting time.
     *
     * @param seconds waiting time in seconds
     */
    void init(int seconds);

    /**
     * Initialise object with a random waiting time.
     *
     * @param seconds maximum waiting time in seconds
     */
    void initRandom(int seconds);

    /**
     * Check for initialisation.
     *
     * @return whether or not the object was already initialised
     */
    boolean hasInit();

    /**
     * Simulate waiting. This method should use Thread.sleep until the waiting time is over
     *
     * @throws InterruptedException in case of manual interruption
     */
    void complete() throws InterruptedException;
}
