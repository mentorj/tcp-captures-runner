package com.javaxpert.demos.poc.network.captures;

import com.rabbitmq.client.*;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeoutException;

public class CapturesSuite {
    private static Logger logger = LoggerFactory.getLogger(CapturesSuite.class);
    private ConnectionFactory cf = null;
    private DeliverCallback deliverCallback;
    public CapturesSuite(){
        logger.info("building suite of captures");
        // setup config
        Config defaultConfig = ConfigFactory.parseResources("defaults.conf");
        defaultConfig.resolve();
        //logger.trace(defaultConfig.root().render());

        cf = new ConnectionFactory();
//        cf.setAutomaticRecoveryEnabled(true);
        cf.setConnectionTimeout(defaultConfig.getInt("timeout"));
        cf.setHandshakeTimeout(defaultConfig.getInt("handshake.timeout"));
        cf.setHeartbeatExecutor(Executors.newSingleThreadScheduledExecutor());
//        cf.setExceptionHandler(new ExceptionHandler() {
//            @Override
//            public void handleUnexpectedConnectionDriverException(Connection connection, Throwable throwable) {
//                logger.info("Error occured with  rabbit connection :" + throwable.getMessage() + ". Connection id =" + connection.getId() + " Name = " + connection.getClientProvidedName());
//            }
//
//            @Override
//            public void handleReturnListenerException(Channel channel, Throwable throwable) {
//                logger.info("Error occured with  rabbit connection :" + throwable.getMessage() + ". Connection id =" + channel.getConnection().getId() + " Name = " + channel.getConnection().getClientProvidedName());
//            }
//
//            @Override
//            public void handleConfirmListenerException(Channel channel, Throwable throwable) {
//                logger.info("Error occured with  rabbit connection :" + throwable.getMessage() + ". Connection id =" + channel.getConnection().getId() + " Name = " + channel.getConnection().getClientProvidedName());
//            }
//
//            @Override
//            public void handleBlockedListenerException(Connection connection, Throwable throwable) {
//                logger.info("Error occured with  rabbit connection :" + throwable.getMessage() + ". Connection id =" + connection.getId() + " Name = " + connection.getClientProvidedName());
//            }
//
//            @Override
//            public void handleConsumerException(Channel channel, Throwable throwable, Consumer consumer, String s, String s1) {
//                logger.info("Error occured with  rabbit connection :" + throwable.getMessage() + ". Connection id =" + channel.getConnection().getId() + " Name = " + channel.getConnection().getClientProvidedName());
//            }
//
//            @Override
//            public void handleConnectionRecoveryException(Connection connection, Throwable throwable) {
//                logger.info("Error occured with  rabbit connection  recovery :" + throwable.getMessage() + ". Connection id =" + connection.getId() + " Name = " + connection.getClientProvidedName());
//            }
//
//            @Override
//            public void handleChannelRecoveryException(Channel channel, Throwable throwable) {
//                logger.info("ChannelRecoveryException occured " + channel.getChannelNumber() + "/" + channel.getConnection().toString());
//            }
//
//            @Override
//            public void handleTopologyRecoveryException(Connection connection, Channel channel, TopologyRecoveryException e) {
//
//            }
//        });
        cf.setRequestedHeartbeat(defaultConfig.getInt("heartbeat.timeout"));
        deliverCallback = (consumerTag, delivery) -> {
            String message = new String(delivery.getBody(), "UTF-8");
            logger.info(" [x] Received '" + message + "'");
        };

    }
    @NetworkCapture(captureName = "test.pcap",captureItf = "docker0")
    public void basicCapture(){
        logger.info("basic capture launched");
        try {
            // adds a  tempo to let packets capture initialize
            Thread.currentThread().sleep(500);
            logger.debug("starting capture after a sleep");
            Connection conn = cf.newConnection();
            Channel channel = conn.createChannel();
            channel.queueDeclare("testQueue",false,false,false,null);
            channel.exchangeDeclare("testExchange","direct");
            channel.queueBind("testQueue","testExchange","test");
            logger.debug("queue & channel declared");
            channel.basicPublish("testExchange","test",null,"hello world".getBytes());
            channel.basicConsume("testQueue",deliverCallback, consumerTag -> { });
//            GetResponse response = channel.basicGet("testQueue",true);
//            logger.info("got message = " + (response==null?"null":new String(response.getBody())));
            Thread.currentThread().sleep(500);
            channel.close();
            conn.close();
        } catch (IOException e) {
            logger.error(e.getMessage());
        } catch (TimeoutException e) {
            logger.error(e.getMessage());
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

    }

    @BeforeCapture
    public void setupCapture(){
        logger.debug("this is the before capture method");

    }
}
