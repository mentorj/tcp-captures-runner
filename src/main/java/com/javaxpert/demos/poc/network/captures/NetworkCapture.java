package com.javaxpert.demos.poc.network.captures;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
/**
 * This annotation placed on methods defines the method
 * as a capture scenario.  This scenario will be launched by the main class extracting by reflection
 * all
 */
public @interface NetworkCapture {
   String captureName() default "capture.pcap";
   int maxPackets() default 0;
}
