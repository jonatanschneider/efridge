package de.thm.mni.vs.gruppe5.common.model;

public interface Completable {
    void init(int seconds);
    void initRandom(int seconds);
    boolean hasInit();
    void complete() throws InterruptedException;
}
