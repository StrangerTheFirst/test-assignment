package com.example.monitor.entities;

/**
 * Host is defined as host:port combination
 */
public class Host {
    private String host;
    private int port;

    public Host() {
    }

    public Host(String host, int port) {
        this.host = host;
        this.port = port;
    }

    /**
     * @return Hostname
     */
    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    /**
     * @return Server port
     */
    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Host host1 = (Host) o;

        if (port != host1.port) return false;
        return host != null ? host.equals(host1.host) : host1.host == null;
    }

    @Override
    public int hashCode() {
        int result = host != null ? host.hashCode() : 0;
        result = 31 * result + port;
        return result;
    }

    @Override
    public String toString() {
        return host + ":" + port;
    }
}
