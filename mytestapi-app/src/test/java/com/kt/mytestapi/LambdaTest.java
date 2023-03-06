package com.kt.mytestapi;

import org.junit.jupiter.api.Test;

public class LambdaTest {
    // java8부터 함수형 프로그래밍 개념이 추가됨
    @Test
    public void runnable() {
        //1.Anonymous Inner Class
        Thread t1 = new Thread(new Runnable() {
            @Override
            public void run() {
                System.out.println("Anonymous Inner Class");
            }
        });
        t1.start();

        //2. Lambda Function
        Thread t2 = new Thread(() -> System.out.println("Lambda Expression"));
        t2.start();
    }

}
