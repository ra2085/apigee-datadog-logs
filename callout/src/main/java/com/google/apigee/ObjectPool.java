package com.google.apigee;

import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public abstract class ObjectPool<T>
{
    private ConcurrentLinkedQueue<T> pool;
    private ScheduledExecutorService executorService;

    public ObjectPool(final int minIdle) {
        // initialize pool
        initialize(minIdle);
    }

    public ObjectPool(final int minIdle, final int maxIdle, final long validationInterval) {
        initialize(minIdle);

        executorService = Executors.newSingleThreadScheduledExecutor();
        executorService.scheduleWithFixedDelay(new Runnable()
        {
            @Override
            public void run() {
                int size = pool.size();
                if (size < minIdle) {
                    int sizeToBeAdded = minIdle - size;
                    for (int i = 0; i < sizeToBeAdded; i++) {
                        try {
                            pool.add(createObject());
                        } catch (Exception e) {}
                    }
                } else if (size > maxIdle) {
                    int sizeToBeRemoved = size - maxIdle;
                    for (int i = 0; i < sizeToBeRemoved; i++) {
                        pool.poll();
                    }
                }
            }
        }, validationInterval, validationInterval, TimeUnit.MILLISECONDS);
    }

    public T borrowObject() {
        T object = null;
        while(true) {
            object = pool.poll();
            if (object == null) {
                try {
                    object = createObject();
                } catch (Exception e) {}
                return object;
            } else if (validateObject(object)) {
                return object;
            }
        }
    }

    public void returnObject(T object) {
        if (object == null) {
            return;
        }

        this.pool.offer(object);
    }

    public void shutdown() {
        if (executorService != null) {
            executorService.shutdown();
        }
    }

    protected abstract T createObject() throws Exception;

    protected abstract boolean validateObject(T object);

    private void initialize(final int minIdle) {
        pool = new ConcurrentLinkedQueue<T>();

        for (int i = 0; i < minIdle; i++) {
            try {
                pool.add(createObject());
            } catch (Exception e) {
                //IGNORED
            }
        }
    }
}
