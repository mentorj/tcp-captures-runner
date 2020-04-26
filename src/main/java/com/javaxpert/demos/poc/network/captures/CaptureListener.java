package com.javaxpert.demos.poc.network.captures;

import org.pcap4j.core.*;
import org.pcap4j.core.PcapNetworkInterface.PromiscuousMode;
import org.pcap4j.packet.Packet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

public class CaptureListener {
    private static Logger logger = LoggerFactory.getLogger(CaptureListener.class);
    private PcapHandle handle = null;
    private PcapDumper dumper = null;
    private ExecutorService service = Executors.newFixedThreadPool(2);
    private volatile Future currentTask;

    public void captureStarted(CaptureStartedEvent evt) {
        logger.debug("received a new CaptureStartedEvent from thread =" + Thread.currentThread().getId());
        logger.debug("starting capture" + evt.getCaptureInterface() + " Name = " + evt.getCaptureName());
//        if(handle!=null && handle.isOpen()){
//            logger.debug("handle is not null");
//            try {
//                handle.
//                handle.breakLoop();
//                dumper.flush();
//            } catch (NotOpenException | PcapNativeException e) {
//                logger.error(e.getMessage());
//            }
//
//        }

        final String capture_itf = evt.getCaptureInterface();
        final String capture_name = evt.getCaptureName();

        currentTask = service.submit(() -> {
            try {
                logger.debug("Executing packets capture inside thread = " + Thread.currentThread().getId());
                PcapNetworkInterface network_interface = Pcaps.getDevByName(capture_itf);
                PcapNetworkInterface.PromiscuousMode mode;

                handle = network_interface.openLive(2048, PromiscuousMode.PROMISCUOUS, 40);
                dumper = handle.dumpOpen(capture_name);
                // @TODO : drop hardcoded filter
                handle.setFilter("tcp port 5672", BpfProgram.BpfCompileMode.OPTIMIZE);
                // Create a listener that defines what to do with the received packets
                PacketListener listener = new PacketListener() {
                    @Override
                    public void gotPacket(Packet packet) {
                        // Dump packets to file
                        try {
                            //logger.debug("got packet :" + packet.toString());
                            dumper.dump(packet, handle.getTimestamp());
                        } catch (NotOpenException e) {
                            logger.error(e.getMessage());
                        }
                    }
                };

                // Tell the handle to loop using the listener we created
                try {
                    int maxPackets = 512;
                    handle.loop(maxPackets, listener);

                } catch (InterruptedException e) {
                    logger.error("Error received :" + e.getMessage());
                }
            } catch (PcapNativeException | NotOpenException e) {
                e.printStackTrace();
                e.getCause().printStackTrace();
                logger.error("Encountered error...." + e.getMessage() + " " + e.getCause());
            }
        });

    }

    public void captureStopped(CaptureStoppedEvent evt) {
        logger.info("StoppedEvent received from thread = " + Thread.currentThread().getId());
        service.submit(() -> {
            logger.debug("Handling stopped event from Eexecutor Thread =" + Thread.currentThread().getId());
            if (dumper != null && dumper.isOpen()) {
                try {
                    handle.breakLoop();
                    dumper.flush();

                    dumper.close();
                    logger.debug("dumper closed");
                } catch (PcapNativeException | NotOpenException e) {
                    logger.error(e.toString());
                }
            }
            logger.debug("stopping capture packets thread, isFinished? =" + currentTask.isDone());
            currentTask.cancel(true);
            while (!currentTask.isDone()) {
                try {
                    TimeUnit.MILLISECONDS.sleep(100);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                logger.debug("waiting for packets capture thread to finish");
            }
            logger.info("Handled captureStopped event");
        });

    }
}
