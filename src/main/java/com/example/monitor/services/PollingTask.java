package com.example.monitor.services;

import com.example.monitor.entities.Host;
import com.example.monitor.entities.Service;
import com.example.monitor.entities.ServiceState;

import java.io.IOException;
import java.net.Socket;
import java.time.LocalDateTime;
import java.util.logging.Logger;

/**
 * Async task, performs polling query to specified service
 * and notifies all the subscribers, if the service's state is changed
 */
public class PollingTask implements Runnable {
    private Host host;
    private Service service;

    private Logger logger = Logger.getLogger("main");

    public PollingTask(Host host, Service service) {
        this.host = host;
        this.service = service;
    }

    /**
     * Connection established callback
     */
    private void connected() {
        LocalDateTime currentTime = LocalDateTime.now();
        ServiceState currentState = service.getServiceState();

        logger.info(String.format("%s connected (%s)", host, currentState));

        service.setStateChangedTime(currentTime);
        service.setServiceState(ServiceState.UP);

        if(currentState == ServiceState.UNDEFINED || currentState == ServiceState.DOWN) {
            service.sendNotification(host);
        }
    }

    /**
     * Connection failed callback
     */
    private void disconnected() {
        LocalDateTime currentTime = LocalDateTime.now();
        ServiceState currentState = service.getServiceState();

        logger.info(String.format("%s connection lost (%s)", host, currentState));

        LocalDateTime lastAccessTime = service.getStateChangedTime();

        // If service was UP, but last connection attempt was failed
        if(currentState == ServiceState.UP) { // Mark service as MAY BE DOWN
            if(service.getGraceTime() == 0) { // If grace time is 0, mark service as DOWN
                service.setServiceState(ServiceState.DOWN);
                service.sendNotification(host);
            } else {                          // Otherwise mark service as MAY_BE_DOWN
                service.setServiceState(ServiceState.MAY_BE_DOWN);
            }
            service.setStateChangedTime(currentTime);
            return;
        }

        // If service state is UNKNOWN or grace time is up, mark service as DOWN
        boolean notifyNeeded = currentState == ServiceState.UNDEFINED || currentState == ServiceState.MAY_BE_DOWN &&
                lastAccessTime.plusSeconds(service.getGraceTime()).isBefore(currentTime.plusNanos(1)) ;

        if(notifyNeeded) {
            service.setServiceState(ServiceState.DOWN);
            service.setStateChangedTime(currentTime);
            service.sendNotification(host);
        }
    }

    public void run() {
        synchronized (host) {
            Socket socket = null;
            try {
                socket = new Socket(host.getHost(), host.getPort());
                if (socket.isConnected()) {
                    connected();
                } else {
                    disconnected();
                }
            } catch (Exception e) {
                disconnected();
            } finally {
                try {
                    if (socket != null) {
                        socket.close();
                    }
                } catch (IOException ignored) {
                }
            }
        }
    }
}