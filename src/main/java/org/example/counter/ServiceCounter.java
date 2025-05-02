package org.example.counter;

import org.springframework.stereotype.Component;

@Component
public class ServiceCounter {

    private static int count;

    private ServiceCounter() {}

    public static synchronized void increment() {
        count++;
    }

    public static synchronized int getCount() {
        return count;
    }
}
