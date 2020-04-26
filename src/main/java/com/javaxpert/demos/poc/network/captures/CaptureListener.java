package com.javaxpert.demos.poc.network.captures;

import com.google.common.eventbus.Subscribe;
import org.pcap4j.core.*;
import org.pcap4j.core.PcapNetworkInterface.PromiscuousMode;
import org.pcap4j.packet.Packet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CaptureListener {
    private static Logger logger = LoggerFactory.getLogger(CaptureListener.class);
    private PcapHandle handle = null;
    private PcapDumper dumper = null;
    @Subscribe
    public void captureStarted(CaptureStartedEvent evt){
        logger.debug("received a new CaptureStartedEvent from thread =" + Thread.currentThread().getId());
        logger.debug("starting capture" + evt.getCaptureInterface() + " Name = " + evt.getCaptureName());
        String capture_itf = evt.getCaptureInterface();
        String capture_name = evt.getCaptureName();
        try {
            PcapNetworkInterface network_interface = Pcaps.getDevByName(capture_itf);
            PcapNetworkInterface.PromiscuousMode mode;
            handle = network_interface.openLive(2048,PromiscuousMode.PROMISCUOUS,40 );
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
            logger.error("Encountered error...."+ e.getMessage() + " " + e.getCause());
        }
    }

    @Subscribe
    public void captureStopped(CaptureStoppedEvent evt){
        logger.info("StoppedEvent received from thread = " + Thread.currentThread().getId());
        if(handle!=null && handle.isOpen() ){
            handle.close();
            logger.trace("handle closed");
        }
        if(dumper!=null && dumper.isOpen()){
            dumper.close();
            logger.trace("dumper closed");
        }
        logger.info("Handle captureStopped event");
    }
}
