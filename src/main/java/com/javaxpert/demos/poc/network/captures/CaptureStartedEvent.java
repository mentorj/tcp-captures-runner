package com.javaxpert.demos.poc.network.captures;

public class CaptureStartedEvent {
    private String captureName;
    private String captureInterface;

    public String getCaptureName() {
        return captureName;
    }

    public String getCaptureInterface() {
        return captureInterface;
    }

    public CaptureStartedEvent(String name, String itf){
        captureName=name;
        captureInterface=itf;
    }
}
