package com.javaxpert.demos.poc.network.captures;

import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Optional;

public class CapturesRunner {
    public static void main(String[] args) throws ClassNotFoundException, IllegalAccessException, InstantiationException {
        Logger logger =LoggerFactory.getLogger(CapturesRunner.class);
        Class c =Class.forName(args[0]);
        Object suite =  c.newInstance();
        Method[] methods =c.getDeclaredMethods();
        Config defaultConfig = ConfigFactory.parseResources("defaults.conf");
        defaultConfig.resolve();
        logger.debug(defaultConfig.root().render());

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
                        System.out.println("invoking before method");
                        beforeMethod.invoke(suite,null);
                        System.out.println("before invoking capture");
                        m.invoke(suite, null);
                    } catch (Throwable t){
                        System.out.println(t.getMessage());
                    }
                });
        ;
    }
}
