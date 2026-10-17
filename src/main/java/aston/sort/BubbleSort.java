package aston.sort;

import aston.util.CustomList;
import java.util.Comparator;
/// Сортировка **пузырьком**.
/// Проходит по списку и сравнивает соседние элементы: если левый больше
/// правого, меняет их местами. За каждый проход самый большой из оставшихся
/// элементов «всплывает» в конец списка.
///
/// Равные элементы сохраняют исходный порядок
public class BubbleSort implements SortStrategy {

    @Override
    public <T> void sort(CustomList<T> list, Comparator<? super T> comparator) {
        int n = list.size();
        for (int pass = 0; pass < n - 1; pass++) {
            boolean swapped = false;
            for (int i = 0; i < n - 1 - pass; i++) {
                T left = list.get(i);
                T right = list.get(i + 1);
                if (comparator.compare(left, right) > 0) {
                    list.set(i, right);
                    list.set(i + 1, left);
                    swapped = true;
                }
            }
            if (!swapped) {
                break;
            }
        }
    }

    @Override
    public String name() {
        return "Сортировка пузырьком";
    }
}
