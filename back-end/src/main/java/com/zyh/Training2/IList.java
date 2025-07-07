package com.zyh.Training2;


import java.util.Comparator;
import java.util.Iterator;

public interface IList<E>{
    int size();

    boolean isEmpty();

    <T> T[]toArray(T[] a);

    boolean add(E e);

    void sort(Comparator<?super E> c);

    void clear();

    E get(int index);

    E set(int index,E element);

    void add(int index,E element);

    E remove(int index);

    int indexOf(Object o);

    Iterator<E> iterator();
}