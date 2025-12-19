
package coursework;

import java.io.Serializable;
import java.util.Arrays;

/**
 * Minimal circular queue (FIFO) with fixed capacity.
 * Purpose:
 * - Used to keep ONLY the last 4 activities per product.
 * - When the queue is full, enqueue() automatically discards the oldest item.
 * <p>
 * Serialization:
 * - Implements Serializable so the queue contents can be saved/restored with products.
 * <p>
 * Arrays & generics note:
 * - Provides a typed toArray(T[] a) so callers receive the correct runtime array type
 *   (e.g., Activity[]) and avoid ClassCastException when returning/using arrays.
 */
public class Queue<T> implements Serializable {
    private static final long serialVersionUID = 1L;

    /** Backing array storing elements in circular fashion. */
    private T[] data;

    /** Index of the logical head (oldest element). */
    private int front;

    /** Index of the last inserted element. */
    private int rear;

    /** Current number of elements in the queue. */
    private int size;

    /** Maximum capacity of the queue. */
    private int capacity;

    /**
     * Constructs a fixed-capacity circular queue.
     *
     * @param capacity max number of entries retained (e.g., 4 activities)
     */
    @SuppressWarnings("unchecked")
    public Queue(int capacity) {
        this.capacity = capacity;
        this.data = (T[]) new Object[capacity]; // backing array
        this.front = 0;
        this.rear = -1;
        this.size = 0;
    }

    /**
     * Adds an item to the tail (rear).
     * If at capacity, the oldest item is removed automatically (FIFO).
     *
     * Amortized O(1).
     *
     * @param item element to enqueue
     */
    public void enqueue(T item) {
        if (size == capacity) {
            dequeue();         // drop oldest when full
        }
        rear = (rear + 1) % capacity;
        data[rear] = item;
        size++;
    }

    /**
     * Removes and returns the oldest item from the head (front).
     *
     * @return the removed item, or null if the queue is empty
     */
    public T dequeue() {
        if (isEmpty()) return null;
        T removed = data[front];
        front = (front + 1) % capacity;
        size--;
        return removed;
    }

    /** @return true if no elements are stored */
    public boolean isEmpty() { return size == 0; }

    /** @return true if size == capacity */
    public boolean isFull()  { return size == capacity; }

    /** @return current number of elements in the queue */
    public int getSize()     { return size; }

    /**
     * Returns a typed array containing elements in FIFO order.
     * If the provided array is too small, a new one is created (preserving type).
     *
     * This avoids returning Object[] and the resulting ClassCastException
     * when callers expect a specific component type like Activity[].
     *
     * @param a destination array (typed), e.g., new Activity[0]
     * @return array of elements with the same runtime type as 'a'
     */
    public T[] toArray(T[] a) {
        if (a.length < size) a = Arrays.copyOf(a, size); // preserve component type
        for (int i = 0; i < size; i++) {
            a[i] = data[(front + i) % capacity];         // logical order: front -> rear
        }
        if (a.length > size) a[size] = null;             // optional per Collection.toArray contract
        return a;
    }
}
