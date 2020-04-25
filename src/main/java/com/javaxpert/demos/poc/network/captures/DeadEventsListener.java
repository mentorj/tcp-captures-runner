package com.javaxpert.demos.poc.network.captures;

import com.google.common.eventbus.DeadEvent;
import com.google.common.eventbus.Subscribe;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DeadEventsListener {
    private static Logger logger = LoggerFactory.getLogger(DeadEventsListener.class);
    @Subscribe
    public void handleDeadEvent(DeadEvent  evt){
        logger.info("Dead Event received :" + evt.getEvent() + " Source = " + evt.getSource());
    }
}
