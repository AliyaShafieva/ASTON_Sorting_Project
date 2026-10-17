package aston.sort;

import aston.util.CustomList;
import java.util.Comparator;
/// Стратегия сортировки - общий интерфейс для всех алгоритмов (паттерн **Strategy**).
/// Каждый алгоритм реализует этот интерфейс, поэтому их можно подставлять друг
/// вместо друга через [Sorter], остальной код менять не нужно!.
///
/// Реализации: [InsertionSort], [BubbleSort].
public interface SortStrategy {
    /// @param list       список для сортировки; пустой список или один элемент допустимы, переставляем элементы внутри нашего списка.
    /// @param comparator правило сравнения, `AutomobileComparators.BY_MODEL`. Алгоритм не привязан к конкретному полю.
    /// @param <T>        тип элементов списка, сортируем список любого типа.
    <T> void sort(CustomList<T> list, Comparator<? super T> comparator);
    /// Название алгоритма для меню, например «Сортировка вставками».
    /// @return название алгоритма
    String name();
}
