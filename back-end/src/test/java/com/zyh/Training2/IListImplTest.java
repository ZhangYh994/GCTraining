package com.zyh.Training2;

import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;
import java.util.Comparator;


class IListImplTest {
    private IListImpl<Integer> list;

    @BeforeEach
    void setUp() {
        list = new IListImpl<>();
    }

    @Test
    void testSize() {
        assertEquals(0, list.size());
        list.add(1);
        assertEquals(1, list.size());
        list.add(2);
        assertEquals(2, list.size());
        list.remove(0);
        assertEquals(1, list.size());
        list.clear();
        assertEquals(0, list.size());
        list.clear();
        assertEquals(0, list.size());
        for (int i = 0; i < 100; i++) list.add(i);
        assertEquals(100, list.size());
        for (int i = 0; i < 100; i++) list.remove(0);
        assertEquals(0, list.size());
    }

    @Test
    void testIsEmpty() {
        assertTrue(list.isEmpty());
        list.add(1);
        assertFalse(list.isEmpty());
        list.remove(0);
        assertTrue(list.isEmpty());
        list.clear();
        assertTrue(list.isEmpty());
        for (int i = 0; i < 10; i++) list.add(i);
        assertFalse(list.isEmpty());
        list.clear();
        assertTrue(list.isEmpty());
    }

    @Test
    void testToArray() {
        list.add(1);
        list.add(2);
        list.add(3);

        Integer[] array = new Integer[3];
        array = list.toArray(array);
        assertArrayEquals(new Integer[]{1, 2, 3}, array);

        Integer[] smallArray = new Integer[1];
        smallArray = list.toArray(smallArray);
        assertArrayEquals(new Integer[]{1, 2, 3}, smallArray);

        Integer[] largeArray = new Integer[5];
        largeArray = list.toArray(largeArray);
        assertArrayEquals(new Integer[]{1, 2, 3, null, null}, largeArray);

        list.clear();
        Integer[] emptyArray = new Integer[0];
        emptyArray = list.toArray(emptyArray);
        assertArrayEquals(new Integer[]{}, emptyArray);
        Integer[] big = new Integer[10];
        big = list.toArray(big);
        assertArrayEquals(new Integer[]{null, null, null, null, null, null, null, null, null, null}, big);
    }

    @Test
    void testAdd() {
        assertTrue(list.add(1));
        assertEquals(1, list.size());
        assertEquals(1, list.get(0));

        assertTrue(list.add(2));
        assertEquals(2, list.size());
        assertEquals(2, list.get(1));

        assertTrue(list.add(null));
        assertEquals(3, list.size());
        assertNull(list.get(2));

        for (int i = 0; i < 10; i++) list.add(i);
        assertEquals(13, list.size());
    }

    @Test
    void testSort() {
        list.add(3);
        list.add(1);
        list.add(4);
        list.add(2);

        list.sort(Comparator.naturalOrder());
        assertEquals(1, list.get(0));
        assertEquals(2, list.get(1));
        assertEquals(3, list.get(2));
        assertEquals(4, list.get(3));

        list.sort(Comparator.reverseOrder());
        assertEquals(4, list.get(0));
        assertEquals(3, list.get(1));
        assertEquals(2, list.get(2));
        assertEquals(1, list.get(3));

        list.clear();
        list.sort(Comparator.naturalOrder());
        assertEquals(0, list.size());

        list.add(99);
        list.sort(Comparator.naturalOrder());
        assertEquals(99, list.get(0));

        list.add(null);
        assertDoesNotThrow(() -> list.sort(Comparator.nullsLast(Comparator.naturalOrder())));
    }

    @Test
    void testClear() {
        list.add(1);
        list.add(2);
        list.clear();
        assertEquals(0, list.size());
        assertTrue(list.isEmpty());

        list.clear();
        assertEquals(0, list.size());

        list.add(5);
        assertEquals(1, list.size());
        list.clear();
        assertEquals(0, list.size());
    }

    @Test
    void testGet() {
        list.add(1);
        list.add(2);
        list.add(3);

        assertEquals(1, list.get(0));
        assertEquals(2, list.get(1));
        assertEquals(3, list.get(2));

        assertThrows(IndexOutOfBoundsException.class, () -> list.get(-1));
        assertThrows(IndexOutOfBoundsException.class, () -> list.get(3));

        list.clear();
        assertThrows(IndexOutOfBoundsException.class, () -> list.get(0));

        list.add(10);
        assertThrows(IndexOutOfBoundsException.class, () -> list.get(1));
    }

    @Test
    void testSet() {
        list.add(1);
        list.add(2);
        list.add(3);

        assertEquals(2, list.set(1, 5));
        assertEquals(5, list.get(1));

        assertThrows(IndexOutOfBoundsException.class, () -> list.set(-1, 0));
        assertThrows(IndexOutOfBoundsException.class, () -> list.set(3, 0));

        list.clear();
        assertThrows(IndexOutOfBoundsException.class, () -> list.set(0, 1));

        list.add(7);
        assertEquals(7, list.set(0, null));
        assertNull(list.get(0));
    }

    @Test
    void testAddAtIndex() {
        list.add(0, 1);
        assertEquals(1, list.get(0));

        list.add(0, 2);
        assertEquals(2, list.get(0));
        assertEquals(1, list.get(1));

        list.add(1, 3);
        assertEquals(2, list.get(0));
        assertEquals(3, list.get(1));
        assertEquals(1, list.get(2));

        assertThrows(IndexOutOfBoundsException.class, () -> list.add(-1, 0));
        assertThrows(IndexOutOfBoundsException.class, () -> list.add(4, 0));

        list.add(list.size(), 9);
        assertEquals(9, list.get(list.size() - 1));

        list.add(0, null);
        assertNull(list.get(0));
    }

    @Test
    void testRemove() {
        list.add(1);
        list.add(2);
        list.add(3);

        assertEquals(2, list.remove(1));
        assertEquals(2, list.size());
        assertEquals(1, list.get(0));
        assertEquals(3, list.get(1));

        assertEquals(1, list.remove(0));
        assertEquals(1, list.size());

        assertThrows(IndexOutOfBoundsException.class, () -> list.remove(-1));
        assertThrows(IndexOutOfBoundsException.class, () -> list.remove(1));

        assertEquals(3, list.remove(0));
        assertEquals(0, list.size());

        assertThrows(IndexOutOfBoundsException.class, () -> list.remove(0));
    }

    @Test
    void testIndexOf() {
        list.add(1);
        list.add(2);
        list.add(3);
        list.add(null);
        list.add(2);

        assertEquals(0, list.indexOf(1));
        assertEquals(1, list.indexOf(2));
        assertEquals(2, list.indexOf(3));
        assertEquals(3, list.indexOf(null));
        assertEquals(-1, list.indexOf(4));
        assertEquals(1, list.indexOf(2));

        list.clear();
        assertEquals(-1, list.indexOf(1));

        for (int i = 0; i < 5; i++) list.add(null);
        assertEquals(0, list.indexOf(null));
    }
}