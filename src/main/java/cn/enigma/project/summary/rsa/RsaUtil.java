package cn.enigma.project.summary.rsa;

import cn.enigma.project.summary.rsa.pojo.EncryptValue;
import cn.enigma.project.summary.rsa.pojo.PublicKey;
import cn.hutool.core.codec.Base64;
import cn.hutool.core.util.IdUtil;
import cn.hutool.crypto.SecureUtil;
import cn.hutool.crypto.asymmetric.KeyType;
import cn.hutool.crypto.asymmetric.RSA;

import java.security.KeyPair;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * @author luzh
 * Create: 2019-07-12 11:10
 * Modified By:
 * Description: 生成一个密钥池，每个密钥对可以使用三次
 */
public class RsaUtil {

    /**
     * 一个密钥对最大使用次数
     */
    private static final int MAX_USE_TIME = 3;

    /**
     * 密钥池中密钥对最少个数
     */
    private static final int MIN_KEYPAIR_NUMBER = 5;

    /**
     * 密钥有效时间（单位ms）
     */
    private static final long KEYPAIR_AVAILABLE_TIME = 30 * 60 * 1000L;

    private static final int DEFAULT_SIZE = 100;

    /**
     * 将密钥对保存到map中，具有唯一id
     */
    private static volatile ConcurrentHashMap<String, KeyPair> keyPairPool = new ConcurrentHashMap<>(DEFAULT_SIZE);

    /**
     * 密钥对的使用次数，getPublicKey方法调用后会将密钥对使用次数+1
     */
    private static volatile ConcurrentHashMap<String, Integer> keyPairGetTimes = new ConcurrentHashMap<>(DEFAULT_SIZE);

    /**
     * 解密时根据id查找到密钥对，解密一次使用次数+1
     */
    private static volatile ConcurrentHashMap<String, Integer> keyPairUseTimes = new ConcurrentHashMap<>(DEFAULT_SIZE);

    /**
     * 密钥对最后获取时间
     */
    private static volatile ConcurrentHashMap<String, Long> keyPairLastGetTime = new ConcurrentHashMap<>(DEFAULT_SIZE);

    /**
     * 密钥对最后使用时间
     */
    private static volatile ConcurrentHashMap<String, Long> keyPairLastUseTime = new ConcurrentHashMap<>(DEFAULT_SIZE);

    /**
     * 所有密钥对对应的id列表
     */
    private static volatile List<String> idList = new ArrayList<>(DEFAULT_SIZE);

    /**
     * 每个密钥对的锁
     */
    private static volatile Map<String, Lock> idLockMap = new HashMap<>(DEFAULT_SIZE);

    /**
     * 初始化密钥对
     */
    private static void initKeyPair() {
        for (int i = 0; i < MIN_KEYPAIR_NUMBER; i++) {
            generatePublicKey();
        }
    }

    /**
     * 生成一个新的密钥对
     */
    private static void generatePublicKey() {
        String id = getId();
        generatePublicKey(id);
    }

    /**
     * 生成id
     *
     * @return id
     */
    private static String getId() {
        return IdUtil.getSnowflake(1, 1).nextId() + "";
    }

    /**
     * 生成一个新的密钥对，并保存到对应的map list中，返回公钥和对应的id
     *
     * @param id id
     * @return 公钥、id
     */
    private static java.security.PublicKey generatePublicKey(String id) {
        KeyPair keyPair = SecureUtil.generateKeyPair("RSA");
        keyPairPool.put(id, keyPair);
        idList.add(id);
        idLockMap.put(id, new ReentrantLock());
        return keyPair.getPublic();
    }

    /**
     * 生成新的密钥对，返回公钥
     *
     * @return 公钥
     */
    private static PublicKey getNewPublicKey() {
        String id = getId();
        java.security.PublicKey publicKey = generatePublicKey(id);
        keyPairGetTimes.put(id, 1);
        keyPairLastGetTime.put(id, Instant.now().toEpochMilli());
        return new PublicKey(id, publicKey);
    }

    /**
     * 清除失效的id，重新生成一个密钥对
     *
     * @param id 失效的id
     * @return 公钥
     */
    private static PublicKey getPublicKeyAndClearInvalidKey(String id) {
        clearInvalidKeyPair(id);
        return getNewPublicKey();
    }

    /**
     * 清除失效的密钥对
     *
     * @param id 密钥对id
     */
    private static void clearInvalidKeyPair(String id) {
        keyPairPool.remove(id);
        idList.remove(id);
        idLockMap.remove(id);
        keyPairUseTimes.remove(id);
        keyPairGetTimes.remove(id);
        keyPairLastGetTime.remove(id);
        keyPairLastUseTime.remove(id);
    }

    /**
     * 从密钥对池中随机获取公钥
     *
     * @return 公钥和id
     */
    public static PublicKey getPublicKey() {
        if (idList.isEmpty()) {
            initKeyPair();
            return getPublicKey();
        } else {
            if (keyPairPool.size() < MIN_KEYPAIR_NUMBER) {
                initKeyPair();
            }
            // 先随机从id列表中获取一个id，然后取出密钥对的锁。
            int index = ThreadLocalRandom.current().nextInt(0, idList.size());
            String id = idList.get(index);
            Lock lock = idLockMap.getOrDefault(id, null);
            if (null == lock) {
                return getPublicKeyAndClearInvalidKey(id);
            }
            return getPublicKey(id, lock);
        }
    }

    /**
     * 操作读取密钥对，并更新密钥对的获取次数。更新时加锁处理。
     *
     * @param id   密钥id
     * @param lock 密钥锁
     * @return 公钥
     */
    private static PublicKey getPublicKey(String id, Lock lock) {
        try {
            lock.lock();
            KeyPair keyPair = keyPairPool.getOrDefault(id, null);
            if (null == keyPair) {
                return getPublicKeyAndClearInvalidKey(id);
            }
            int useTime = keyPairGetTimes.getOrDefault(id, 0);
            if (useTime >= MAX_USE_TIME) {
                return getNewPublicKey();
            }
            keyPairGetTimes.put(id, useTime + 1);
            keyPairLastGetTime.put(id, Instant.now().toEpochMilli());
            return new PublicKey(id, keyPair.getPublic());
        } finally {
            lock.unlock();
        }
    }

    private static EncryptValue encryptBO(PublicKey bo) {
        String id = bo.getId();
        String value = id + "-" + Instant.now().toEpochMilli();
        return new EncryptValue(id, encrypt(value, id));
    }

    private static String encrypt(String value, String id) {
        KeyPair keyPair = keyPairPool.getOrDefault(id, null);
        if (null == keyPair) {
            throw new RuntimeException("加密错误");
        }
        RSA rsa = new RSA(keyPair.getPrivate(), keyPair.getPublic());
        byte[] encrypt = rsa.encrypt(value, KeyType.PublicKey);
        return Base64.encode(encrypt);
    }

    private static String decrypt(EncryptValue bo) {
        return decrypt(bo.getId(), bo.getValue());
    }

    /**
     * 解密
     *
     * @param id    keypair id
     * @param value 要解密的内容
     * @return 解密后的字符串
     */
    public static String decrypt(String id, String value) {
        KeyPair keyPair = keyPairPool.getOrDefault(id, null);
        if (null == keyPair) {
            throw new RuntimeException("密钥失效");
        }
        Lock lock = idLockMap.getOrDefault(id, null);
        if (null == lock) {
            throw new RuntimeException("获取锁错误");
        }
        RSA rsa = new RSA(keyPair.getPrivate(), keyPair.getPublic());
        byte[] decrypt = rsa.decrypt(Base64.decode(value.getBytes()), KeyType.PrivateKey);
        // 解密之后更新id的使用次数
        increaseIdUseTime(id, lock);
        return new String(decrypt);
    }

    private static void increaseIdUseTime(String id, Lock lock) {
        try {
            lock.lock();
            int idUseTimes = keyPairUseTimes.getOrDefault(id, 0) + 1;
            keyPairUseTimes.put(id, idUseTimes);
            if (idUseTimes >= MAX_USE_TIME) {
                System.out.println(id + "使用次数" + idUseTimes + "，从系统中移除 " + Instant.now().toEpochMilli());
                clearInvalidKeyPair(id);
                return;
            }
            // 这里逻辑不是很严谨，有待加强
            int idGetTimes = keyPairGetTimes.getOrDefault(id, 0);
            if (idGetTimes >= MAX_USE_TIME) {
                Long lastGetTime = keyPairLastGetTime.getOrDefault(id, Instant.now().toEpochMilli());
                if (Instant.now().toEpochMilli() - lastGetTime > KEYPAIR_AVAILABLE_TIME) {
                    clearInvalidKeyPair(id);
                    return;
                }
                Long lastUseTime = keyPairLastUseTime.getOrDefault(id, Instant.now().toEpochMilli());
                if (Instant.now().toEpochMilli() - lastUseTime > KEYPAIR_AVAILABLE_TIME) {
                    clearInvalidKeyPair(id);
                    return;
                }
            }
            keyPairLastUseTime.put(id, Instant.now().toEpochMilli());
        } finally {
            lock.unlock();
        }
    }

    public static void main(String[] args) {
        initKeyPair();
        Long start = Instant.now().toEpochMilli();
        System.out.println(start);
        List<CompletableFuture<String>> list = new ArrayList<>(100);
        int times1 = 30;
        CountDownLatch countDownLatch = new CountDownLatch(times1);
        for (int i = 0; i < times1; i++) {
            CompletableFuture<String> future = CompletableFuture
                    .supplyAsync(RsaUtil::getPublicKey)
                    .thenApplyAsync(RsaUtil::encryptBO)
                    .thenApplyAsync(RsaUtil::decrypt);
            list.add(future);
        }
        System.out.println(Instant.now().toEpochMilli());
        System.out.println(Instant.now().toEpochMilli() - start);
        list.parallelStream().forEach(t -> {
            Thread thread = new Thread(() -> {
                t.join();
                countDownLatch.countDown();
            });
            thread.start();
        });
        System.out.println("开启" + times1 + "个线程");
        System.out.println("总共耗时" + (Instant.now().toEpochMilli() - start) + "ms");
        try {
            countDownLatch.await();
            System.out.println("全部执行完毕");
            System.out.println("总共耗时" + (Instant.now().toEpochMilli() - start) + "ms");
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        System.out.println("id列表数量" + idList.size());
        System.out.println("密钥对数量" + keyPairPool.size());
        System.out.println("id数量" + keyPairPool.size());
        System.out.println("id使用次数" + keyPairUseTimes);
        System.out.println("密钥对使用次数" + keyPairGetTimes);
        System.out.println("id列表" + idList);
    }
}
