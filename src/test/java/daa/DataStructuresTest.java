package daa;

import org.junit.jupiter.api.Test;
import java.util.ArrayList;
import java.util.PriorityQueue;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.*;

class DataStructuresTest {
    @Test void dynamicArrayBasicAndEdges() {
        Metrics m = new Metrics();
        DynamicArray a = new DynamicArray(1, m);
        assertEquals(0, a.size());
        a.add(10); a.add(20); a.add(20); a.add(1, 15);
        assertEquals(4, a.size());
        assertEquals(10, a.get(0));
        assertEquals(20, a.get(a.size() - 1));
        assertTrue(a.contains(15));
        assertTrue(a.contains(20));
        assertFalse(a.contains(999));
        assertEquals(15, a.remove(1));
        assertEquals(20, a.remove(a.size() - 1));
        assertEquals(2, a.size());
    }

    @Test void dynamicArrayInvalidAndEmpty() {
        DynamicArray a = new DynamicArray();
        assertThrows(IndexOutOfBoundsException.class, () -> a.get(0));
        assertThrows(IndexOutOfBoundsException.class, () -> a.remove(0));
        assertThrows(IndexOutOfBoundsException.class, () -> a.add(2, 1));
        a.add(1);
        assertThrows(IndexOutOfBoundsException.class, () -> a.get(-1));
        assertThrows(IndexOutOfBoundsException.class, () -> a.add(2, 1));
    }

    @Test void dynamicArrayRandomAgreement() {
        Random r = new Random(42);
        DynamicArray a = new DynamicArray(2);
        ArrayList<Integer> expected = new ArrayList<>();
        for (int step = 0; step < 500; step++) {
            int op = r.nextInt(3);
            if (op == 0 || expected.isEmpty()) {
                int x = r.nextInt(1000);
                a.add(x); expected.add(x);
            } else if (op == 1) {
                int index = r.nextInt(expected.size());
                assertEquals(expected.get(index), a.get(index));
            } else {
                int index = r.nextInt(expected.size());
                assertEquals(expected.remove(index), a.remove(index));
            }
            assertEquals(expected.size(), a.size());
            for (int i = 0; i < expected.size(); i++) assertEquals(expected.get(i), a.get(i));
        }
    }

    @Test void linkedListEdgesAndTailRemoval() {
        MyLinkedList l = new MyLinkedList();
        assertEquals(0, l.size());
        l.add(10); l.add(30); l.add(1, 20); l.add(40);
        assertEquals(10, l.get(0));
        assertEquals(40, l.get(l.size() - 1));
        assertTrue(l.contains(20));
        assertTrue(l.contains(40));
        assertFalse(l.contains(999));
        assertEquals(40, l.remove(l.size() - 1)); // must update tail
        assertEquals(30, l.remove(l.size() - 1));
        assertEquals(10, l.remove(0));
        assertEquals(20, l.remove(0));
        assertEquals(0, l.size());
    }

    @Test void linkedListInvalidAndSingleElement() {
        MyLinkedList l = new MyLinkedList();
        assertThrows(IndexOutOfBoundsException.class, () -> l.get(0));
        assertThrows(IndexOutOfBoundsException.class, () -> l.remove(0));
        assertThrows(IndexOutOfBoundsException.class, () -> l.add(2, 1));
        l.add(7);
        assertEquals(7, l.get(0));
        assertEquals(7, l.remove(0));
        assertEquals(0, l.size());
    }

    @Test void linkedListRandomAgreement() {
        Random r = new Random(42);
        MyLinkedList list = new MyLinkedList();
        ArrayList<Integer> expected = new ArrayList<>();
        for (int step = 0; step < 500; step++) {
            int op = r.nextInt(3);
            if (op == 0 || expected.isEmpty()) {
                int x = r.nextInt(1000);
                int index = r.nextInt(expected.size() + 1);
                list.add(index, x); expected.add(index, x);
            } else if (op == 1) {
                int index = r.nextInt(expected.size());
                assertEquals(expected.get(index), list.get(index));
            } else {
                int index = r.nextInt(expected.size());
                assertEquals(expected.remove(index), list.remove(index));
            }
            assertEquals(expected.size(), list.size());
            for (int i = 0; i < expected.size(); i++) assertEquals(expected.get(i), list.get(i));
        }
    }

    @Test void heapPropertyAfterEveryOperation() {
        MinHeap h = new MinHeap();
        int[] values = {7, 2, 9, 1, 5, 1, 8, -3, 20};
        for (int x : values) {
            h.insert(x);
            assertTrue(h.isHeapPropertyValid());
        }
        while (h.size() > 0) {
            h.extractMin();
            assertTrue(h.isHeapPropertyValid());
        }
    }

    @Test void heapRandomAgreementWithPriorityQueue() {
        Random r = new Random(42);
        MinHeap actual = new MinHeap();
        PriorityQueue<Integer> expected = new PriorityQueue<>();
        for (int i = 0; i < 500; i++) {
            int x = r.nextInt(1000) - 500;
            actual.insert(x); expected.add(x);
            assertTrue(actual.isHeapPropertyValid());
        }
        while (!expected.isEmpty()) {
            assertEquals(expected.poll().intValue(), actual.extractMin());
            assertTrue(actual.isHeapPropertyValid());
        }
        assertEquals(0, actual.size());
    }

    @Test void heapEmptyAndPeek() {
        MinHeap h = new MinHeap();
        assertThrows(IllegalStateException.class, h::peekMin);
        assertThrows(IllegalStateException.class, h::extractMin);
        h.insert(5);
        assertEquals(5, h.peekMin());
        assertTrue(h.isHeapPropertyValid());
    }
}
