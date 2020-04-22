package com.javaxpert.demos.poc.network.captures;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.Arrays;

public class CapturesRunner {
    public static void main(String[] args) throws ClassNotFoundException, IllegalAccessException, InstantiationException {
        Class c =Class.forName(args[0]);
        Object suite =  c.newInstance();
        Method[] methods =c.getDeclaredMethods();

        Arrays.stream(methods)
                .filter(m ->
                        Arrays.stream(m.getDeclaredAnnotations())
                                .anyMatch(a -> a.annotationType().equals(NetworkCapture.class)))

                .forEach(m -> {
                    try {
                        System.out.println("before invoking capture");
                        m.invoke(suite, null);
                    } catch (Throwable t){
                        System.out.println(t.getMessage());
                    }
                });
        ;
    }
}
