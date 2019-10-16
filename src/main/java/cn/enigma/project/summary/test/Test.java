package cn.enigma.project.summary.test;

import java.time.LocalDateTime;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;

public class Test {

    public static void main(String[] args) throws InterruptedException {
        ConcurrentHashMap<String, String> map = new ConcurrentHashMap<>();
        System.out.println(map.putIfAbsent("1", "1"));
        System.out.println(map.putIfAbsent("1", "1"));
    }
}
