package cn.enigma.project.summary.test;

import java.time.Instant;
import java.time.LocalDateTime;

/**
 * @author luzh
 * Create: 2019-10-23 17:34
 * Modified By:
 * Description:
 */
public class Accumulate {

    public static void main(String[] args) {
        System.out.println(LocalDateTime.now().toString() + " start");
        long n = 11100L;
        long start = Instant.now().toEpochMilli();
        long result = accumulateV1(n);
        long end = Instant.now().toEpochMilli();
        System.out.println(result);
        System.out.println("v1 use time: " + (end - start));


        long n1 = 11100L;
        long start1 = Instant.now().toEpochMilli();
        long result1 = accumulateV2(n1);
        long end1 = Instant.now().toEpochMilli();
        System.out.println(result1);
        System.out.println("v2 use time: " + (end1 - start1));

        long n2 = 11100L;
        long start2 = Instant.now().toEpochMilli();
        long result2 = accumulateV3(n2);
        long end2 = Instant.now().toEpochMilli();
        System.out.println(result2);
        System.out.println("v3 use time: " + (end2 - start2));

    }

    private static long accumulateV1(long n) {
        long r = 0L;
        for (long i = 1L; i <= n; i++) {
            r = r + i;
        }
        return r;
    }

    private static long accumulateV2(long n) {
        return n == 1 ? 1 : n + accumulateV2(n - 1);
    }

    private static long accumulateV3(long n) {
        return accumulateV3Helper(1, n);
    }

    private static long accumulateV3Helper(long acc, long n) {
        return 1 == n ? acc : accumulateV3Helper(acc + n, n - 1);
    }
}
