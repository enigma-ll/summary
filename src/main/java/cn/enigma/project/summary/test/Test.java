package cn.enigma.project.summary.test;

import java.time.LocalDateTime;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ThreadLocalRandom;

public class Test {

    public static void main(String[] args) throws ExecutionException, InterruptedException {
        System.out.println("1:" + LocalDateTime.now().toString());
        CompletableFuture<String> completableFuture = CompletableFuture.supplyAsync(() -> {
            System.out.println("start:" + LocalDateTime.now().toString());
            try {
                Thread.sleep(ThreadLocalRandom.current().nextInt(100, 1000));
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            return LocalDateTime.now().toString();
        });
        completableFuture.complete("123");
        System.out.println(completableFuture.get());
    }

}
