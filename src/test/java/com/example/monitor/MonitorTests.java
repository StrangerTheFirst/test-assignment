package com.example.monitor;

import com.example.monitor.entities.*;
import com.example.monitor.services.Monitor;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.io.IOException;
import java.net.*;
import java.time.LocalTime;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class MonitorTests extends MonitorApplicationTests {

    private Monitor monitor;

    private final static String A_HOSTNAME = "127.0.22.21";
    private final static int A_PORT = 9000;

    private final static String B_HOSTNAME = "127.11.22.21";
    private final static int B_PORT = 19000;

    @Before
    public void setUp() {
        monitor = new Monitor();
    }

    @Test
    public void shouldNotifySubscribers() throws IOException, InterruptedException {
        Host host = new Host(A_HOSTNAME, A_PORT);

        Subscriber subscriber1 = new Subscriber();
        final int subscriber1PollingFrequency = 2;

        Subscriber subscriber2 = new Subscriber();
        final int subscriber2PollingFrequency = 1;

        monitor.subscribe(subscriber1, host, subscriber1PollingFrequency);
        monitor.subscribe(subscriber2, host, subscriber2PollingFrequency);

        final int graceTime = 4;
        monitor.setGraceTime(host, graceTime);

        // Start service for 9 seconds
        ServerSocket serviceSocket = createAndStartService(A_HOSTNAME, A_PORT);

        monitor.start();
        Thread.sleep(9000);

        // Stop service for 7 seconds
        serviceSocket.close();
        Thread.sleep(7000);

        // Start service for 4 seconds
        serviceSocket = createAndStartService(A_HOSTNAME, A_PORT);
        Thread.sleep(4000);

        // Start service for 1 second
        serviceSocket.close();
        Thread.sleep(1000);

        // Start service for 3 second
        serviceSocket = createAndStartService(A_HOSTNAME, A_PORT);
        Thread.sleep(3000);

        monitor.stopServer();
        serviceSocket.close();

        List<Subscriber> subscribers = Arrays.asList(subscriber1, subscriber2);
        List<ServiceState> resultStates = Arrays.asList(ServiceState.UP, ServiceState.DOWN, ServiceState.UP);

        subscribers.forEach(s -> {
            Assert.assertEquals(3, s.getNotificationList().size());
            Assert.assertTrue(
                    s.getNotificationList().stream().map(
                            n -> n.getServiceState()).collect(Collectors.toList()).equals(resultStates));
        });
    }

    @Test
    public void shouldConsiderZeroGraceTime() throws IOException, InterruptedException {
        Subscriber subscriber = new Subscriber();
        final int subscriberPollingFrequency = 2;

        Host host = new Host(A_HOSTNAME, A_PORT);

        monitor.subscribe(subscriber, host, subscriberPollingFrequency);

        // Start service for 3 seconds
        ServerSocket serviceSocket = createAndStartService(A_HOSTNAME, A_PORT);

        monitor.start();
        Thread.sleep(3000);

        // Stop service for 2 seconds
        serviceSocket.close();
        Thread.sleep(4000);

        // Start service for 3 seconds
        serviceSocket = new ServerSocket(9000, 10,  InetAddress.getByName("127.0.22.21"));
        Thread.sleep(3000);

        monitor.stopServer();
        serviceSocket.close();

        Assert.assertEquals(3, subscriber.getNotificationList().size());
        Assert.assertFalse(
                subscriber.getNotificationList().stream().map(
                        n -> n.getServiceState()).collect(Collectors.toList()).contains(ServiceState.MAY_BE_DOWN));
    }

    @Test
    public void shouldGetMinimumPollingFrequency() {
        Subscriber subscriber1 = new Subscriber();
        final int subscriber1APollingFrequency = 5;
        final int subscriber1BPollingFrequency = 5;

        Subscriber subscriber2 = new Subscriber();
        final int subscriber2APollingFrequency = 4;
        final int subscriber2BPollingFrequency = 0;

        Subscriber subscriber3 = new Subscriber();
        final int subscriber3APollingFrequency = 14;

        Host hostA = new Host(A_HOSTNAME, A_PORT);
        Host hostB = new Host(B_HOSTNAME, B_PORT);

        monitor.subscribe(subscriber1, hostA, subscriber1APollingFrequency);
        monitor.subscribe(subscriber2, hostA, subscriber2APollingFrequency);
        monitor.subscribe(subscriber3, hostA, subscriber3APollingFrequency);

        monitor.subscribe(subscriber1, hostB, subscriber1BPollingFrequency);
        monitor.subscribe(subscriber2, hostB, subscriber2BPollingFrequency);

        Map<Host, Service> config = (Map<Host, Service>) ReflectionTestUtils.getField(monitor,"config");
        Assert.assertEquals(4, config.get(hostA).getPollingFrequency());
        Assert.assertEquals(1, config.get(hostB).getPollingFrequency());
    }

    @Test
    public void shouldScheduleExtraChecks() {
        Subscriber subscriber = new Subscriber();
        final int subscriberAPollingFrequency = 5;
        final int graceTime = 2;

        Host host = new Host(A_HOSTNAME, A_PORT);

        monitor.subscribe(subscriber, host, subscriberAPollingFrequency);
        monitor.setGraceTime(host, graceTime);

        Map<Host, Service> config = (Map<Host, Service>) ReflectionTestUtils.getField(monitor,"config");
        Assert.assertEquals(subscriberAPollingFrequency, config.get(host).getPollingFrequency());

        Assert.assertEquals(graceTime,
                (int) ReflectionTestUtils.invokeGetterMethod(config.get(host), "getPollingInterval"));
    }

    @Test
    public void shouldConsiderOutage() throws InterruptedException, IOException {
        Subscriber subscriber = new Subscriber();
        final int subscriberPollingFrequency = 1;

        Host host = new Host(A_HOSTNAME, A_PORT);

        OutageTime outageTime = new OutageTime();
        outageTime.setStartOutageTime(LocalTime.now().minusMinutes(5));
        outageTime.setEndOutageTime(LocalTime.now().plusMinutes(5));

        monitor.subscribe(subscriber, host, subscriberPollingFrequency);
        monitor.setOutageTime(host, new OutageTime());

        // Start service for 10 seconds
        ServerSocket serviceSocket = createAndStartService(A_HOSTNAME, A_PORT);

        monitor.start();

        Thread.sleep(10000);
        monitor.stopServer();

        Assert.assertEquals(0,  subscriber.getNotificationList().size());
    }

    private ServerSocket createAndStartService(String hostName, int port) throws IOException {
        return new ServerSocket(port, 10, InetAddress.getByName(hostName));
    }
}