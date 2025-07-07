package com.zyh.Training2;

import java.util.Comparator;
import java.util.Iterator;


class Node<E> {
    E data;
    Node<E> next;

    Node(E data) {
        this.data = data;
        this.next = null;
    }
}


public class IListImpl<E> extends Object implements IList<E>{
    private Node<E> head;
    private int size;

    public IListImpl() {
        head = null;
        size = 0;
    }

    /**
     * 获取链表的大小
     * @return 链表的元素个数
     */
    @Override
    public int size() {
        return size;
    }

    /**
     * 判断链表是否为空
     * @return 链表为空返回true，否则返回false
     */
    @Override
    public boolean isEmpty() {
        return size == 0;
    }

    /**
     * 将链表转换为数组
     * @param a 目标数组
     * @return 包含链表元素的数组
     */
    @Override
    public <T> T[] toArray(T[] a) {
        if (a.length < size) {
            a = (T[]) java.lang.reflect.Array.newInstance(a.getClass().getComponentType(), size);
        }

        Object[] result = a;
        Node<E> current = head;
        for (int i = 0; i < size; i++) {
            result[i] = current.data;
            current = current.next;
        }

        if (a.length > size) {
            a[size] = null;
        }

        return a;
    }

    /**
     * 添加元素到链表末尾
     * @param e 要添加的元素
     * @return 添加成功返回true
     */
    @Override
    public boolean add(E e) {
        // 在链表末尾添加元素
        add(size, e);
        return true;
    }

    /**
     * 使用归并排序对链表进行排序
     * @param c 比较器
     */
    @Override
    public void sort(Comparator<? super E> c) {
        if (size > 1) {
            head = mergeSort(head, c);
        }
    }

    /**
     * 归并排序主逻辑，对链表进行递归分割和合并
     * @param head 当前子链表的头结点
     * @param c 比较器
     * @return 排序后的子链表头结点
     */
    private Node<E> mergeSort(Node<E> head, Comparator<? super E> c) {
        if (head == null || head.next == null) {
            return head;
        }

        // 链表中点断开，分为左右两部分
        Node<E> middle = getMiddle(head);
        Node<E> nextOfMiddle = middle.next;
        middle.next = null;

        // 递归排序左右子链表
        Node<E> left = mergeSort(head, c);
        Node<E> right = mergeSort(nextOfMiddle, c);

        // 合并两个有序子链表
        return merge(left, right, c);
    }

    /**
     * 快慢指针法找到链表的中间节点
     * @param head 子链表头结点
     * @return 中间节点
     */
    private Node<E> getMiddle(Node<E> head) {
        if (head == null) {
            return null;
        }

        Node<E> slow = head;
        Node<E> fast = head.next;

        while (fast != null && fast.next != null) {
            slow = slow.next;
            fast = fast.next.next;
        }

        return slow;
    }

    /**
     * 合并两个已排序的子链表
     * @param a 第一个子链表的头节点
     * @param b 第二个子链表的头节点
     * @param c 比较器
     * @return 合并后的链表头节点
     */
    private Node<E> merge(Node<E> a, Node<E> b, Comparator<? super E> c) {
        Node<E> result;
        if (a == null) {
            return b;
        }
        if (b == null) {
            return a;
        }

        // 比较两个节点的数据，选择较小的作为头节点
        if (c.compare(a.data, b.data) <= 0) {
            result = a;
            result.next = merge(a.next, b, c);
        } else {
            result = b;
            result.next = merge(a, b.next, c);
        }

        return result;
    }

    /**
     * 清空链表
     */
    @Override
    public void clear() {
        head = null;
        size = 0;
    }

    /**
     * 获取指定索引处的元素
     * @param index 索引位置
     * @return 索引处的元素
     */
    @Override
    public E get(int index) {
        checkExistIndex(index);
        Node<E> current = getNode(index);
        if (current == null) {
            throw new IndexOutOfBoundsException("Index: " + index + ", Size: " + size);
        }
        return current.data;
    }

    /**
     * 设置指定索引处的元素
     * @param index 索引位置
     * @param element 要设置的元素
     * @return 被替换的旧元素
     */
    @Override
    public E set(int index, E element) {
        checkExistIndex(index);
        Node<E> node = getNode(index);
        E oldValue = node.data;
        node.data = element;
        return oldValue;
    }

    /**
     * 在指定索引处添加元素
     * @param index 索引位置
     * @param element 要添加的元素
     */
    @Override
    public void add(int index, E element) {
        checkNewIndex(index);

        if (index == 0) {
            Node<E> newNode = new Node<>(element);
            newNode.next = head;
            head = newNode;
        } else {
            Node<E> prev = getNode(index - 1);
            Node<E> newNode = new Node<>(element);
            newNode.next = prev.next;
            prev.next = newNode;
        }
        size++;
    }

    /**
     * 移除指定索引处的元素
     * @param index 索引位置
     * @return 被移除的元素
     */
    @Override
    public E remove(int index) {
        checkExistIndex(index);
        Node<E> removeNode;
        if (index == 0) {
            removeNode = head;
            head = removeNode.next;
        } else {
            Node<E> prev = getNode(index - 1);
            removeNode = prev.next;
            prev.next = removeNode.next;
        }
        size--;
        return removeNode.data;
    }

    /**
     * 查找元素在链表中的索引
     * @param o 要查找的元素
     * @return 元素的索引，如果不存在则返回-1
     */
    @Override
    public int indexOf(Object o) {
        int index = 0;
        Node<E> current = head;

        if (o == null) {
            while (current != null) {
                if (current.data == null) {
                    return index;
                }
                current = current.next;
                index++;
            }
        } else {
            while (current != null) {
                if (o.equals(current.data)) {
                    return index;
                }
                current = current.next;
                index++;
            }
        }

        return -1;
    }

    @Override
    public Iterator<E> iterator() {
        return new Iterator<E>() {
            private Node<E> current = head;

            @Override
            public boolean hasNext() {
                return current != null;
            }

            @Override
            public E next() {
                if (!hasNext()) {
                    throw new java.util.NoSuchElementException();
                }
                E data = current.data;
                current = current.next;
                return data;
            }
        };
    }

    /**
     * 获取指定索引处的节点
     * @param index 索引位置
     * @return 索引处的节点
     */
    private Node<E> getNode(int index) {
        Node<E> current = head;
        for (int i = 0; i < index; i++) {
            current = current.next;
        }
        return current;
    }

    /**
     * 检查已有索引是否在有效范围内
     * @param index 索引位置
     * @throws IndexOutOfBoundsException 如果索引不在范围内
     */
    private void checkExistIndex(int index) {
        if (index < 0 || index >= size) {
            throw new IndexOutOfBoundsException("Index: " + index + ", Size: " + size);
        }
    }

    /**
     * 检查新索引是否在有效范围内
     * @param index 新索引位置
     * @throws IndexOutOfBoundsException 如果新索引不在范围内
     */
    private void checkNewIndex(int index) {
        if (index < 0 || index > size) {
            throw new IndexOutOfBoundsException("Index: " + index + ", Size: " + size);
        }
    }
}
