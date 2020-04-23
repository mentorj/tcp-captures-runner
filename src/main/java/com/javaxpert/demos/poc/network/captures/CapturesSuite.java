package com.javaxpert.demos.poc.network.captures;

import org.pcap4j.core.PcapHandle;
import org.pcap4j.core.PcapNetworkInterface;

public class CapturesSuite {
    private PcapNetworkInterface defaultInterface;
    private PcapHandle handle;
    public CapturesSuite(){
        System.out.println("building object");
    }
    @NetworkCapture(captureName = "test.pcap")
    public void basicCapture(){
        System.out.print("basic capture launched");
    }

    @BeforeCapture
    public void setupCapture(){
        System.out.println("this is the before capture method");

    }

    public void useless(){
        System.out.println("not very usefull...");
    }
}
