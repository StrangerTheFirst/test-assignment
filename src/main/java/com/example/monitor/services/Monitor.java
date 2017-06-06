package com.example.monitor.services;

import com.example.monitor.entities.*;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Service monitor class.
 * Class runs in its own thread
 */
public class Monitor extends Thread {
    private Map<Host, Service> config = new ConcurrentHashMap<>();

    private volatile boolean isThreadRunning = true;
    private ExecutorService threadPool = Executors.newCachedThreadPool();

    public void run() {
        try {
            while (isThreadRunning) {
                try {
                    config.forEach((k,v) -> {
                        // If it's time to perform polling query to host k,
                        // run new async task to do that
                        if(v.isTimeToRun())
                            threadPool.submit(new PollingTask(k, v));
                    });

                    // Service should not be polled more frequently than once a second
                    Thread.sleep(1000);
                } catch (Exception ignored) { }
            }
        } catch (Exception ignored) { }
    }

    public void stopServer() {
        isThreadRunning = false;
    }

    /**
     *
     * @param subscriber New subscriber, interested in the service
     * @param host Service host/port combination
     * @param pollingFrequency Polling frequency
     */
    public void subscribe(Subscriber subscriber, Host host, int pollingFrequency) {
        // Polling frequency should be more than a second
        pollingFrequency = pollingFrequency < 1 ? 1 : pollingFrequency;

        Service service = config.get(host);
        if(service == null) {
            config.put(host, new Service(pollingFrequency, subscriber));
        } else {
            // Calculating minimum polling frequency for server
            if(service.getPollingFrequency() > pollingFrequency) {
                service.setPollingFrequency(pollingFrequency);
                service.getSubscribers().add(subscriber);
            }
        }
    }

    public void setOutageTime(Host host, OutageTime outageTime) {
        Service service = config.get(host);
        if(service != null) {
            service.setOutageTime(outageTime);
        }
    }

    public void setGraceTime(Host host, int graceTime) {
        Service service = config.get(host);
        if(service != null) {
            service.setGraceTime(graceTime);
        }
    }
}