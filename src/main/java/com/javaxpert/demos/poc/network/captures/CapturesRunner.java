package com.javaxpert.demos.poc.network.captures;

import com.google.common.eventbus.EventBus;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

public class CapturesRunner {
    public static void main(String[] args) throws ClassNotFoundException, IllegalAccessException, InstantiationException {
        // setup logger
        Logger logger =LoggerFactory.getLogger(CapturesRunner.class);

        // starts event bus
        //EventBus eventBus = EventBusFactory.getEventBus();
        //eventBus.register(new CaptureListener());
        //eventBus.register(new DeadEventsListener());
        final CaptureListener listener = new CaptureListener();
        logger.debug("eventbus started & listener registered");

        // get target class name
        // TODO : CHANGE ME - hardcoded
        Class c =Class.forName("com.javaxpert.demos.poc.network.captures.CapturesSuite");
        Object suite =  c.newInstance();
        Method[] methods =c.getDeclaredMethods();

        // find the BeforeCapture annotated method
        //  TODO refactor & improve
        Method beforeMethod = Arrays.stream(methods)
                .filter(m-> Arrays.stream(m.getDeclaredAnnotations())
                        .anyMatch(a -> a.annotationType().equals(BeforeCapture.class)))
                .findFirst().get();
        logger.debug("fetched the before method");

        // @TODO : to be tuned later
        ExecutorService service = Executors.newFixedThreadPool(4);
        // find the captures & invoke them
        Arrays.stream(methods)
                .filter(m ->
                        Arrays.stream(m.getDeclaredAnnotations())
                                .anyMatch(a -> a.annotationType().equals(NetworkCapture.class)))

                .forEach(m -> {
                    logger.info("Handling method - "+ m.getName());
                    try {
                        Annotation annotation = m.getDeclaredAnnotation(NetworkCapture.class);
                        Class <? extends  Annotation> clazz = annotation.annotationType();
                        Method capture_name_method = Arrays.stream(clazz.getDeclaredMethods())
                                .filter(m1->m1.getName().equals("captureName"))
                                .findFirst().get();
                        Method capture_itf_method = Arrays.stream(clazz.getDeclaredMethods())
                                .filter(m2->m2.getName().equals("captureItf"))
                                .findFirst().get();

                        logger.info("invoking before method");
                        beforeMethod.invoke(suite,null);
                        String name =(String)capture_name_method.invoke(annotation,null);
                        String itf =(String)capture_itf_method.invoke(annotation,null);
                        logger.debug("fetched capture name ="+ name + " from annotation");

                        Future<String> postEventFuture = service.submit(() -> {
                            listener.captureStarted(new CaptureStartedEvent(name,itf));
                            return "ok";});
                        //eventBus.post(new CaptureStartedEvent(name,itf));
                        while(!postEventFuture.isDone()){
                            logger.debug("posting event CaptureStarted" );
                            TimeUnit.MILLISECONDS.sleep(500);
                        }
                        postEventFuture.get();
                        postEventFuture.cancel(true);
                        logger.info("before invoking capture");
                        Thread.currentThread().sleep(2000);
                        Future<String> invokeScenarioFuture = service.submit( ()->  {m.invoke(suite, null);return "ok";});
                        while(!invokeScenarioFuture.isDone()){
                            TimeUnit.SECONDS.sleep(4);
                            logger.debug("waiting for current scenario to finish");
                        }

                        logger.debug("Scenario finished ....Capture invoked");
                        //TimeUnit.SECONDS.sleep(2);
                        postEventFuture = service.submit(() -> {listener.captureStopped(new CaptureStoppedEvent());
                            return "ok";});
                        //eventBus.post(new CaptureStartedEvent(name,itf));
                        while(!postEventFuture.isDone()){
                            logger.debug("posting event CaptureSTopped");
                            TimeUnit.MILLISECONDS.sleep(500);
                        }
                        postEventFuture.get();
                        postEventFuture.cancel(true);
                        //eventBus.post(new CaptureStoppedEvent());

                        logger.info("Handled this annotatiion...Next one ?");
                    } catch (Throwable t){
                        t.printStackTrace();
                        logger.error(t.getMessage());
                    }
                });
        ;
        logger.info("Runner finished exiting");
        System.exit(0);
    }
}
