package org.fastdownload.file;

import java.io.File;
import java.util.concurrent.CountDownLatch;

public class Test {
    public static void main(String[] args) {
//        File source = new File("C:\\FastDownload\\Data\\1.flv");

        final int N = 10;
        CountDownLatch startSignal = new CountDownLatch(1);
        CountDownLatch doneSignal = new CountDownLatch(N);

        for (int i = 0; i < N; ++i) // create and start threads
        {
            new Thread(new Worker(startSignal, doneSignal)).start();
        }

//        doSomethingElse();            // don't let run yet
        startSignal.countDown();      // let all threads proceed
//        doSomethingElse();
        try {
            doneSignal.await();           // wait for all to finish
//            System.out.println(doneSignal.getCount());
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        System.err.println("全部完成");

    }
}

class Worker implements Runnable {
    private final CountDownLatch startSignal;
    private final CountDownLatch doneSignal;

    Worker(CountDownLatch startSignal, CountDownLatch doneSignal) {
        this.startSignal = startSignal;
        this.doneSignal = doneSignal;
    }

    @Override
    public void run() {
        try {
            startSignal.await();
            doWork();
            doneSignal.countDown();
        } catch (InterruptedException ex) {
        } // return;
    }

    void doWork() {
        // TODO
        System.out.println("\t" + Thread.currentThread().getName());
    }
}

