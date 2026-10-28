package aston.util;

import java.util.ConcurrentModificationException;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Spliterator;
import java.util.Spliterators;

public class CustomList<T> implements Iterable<T> { // T — тип данных, который мы будем хранить
    private static final int DEFAULT_CAPACITY = 10; // Константа, базовый размер массива при создании списка
    private Object[] elements; // Внутренний массив для хранения данных
    private int size = 0;
    private int modCount = 0; // Счетчик модификаций: увеличивается при каждом добавлении, удалении или изменении элемента.
                              // Используется для обнаружения попыток изменить список во время его обхода

    public CustomList() {
        elements = new Object[DEFAULT_CAPACITY]; // Базовая ёмкость
    }

    public void add(T element) { // Метод для добавления элемента в конец списка
        if (size == elements.length) {
            grow(); // Метод для расширения массива
        }
        elements[size++] = element;
        modCount++; // Фиксируем изменение
    }

    // Реализация remove с защитой
    @SuppressWarnings("unchecked")
    public T remove(int index) { // Удаляет элемент по указанному индексу и сдвигает остальные элементы влево.
        if (index >= size || index < 0) // Проверка корректности индекса
            throw new IndexOutOfBoundsException("Index: " + index + ", Size: " + size);
        T removedElement = (T) elements[index];
        int numMoved = size - index - 1; // Определяем, сколько элементов нужно сдвинуть после удаляемого
        if (numMoved > 0)
            System.arraycopy(elements, index + 1, elements, index, numMoved); // Копируем элементы на одно место влево

        elements[--size] = null; // Обнуляем последний элемент и уменьшаем размер списка
        modCount++; // Увеличиваем счетчик, структура списка изменилась
        return removedElement;
    }

    public boolean isEmpty() { // Проверяет, пуст ли список
        return size == 0;
    }

    public boolean contains(T element) { // Проверяет, содержится ли элемент в списке
        return indexOf(element) >= 0;
    }

    public int indexOf(T element) { // Возвращает индекс первого вхождения элемента или -1, если элемент не найден
        if (element == null) {
            for (int i = 0; i < size; i++)
                if (elements[i] == null) return i;
        } else {
            for (int i = 0; i < size; i++)
                if (element.equals(elements[i])) return i;
        }
        return -1;
    }

    public void clear() { // Полностью очищает список
        for (int i = 0; i < size; i++)
            elements[i] = null;
        size = 0;
        modCount++;
    }

    private void grow() { // Увеличивает размер массива в два раза при заполнении
        Object[] newElements = new Object[elements.length * 2];
        System.arraycopy(elements, 0, newElements, 0, elements.length);
        elements = newElements;
    }

    @SuppressWarnings("unchecked")
    public T get(int index) {
        if (index >= size || index < 0)
            throw new IndexOutOfBoundsException("Index: " + index + ", Size: " + size);
        return (T) elements[index];
    }

    public void set(int index, T element) { // Заменяет элемент по индексу на новый
        if (index >= size || index < 0)
            throw new IndexOutOfBoundsException("Index: " + index + ", Size: " + size);
        elements[index] = element;
        modCount++; // Сет тоже меняет структуру данных
    }

    public int size() { return size; }

    @Override
    public Iterator<T> iterator() { // Возвращает итератор для обхода списка
        return new Iterator<T>() {
            private int cursor = 0;
            private final int expectedModCount = modCount; // Запоминаем состояние

            @Override
            public boolean hasNext() { return cursor < size; }

            @Override
            @SuppressWarnings("unchecked")
            public T next() {
                // Проверка на конкурентное изменение
                // Если modCount изменился (был вызван add/remove/set), значит список был изменен
                // сторонним кодом во время итерации — выбрасываем исключение.
                if (modCount != expectedModCount)
                    throw new ConcurrentModificationException("Список был изменен во время итерации!");

                if (!hasNext()) throw new NoSuchElementException();
                return (T) elements[cursor++];
            }
        };
    }


    @Override
    public Spliterator<T> spliterator() {   /* Создает сплитератор для обхода элементов списка.
                                             * Реализация Spliterator позволяет использовать Stream API над коллекцией,
                                             * обеспечивая эффективную последовательную и параллельную обработку данных.
                                             */
        return Spliterators.spliterator(
                elements,        // Исходный массив данных
                0,               // Начальная позиция
                size,            // Конечная позиция (количество реально занятых элементов)
                Spliterator.ORDERED |    // Гарантирует сохранение порядка элементов
                        Spliterator.SIZED |     // Сообщает размер коллекции, оптимизируя выделение памяти
                        Spliterator.SUBSIZED |  // Позволяет эффективно делить список для параллельной обработки
                        Spliterator.IMMUTABLE   // Указывает на то, что структура не меняется в рамках текущей итерации
        );
    }
}
