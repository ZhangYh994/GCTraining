package com.zyh.Training3;

import com.zyh.Training2.IListImpl;

import java.util.Iterator;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class GoodManager {
    private final IListImpl<Goods> goodsList = new IListImpl<>();
    private final ReentrantReadWriteLock lock = new ReentrantReadWriteLock();

    // 添加商品
    public void addGoods(Goods goods) {
        lock.writeLock().lock();
        try {
            goodsList.add(goods);
        } finally {
            lock.writeLock().unlock();
        }
    }

    // 删除指定索引商品
    public Goods removeGoods(int index) {
        lock.writeLock().lock();
        try {
            int size = goodsList.size();
            if (size == 0 || index < 0 || index >= size) {
                throw new IndexOutOfBoundsException("索引 " + index + " 不合法，无法删除商品。");
            }
            return goodsList.remove(index);
        } finally {
            lock.writeLock().unlock();
        }
    }

    // 获取商品列表快照
    public IListImpl<Goods> getGoodsList() {
        lock.readLock().lock();
        try {
            IListImpl<Goods> snapshot = new IListImpl<>();
            Iterator<Goods> it = goodsList.iterator();
            while (it.hasNext()) {
                snapshot.add(it.next());
            }
            return snapshot;
        } finally {
            lock.readLock().unlock();
        }
    }

    // 清空商品
    public void clearGoods() {
        lock.writeLock().lock();
        try {
            goodsList.clear();
        } finally {
            lock.writeLock().unlock();
        }
    }

    // 获取商品数量
    public int size() {
        lock.readLock().lock();
        try {
            return goodsList.size();
        } finally {
            lock.readLock().unlock();
        }
    }
}
