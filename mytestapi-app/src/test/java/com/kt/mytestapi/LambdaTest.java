package com.kt.mytestapi;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.function.Consumer;

public class LambdaTest {
    // java8부터 함수형 프로그래밍 개념이 추가됨
    @Test
    public void consumer() {
        List<String> list = List.of("aa", "bb", "cc");//Immutable List
        //1.Abstract
        list.forEach(new Consumer<String>() {
            @Override
            public void accept(String s) {
                System.out.println("s = "+ s);
            }
        });

        //2.lambda
        list.forEach(val -> System.out.println(val));

        //3.Method Reference
        list.forEach(System.out::println);
    }

    @Test @Disabled
    public void runnable() {
        //1. Anonymous Inner class
        Thread t1 = new Thread(new Runnable() {
            @Override
            public void run() {
                System.out.println("Anonymous Inner class");
            }
        });
        t1.start();
        //2. Lambda Expression
        Thread t2 = new Thread(() -> System.out.println("Lambda Expression"));
        t2.start();
    }

}
