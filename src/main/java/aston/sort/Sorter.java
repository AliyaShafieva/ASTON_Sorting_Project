package aston.sort;

import aston.util.CustomList;
import java.util.Comparator;
/// Контекст паттерна **Strategy**: хранит выбранный алгоритм сортировки и поручает ему работу.
/// Меню работает только с этим классом и не знает, какой алгоритм внутри:
/// ```
/// Sorter sorter = new Sorter(new InsertionSort());
/// sorter.sort(cars, AutomobileComparators.BY_MODEL);
/// sorter.setStrategy(new MergeSort());   // сменили алгоритм
/// ```
public class Sorter {
    private SortStrategy strategy;
    /// Создаёт сортировщик с начальным алгоритмом.
    /// @param strategy алгоритм сортировки
    public Sorter(SortStrategy strategy) {
        this.strategy = strategy;
    }
    /// Меняет алгоритм сортировки. Следующий вызов [#sort] будет использовать новый алгоритм сортировки.
    /// @param strategy новый алгоритм сортировки
    public void setStrategy(SortStrategy strategy){
        this.strategy = strategy;
    }
    /// Текущий алгоритм - например, чтобы показать в меню его [SortStrategy#name()].
    /// @return текущий алгоритм сортировки
    public SortStrategy getStrategy() {
        return strategy;
    }
    /// Сортирует список текущим алгоритмом.
    /// @param list       список для сортировки
    /// @param comparator правило сравнения, например `AutomobileComparators.BY_MODEL`
    /// @param <T>        тип элементов списка
    public <T> void sort(CustomList<T> list, Comparator<? super T> comparator){
        strategy.sort(list, comparator);
    }
}
