package com.example.monitor.entities;

/**
 * Service states
 */
public enum ServiceState {
    /**
     * Service is running
     */
    UP,
    /**
     * Service is down, but grace time is not up
     */
    MAY_BE_DOWN,
    /**
     * Service is down and grace time is up
     */
    DOWN,
    /**
     * Unknown server state (initial value)
     */
    UNDEFINED
}
