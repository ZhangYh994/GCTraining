package com.zyh.Training3;

import org.junit.jupiter.api.Test;
import java.util.Random;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicBoolean;

public class GoodManagerConcurrentTest {
    @Test
    public void testConcurrentOperations() throws InterruptedException {
        final GoodManager manager = new GoodManager();
        final int threadCount = 100;
        final long endTime = System.currentTimeMillis() + 5 * 60 * 1000; // 5分钟
        final CountDownLatch latch = new CountDownLatch(threadCount);
        final AtomicBoolean errorOccurred = new AtomicBoolean(false);
        Random random = new Random();

        Runnable task = () -> {
            try {
                while (System.currentTimeMillis() < endTime && !errorOccurred.get()) {
                    int op = random.nextInt(4);
                    try {
                        switch (op) {
                            case 0: // 添加
                                Goods newGoods = new Goods("商品" + random.nextInt(10000));
                                manager.addGoods(newGoods);
                                System.out.println(Thread.currentThread().getName() + " 添加: " + newGoods);
                                break;
                            case 1: // 删除
                                int size = manager.size();
                                if (size > 0) {
                                    int removeIndex = random.nextInt(size);
                                    try {
                                        manager.removeGoods(removeIndex);
                                        System.out.println(Thread.currentThread().getName() + " 删除索引: " + removeIndex);
                                    } catch (IndexOutOfBoundsException e) {
                                        System.out.println(Thread.currentThread().getName() + " 删除时捕获异常: " + e.getMessage());
                                    }
                                }
                                break;
                            case 2: // 查看
                                manager.getGoodsList();
                                System.out.println(Thread.currentThread().getName() + " 查看商品列表");
                                break;
                            case 3: // 清空
                                manager.clearGoods();
                                System.out.println(Thread.currentThread().getName() + " 清空商品列表");
                                break;
                        }
                    } catch (Exception e) {
                        errorOccurred.set(true);
                        e.printStackTrace();
                    }
                }
            } finally {
                latch.countDown();
            }
        };

        for (int i = 0; i < threadCount; i++) {
            new Thread(task).start();
        }
        latch.await();

        assert !errorOccurred.get() : "并发操作过程中出现异常！";
        assert manager.size() >= 0 : "商品数量不应为负数";
    }
}
