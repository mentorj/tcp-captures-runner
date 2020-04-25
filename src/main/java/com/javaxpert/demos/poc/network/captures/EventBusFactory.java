package com.javaxpert.demos.poc.network.captures;

import com.google.common.eventbus.AsyncEventBus;
import com.google.common.eventbus.EventBus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class EventBusFactory {

    private static Logger logger = LoggerFactory.getLogger(EventBusFactory.class);
    //hold the instance of the event bus here
    private static EventBus eventBus = new AsyncEventBus(Executors.newFixedThreadPool(4));

    public static EventBus getEventBus() {
        logger.info("called getEventBus() " + eventBus.identifier());
        return eventBus;
    }

}
