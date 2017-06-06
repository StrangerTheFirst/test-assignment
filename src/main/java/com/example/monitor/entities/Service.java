package com.example.monitor.entities;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Service parameters
 */
public class Service {
    private OutageTime outageTime;
    private int pollingFrequency;
    private int graceTime;

    private LocalDateTime stateChangedTime;
    private ServiceState serviceState = ServiceState.UNDEFINED;

    private List<Subscriber> subscribers = new ArrayList<>();

    public Service(int pollingFrequency, Subscriber subscriber) {
        this.pollingFrequency = pollingFrequency;
        subscribers.add(subscriber);
    }

    public Service() {
    }

    synchronized public void subscribe(Subscriber subscriber) {
        subscribers.add(subscriber);
    }

    synchronized public OutageTime getOutageTime() {
        return outageTime;
    }

    synchronized public void setOutageTime(OutageTime outageTime) {
        this.outageTime = outageTime;
    }

    synchronized public int getPollingFrequency() {
        return pollingFrequency;
    }

    synchronized public void setPollingFrequency(int pollingFrequency) {
        this.pollingFrequency = pollingFrequency;
    }

    synchronized public int getGraceTime() {
        return graceTime;
    }

    synchronized public void setGraceTime(int graceTime) {
        this.graceTime = graceTime;
    }

    synchronized public ServiceState getServiceState() {
        return serviceState;
    }

    public void setServiceState(ServiceState serviceState) {
        this.serviceState = serviceState;
    }

    synchronized public LocalDateTime getStateChangedTime() {
        return stateChangedTime;
    }

    synchronized public void setStateChangedTime(LocalDateTime stateChangedTime) {
        this.stateChangedTime = stateChangedTime;
    }

    synchronized public List<Subscriber> getSubscribers() {
        return subscribers;
    }

    /**
     * Notifies all the subscribers of current host
     */
    synchronized public void sendNotification(Host host) {
        subscribers.stream().forEach(v -> v.send(new Notification(host, stateChangedTime, serviceState)));
    }

    /**
     * Allows to schedule extra-checks, when the server is down, but grace time is not up
     *
     * @return Polling interval (minimum value between pollingFrequency and graceTime)
     */
    private int getPollingInterval() {
        return graceTime > 0 && graceTime < pollingFrequency ? graceTime : pollingFrequency;
    }

    /**
     * Checks, is it now a time to send new polling query to current service.
     * Specified outage time is considered.
     */
    synchronized public boolean isTimeToRun() {
        LocalDateTime currentTime = LocalDateTime.now();

        OutageTime outageTime = getOutageTime();
        LocalDateTime lastAccessTime = getStateChangedTime();

        return (outageTime == null || outageTime.isInOutage(currentTime.toLocalTime())) &&
               (lastAccessTime == null || currentTime.isAfter(lastAccessTime.plusSeconds(getPollingInterval()).minusNanos(1)));
    }
}
