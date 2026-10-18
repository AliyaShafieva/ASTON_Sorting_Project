package aston.sort;

import aston.util.CustomList;
import java.util.Comparator;
import java.util.function.ToIntFunction;
/// Сортировка **чётных** значений числового поля (доп. 1).
/// Объекты, у которых значение поля чётное, сортируются по этому полю по возрастанию.
/// Объекты с нечётным значением остаются на своих местах.
/// используется любая [SortStrategy] - [InsertionSort] или [BubbleSort].
/// ```
/// //пузырьком
/// EvenValuesSorter sorter = new EvenValuesSorter(new BubbleSort());
/// //или вставками
/// EvenValuesSorter sorter = new EvenValuesSorter(new InsertionSort());
/// sorter.sort(cars, Automobile::getPower); ```
public class EvenValuesSorter {

    private final SortStrategy strategy;
    /// @param strategy алгоритм, которым сортируются чётные элементы
    public EvenValuesSorter(SortStrategy strategy) {
        this.strategy = strategy;
    }
    /// Сортирует чётные по полю `field` элементы, нечётные не трогает.
    ///
    /// @param list  список; меняется на месте
    /// @param field числовое поле, например `Automobile::getPower`
    /// @param <T>   тип элементов списка
    public <T> void sort(CustomList<T> list, ToIntFunction<? super T> field) {
        CustomList<T> evens = new CustomList<>();
        CustomList<Integer> positions = new CustomList<>();

        for (int i = 0; i < list.size(); i++) {
            T element = list.get(i);
            if (field.applyAsInt(element) % 2 == 0) {
                evens.add(element);
                positions.add(i);
            }
        }

        strategy.sort(evens, Comparator.comparingInt(field));

        for (int k = 0; k < evens.size(); k++) {
            list.set(positions.get(k), evens.get(k));
        }
    }
}
