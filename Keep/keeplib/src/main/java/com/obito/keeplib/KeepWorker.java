package com.obito.keeplib;


import java.util.concurrent.atomic.AtomicLong;

public class KeepWorker implements Runnable {

    static final AtomicLong seq = new AtomicLong(0);
    private final long seqNum;

    public KeepWorker(Keep keep) {
        seqNum = seq.getAndIncrement();
    }

    @Override
    public void run() {

    }

    private void download(KeepTask task){

    }
}
