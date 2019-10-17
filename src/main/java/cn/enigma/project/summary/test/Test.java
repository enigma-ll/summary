package cn.enigma.project.summary.test;

import java.util.concurrent.ConcurrentHashMap;

public class Test {

    public static void main(String[] args) {
        ConcurrentHashMap<String, String> map = new ConcurrentHashMap<>();
        System.out.println(map.putIfAbsent("1", "1"));
        System.out.println(map.putIfAbsent("1", "1"));
    }
}
