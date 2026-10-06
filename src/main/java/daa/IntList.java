package daa;

/** Common interface used by the DynamicArray and MyLinkedList benchmarks. */
public interface IntList {
    void add(int x);
    void add(int index, int x);
    int remove(int index);
    int get(int index);
    boolean contains(int x);
    int size();
}
