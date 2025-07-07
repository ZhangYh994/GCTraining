package com.zyh.Training3;

import com.zyh.Training2.IListImpl;
import org.junit.jupiter.api.Test;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Random;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicBoolean;

public class GoodManagerConcurrentSequentialTest {
    private static final SimpleDateFormat TIMESTAMP_FORMAT = new SimpleDateFormat("HH:mm:ss.SSS");
    // 全局唯一的日志锁对象
    private static final Object LOG_LOCK = new Object();

    @Test
    public void testConcurrentOperations() throws InterruptedException {
        final GoodManager manager = new GoodManager();
        final int threadCount = 100;
        final long endTime = System.currentTimeMillis() + 5 * 60 * 1000; // 5分钟
        final CountDownLatch latch = new CountDownLatch(threadCount);
        final AtomicBoolean errorOccurred = new AtomicBoolean(false);
        Random random = new Random();

        // 定期打印状态的线程（也使用全局锁）
        Thread statusThread = new Thread(() -> {
            while (System.currentTimeMillis() < endTime && !errorOccurred.get()) {
                try {
                    Thread.sleep(3000);
                    synchronized (LOG_LOCK) {
                        printWithTimestamp("===== 当前商品列表状态 =====");
                        printWithTimestamp("商品数量: " + manager.size());
                        IListImpl<Goods> currentList = manager.getGoodsList();
                        for (int i = 0; i < currentList.size(); i++) {
                            printWithTimestamp("  " + i + ": " + currentList.get(i));
                        }
                        printWithTimestamp("==========================");
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
        });
        statusThread.start();

        Runnable task = () -> {
            try {
                while (System.currentTimeMillis() < endTime && !errorOccurred.get()) {
                    int op = random.nextInt(4);
                    try {
                        synchronized (LOG_LOCK) { // 所有操作和日志共用同一把锁
                            switch (op) {
                                case 0: // 添加
                                    Goods newGood = new Goods("商品" + random.nextInt(10000));
                                    manager.addGoods(newGood);
                                    printWithTimestamp(Thread.currentThread().getName() + " 添加商品: " + newGood.getName());
                                    printListState(manager);
                                    break;
                                case 1: // 删除
                                    int size = manager.size();
                                    if (size > 0) {
                                        int index = random.nextInt(size);
                                        try {
                                            Goods removedGood = manager.removeGoods(index);
                                            printWithTimestamp(Thread.currentThread().getName() + " 删除成功: " +
                                                    removedGood.getName() + " (索引: " + index + ")");
                                        } catch (IndexOutOfBoundsException e) {
                                            printWithTimestamp(Thread.currentThread().getName() + " 删除时捕获异常: " + e.getMessage());
                                        }
                                        printListState(manager);
                                    } else {
                                        printWithTimestamp(Thread.currentThread().getName() + " 尝试删除商品，但列表为空");
                                    }
                                    break;
                                case 2: // 查看
                                    printWithTimestamp(Thread.currentThread().getName() + " 查看商品列表");
                                    printListState(manager);
                                    break;
                                case 3: // 清空
                                    manager.clearGoods();
                                    printWithTimestamp(Thread.currentThread().getName() + " 清空成功");
                                    printListState(manager);
                                    break;
                            }
                        }
                    } catch (Exception e) {
                        errorOccurred.set(true);
                        synchronized (LOG_LOCK) {
                            printWithTimestamp(Thread.currentThread().getName() + " 操作异常: " + e.getMessage());
                        }
                        e.printStackTrace();
                    }
                    // 添加短暂延迟，避免操作过快
                    try {
                        Thread.sleep(100);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
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
        statusThread.interrupt();
        assert !errorOccurred.get() : "并发操作过程中出现异常！";
        assert manager.size() >= 0 : "商品数量不应为负数";
    }

    // 打印带时间戳的日志（自动同步）
    private static void printWithTimestamp(String message) {
        System.out.println(TIMESTAMP_FORMAT.format(new Date()) + " " + message);
    }

    // 打印当前列表状态（自动同步）
    private static void printListState(GoodManager manager) {
        IListImpl<Goods> currentList = manager.getGoodsList();
        printWithTimestamp("当前状态: 数量=" + currentList.size());
        for (int i = 0; i < currentList.size(); i++) {
            printWithTimestamp("  " + i + ": " + currentList.get(i));
        }
    }
}