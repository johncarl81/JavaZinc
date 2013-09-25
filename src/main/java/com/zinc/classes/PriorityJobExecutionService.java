package com.zinc.classes;

import com.zinc.exceptions.ZincRuntimeException;

import java.util.Comparator;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.*;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * User: NachoSoto
 * Date: 9/21/13
 */
public class PriorityJobExecutionService<Input, Output> {
    private static final int INITIAL_QUEUE_CAPACITY = 20;
    private static final int SCHEDULER_TERMINATION_TIMEOUT = 30;
    private static final int EXECUTOR_SERVICE_TERMINATION_TIMEOUT = 300;

    private final int mConcurrency;
    private final ThreadFactory mThreadFactory;
    private final DataProcessor<Input, Output> mDataProcessor;

    private ExecutorService mScheduler;
    private ExecutorService mExecutorService;

    private final PriorityBlockingQueue<Input> mQueue;
    private final Map<Input, Future<Output>> mFutures = new ConcurrentHashMap<Input, Future<Output>>();
    private final Set<Input> mAddedElements = new HashSet<Input>();

    private final Lock lock = new ReentrantLock();
    private final Condition enqueued = lock.newCondition();

    public PriorityJobExecutionService(final int concurrency,
                                       final ThreadFactory threadFactory,
                                       final Comparator<Input> priorityComparator,
                                       final DataProcessor<Input, Output> dataProcessor) {
        mConcurrency = concurrency;
        mThreadFactory = threadFactory;
        mDataProcessor = dataProcessor;

        mQueue = new PriorityBlockingQueue<Input>(INITIAL_QUEUE_CAPACITY, priorityComparator);
    }

    private boolean isRunning() {
        return (mScheduler != null || mExecutorService != null);
    }

    public synchronized void start() {
        if (isRunning()) {
            throw new ZincRuntimeException("Service is already running.");
        }

        mScheduler =  Executors.newSingleThreadExecutor();
        mExecutorService = Executors.newFixedThreadPool(mConcurrency, mThreadFactory);

        mScheduler.submit(new Runnable() {
            public void run() {
                try {
                    Input data;
                    while ((data = mQueue.take()) != null) {
                        lock.lock();

                        try {
                            mFutures.put(data, submit(data));
                            enqueued.signal();
                        } finally {
                            lock.unlock();
                        }
                    }
                } catch (InterruptedException ex) {
                    Thread.currentThread().interrupt();
                }
            }
        });
    }

    public synchronized boolean stop() throws InterruptedException {
        if (!isRunning()) {
            throw new ZincRuntimeException("Service is already stopped.");
        }

        boolean stopped = false;
        mScheduler.shutdownNow();
        stopped = mScheduler.awaitTermination(SCHEDULER_TERMINATION_TIMEOUT, TimeUnit.SECONDS);

        mExecutorService.shutdown();
        stopped &= mExecutorService.awaitTermination(EXECUTOR_SERVICE_TERMINATION_TIMEOUT, TimeUnit.SECONDS);

        if (stopped) {
            mScheduler = mExecutorService = null;
        }

        return stopped;
    }

    public void add(final Input element) {
        mAddedElements.add(element);
        mQueue.put(element);
    }

    public Future<Output> get(final Input element) {
        if (mAddedElements.contains(element)) {
            return waitForFuture(element);
        } else {
            throw new JobNotFoundException(element);
        }
    }

    private Future<Output> submit(final Input element) {
        return mExecutorService.submit(mDataProcessor.process(element));
    }

        private Future<Output> waitForFuture(final Input element) {
            Future<Output> result;
            lock.lock();

            try {
                while ((result = mFutures.get(element)) == null) {
                    enqueued.awaitUninterruptibly();
                }
            } finally {
                lock.unlock();
            }

        return result;
    }

    public static interface DataProcessor <Input, Output> {
        Callable<Output> process(Input data);
    }

    public static class JobNotFoundException extends ZincRuntimeException {
        public JobNotFoundException(final Object object) {
            super("Object '" + object.toString() + "' had not been added");
        }
    }
}