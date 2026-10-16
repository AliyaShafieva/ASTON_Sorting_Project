package aston.sort;

import aston.util.CustomList;

import java.util.Comparator;

public class Sorter {
    private SortStrategy strategy;

    public Sorter(SortStrategy strategy) {
        this.strategy = strategy;
    }

    public void setStrategy(SortStrategy strategy){
        this.strategy = strategy;
    }

    public SortStrategy getStrategy() {
        return strategy;
    }

    public <T> void sort(CustomList<T> list, Comparator<? super T> comparator){
        strategy.sort(list, comparator);
    }
}
