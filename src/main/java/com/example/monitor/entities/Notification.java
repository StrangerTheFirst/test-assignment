package com.example.monitor.entities;

import java.time.LocalDateTime;

/**
 * Notification, sent to subscribers
 */
public class Notification {
    private Host host;
    private LocalDateTime accessTime;
    private ServiceState serviceState;

    public Notification() {
    }

    public Notification(Host host, LocalDateTime accessTime, ServiceState serviceState) {
        this.host = host;
        this.accessTime = accessTime;
        this.serviceState = serviceState;
    }

    /**
     * @return Host, which has changed its state
     */
    public Host getHost() {
        return host;
    }

    public void setHost(Host host) {
        this.host = host;
    }

    /**
     * @return Time, when service has changed its state
     */
    public LocalDateTime getAccessTime() {
        return accessTime;
    }

    public void setAccessTime(LocalDateTime accessTime) {
        this.accessTime = accessTime;
    }

    /**
     * @return New service state
     */
    public ServiceState getServiceState() {
        return serviceState;
    }

    public void setServiceState(ServiceState serviceState) {
        this.serviceState = serviceState;
    }
}