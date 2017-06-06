package com.example.monitor.entities;

import java.time.LocalTime;

public class OutageTime {
    private LocalTime startOutageTime;
    private LocalTime endOutageTime;

    synchronized public LocalTime getStartOutageTime() {
        return startOutageTime;
    }

    synchronized public void setStartOutageTime(LocalTime startOutageTime) {
        this.startOutageTime = startOutageTime;
    }

    synchronized public LocalTime getEndOutageTime() {
        return endOutageTime;
    }

    synchronized public void setEndOutageTime(LocalTime endOutageTime) {
        this.endOutageTime = endOutageTime;
    }

    synchronized public boolean isInOutage(LocalTime currentTime) {
        if(startOutageTime != null && endOutageTime != null) {
            return currentTime.isAfter(startOutageTime.minusNanos(1)) &&
                    currentTime.isBefore(endOutageTime.plusNanos(1));
        }
        return false;
    }
}