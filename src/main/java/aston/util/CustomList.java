package aston.util;

import java.util.Iterator;

public class CustomList<T> implements Iterable<T> { // T — тип данных, который мы будем хранить
    private Object[] elements;
    private int size = 0;

    public CustomList() {
        elements = new Object[10]; // Базовая емкость
    }

    public void add(T element) {
        if (size == elements.length) {
            grow(); // Метод для расширения массива
        }
        elements[size++] = element;
    }

    private void grow() {
        Object[] newElements = new Object[elements.length * 2];
        System.arraycopy(elements, 0, newElements, 0, elements.length);
        elements = newElements;
    }

    @SuppressWarnings("unchecked")
    public T get(int index) {
        if (index >= size || index < 0) throw new IndexOutOfBoundsException();
        return (T) elements[index];
    }

    public int size() {
        return size;
    }

    @Override
    public Iterator<T> iterator() {
        return new Iterator<T>() {
            private int cursor = 0;
            @Override
            public boolean hasNext() { return cursor < size; }
            @Override
            public T next() { return (T) elements[cursor++]; }
        };
    }
}
