package com.javaxpert.demos.poc.network.captures;

public class CapturesSuite {
    public CapturesSuite(){
        System.out.println("building object");
    }
    @NetworkCapture(captureName = "test.pcap")
    public void basicCapture(){
        System.out.print("basic capture launched");
    }

    public void useless(){
        System.out.println("not very usefull...");
    }
}
