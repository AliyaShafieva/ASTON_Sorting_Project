package aston.util;

import java.util.Iterator;
import java.util.NoSuchElementException;

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

    public void set(int index, T element){
        if (index >= size || index < 0) throw new IndexOutOfBoundsException();
        elements[index] = element;
    }

    public int size() {
        return size;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < size; i++){
            if (i > 0) sb.append(", ");
            sb.append(elements[i]);
        }
        return  sb.append("]").toString();
    }

    @Override
    public Iterator<T> iterator() {
        return new Iterator<T>() {
            private int cursor = 0;
            @Override
            public boolean hasNext() {
                return cursor < size;
            }
            @Override
            @SuppressWarnings("unchecked")
            public T next() {
                if (!hasNext()) throw new NoSuchElementException();
                return (T) elements[cursor++];
            }
        };
    }



}
