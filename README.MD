# TCP Packets Runner

This projet aims to provide a JUnit  like solution to capture
packets defined inside small methods annotated with @NetworkCapture.
TCP capture handled by PCap4J library (may requires root privileges escalation).

Tested under GNU Linux Debian Buster.

## Dependencies
As usual in Java dependencies are managed through a Maven POM file
+ PCap4J
+ RabbitMQ Java client
+ Typesafe Config for configuration management

## Running
mvn clean & 
mvn compile &
sudo mvn exec:java -Dexec.mainClass="com.javaxpert.demos.poc.network.captures.CapturesRunner" -Dexec.classpathScope=runtime

## Outputs
The program should outputs different files (conforming to old pcap format)
one per scenario as defined in the class.

## Current status
Alpha stage , works basically with my tests:
+ needs a huge refactoring
+ needs some hardcoded parameters to be externalized
+ needs a few extensions to be more generic
