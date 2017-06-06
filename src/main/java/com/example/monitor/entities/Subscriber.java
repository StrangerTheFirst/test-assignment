package com.example.monitor.entities;

import java.util.ArrayList;
import java.util.List;

public class Subscriber {
    private List<Notification> notificationList;

    public Subscriber() {
        this.notificationList = new ArrayList<>();
    }

    synchronized public List<Notification> getNotificationList() {
        return notificationList;
    }

    synchronized public void send(Notification notification) {
        notificationList.add(notification);
    }
}
