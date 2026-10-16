package aston.sort;

import aston.util.CustomList;

import java.util.Comparator;

public class InsertionSort implements SortStrategy{
    @Override
    public <T> void sort(CustomList<T> list, Comparator<? super T> comparator) {
        for (int i = 1; i < list.size(); i++){
            T current = list.get(i);
            int j = i - 1;
            while (j >= 0 && comparator.compare(list.get(j), current) > 0){
                list.set(j + 1, list.get(j));
                j--;
            }
            list.set(j + 1, current);
        }
    }

    @Override
    public String name() {
        return "Сортировка вставками";
    }
}
