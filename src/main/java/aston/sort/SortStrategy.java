package aston.sort;

import aston.util.CustomList;

import java.util.Comparator;

public interface SortStrategy {
    <T> void sort(CustomList<T> list, Comparator<? super T> comparator);

    String name();
}
