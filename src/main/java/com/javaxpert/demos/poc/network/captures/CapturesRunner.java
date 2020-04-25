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

public class CapturesRunner {
    public static void main(String[] args) throws ClassNotFoundException, IllegalAccessException, InstantiationException {
        // setup logger
        Logger logger =LoggerFactory.getLogger(CapturesRunner.class);

        // starts event bus
        EventBus eventBus = EventBusFactory.getEventBus();
        eventBus.register(new CaptureListener());

        logger.debug("eventbus started & listener registered");

        // get target class name
        Class c =Class.forName("com.javaxpert.demos.poc.network.captures.CapturesSuite");
        Object suite =  c.newInstance();
        Method[] methods =c.getDeclaredMethods();

        // find the BeforeCapture annotated method
        //  TODO refactor & improve
        Method beforeMethod = Arrays.stream(methods)
                .filter(m-> Arrays.stream(m.getDeclaredAnnotations())
                        .anyMatch(a -> a.annotationType().equals(BeforeCapture.class)))
                .findFirst().get();


        // find the captures & invoke them
        Arrays.stream(methods)
                .filter(m ->
                        Arrays.stream(m.getDeclaredAnnotations())
                                .anyMatch(a -> a.annotationType().equals(NetworkCapture.class)))

                .forEach(m -> {
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
                        eventBus.post(new CaptureStartedEvent(name,itf));
                        logger.info("before invoking capture");
                        Thread.currentThread().sleep(2000);
                        m.invoke(suite, null);
                        logger.debug("Capture invoked");
                        Thread.currentThread().sleep(2000);
                        eventBus.post(new CaptureStoppedEvent());
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
