package aston.util;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.Callable; //интерфейс для выполнения задачи и возврата результата
import java.util.concurrent.ExecutionException; //исключение, связанное с получением результата через Future
import java.util.concurrent.ExecutorService; //интерфейс для управления выполнением задач в потоках
import java.util.concurrent.Executors; //вспомогательный класс, предоставляющий методы для создания исполнителей задач
import java.util.concurrent.Future; //предоставляет результаты асинхронно выполняемой задачи

public final class ParallelOccurrenceCounter {

    private ParallelOccurrenceCounter() {
        // Запрещаем создание экземпляров утилитарного класса
    }

    /**
     * Подсчитывает количество вхождений элемента в коллекцию
     * с использованием нескольких потоков.
     *
     * @param collection коллекция для поиска
     * @param target искомый элемент
     * @param threadCount количество потоков
     * @param <T> тип элементов коллекции
     * @return количество вхождений элемента
     */
    public static <T> long countOccurrences(
            CustomList<T> collection,
            T target,
            int threadCount //кол-во потоков
    ) {
        Objects.requireNonNull(collection, "Коллекция не должна быть null"); //проверка коллекции на null

        if (threadCount <= 0) { //проверка кол-ва потоков
            throw new IllegalArgumentException(
                    "Количество потоков должно быть больше нуля"
            );
        }

        int size = collection.size(); //получение кол-ва элементов в коллекции

        if (size == 0) { //обработка пустой коллекции
            System.out.println(
                    "Элемент " + target + " встречается в коллекции 0 раз(а)."
            );
            return 0;
        }

        int actualThreadCount = Math.min(threadCount, size); //определяем кол-во потоков, выбирая минимальное из threadCount и size
        int chunkSize = (size + actualThreadCount - 1) / actualThreadCount; //вычисление максимального размера одного диапазона обработки
        //идёт округление вверх

        ExecutorService executor =
                Executors.newFixedThreadPool(actualThreadCount); //запуск механизма управления рабочими потоками

        List<Future<Long>> futures = new ArrayList<>(); //создание списка результатов Future
        //В futures хранятся не сами числа, а объекты Future<Long>, через которые можно получить результаты вычислений

        try {
            for (int thread = 0; thread < actualThreadCount; thread++) { //цикл создания задач
                int start = thread * chunkSize; //начальный индекс
                int end = Math.min(start + chunkSize, size); //конечный индекс

                if (start >= end) { //проверка, есть ли элементы в диапазоне
                    break;
                }

                Callable<Long> task = () -> { //создание задачи
                    long count = 0;

                    for (int i = start; i < end; i++) { //подсчёт вхождений элемента в диапазоне
                        if (Objects.equals(collection.get(i), target)) {
                            count++;
                        }
                    }

                    return count; //возврат числа найденных совпадений
                };

                futures.add(executor.submit(task)); //отправка задачи исполнителю
            }

            long totalCount = 0; //итоговое кол-во вхождений элемента во всей коллекции

            for (Future<Long> future : futures) { //суммирование всех вхождений в единый счётчик
                totalCount += future.get();
            }

            System.out.println( //вывод результата в консоль
                    "Элемент " + target
                            + " встречается в коллекции "
                            + totalCount + " раз(а)."
            );

            return totalCount; //метод возвращает итоговое кол-во вхождений вызывающему коду

        // обработка исключений
        } catch (InterruptedException e) { //Если задача ещё не завершена, поток ожидает её результата. Однако во время этого ожидания
            //поток может получить сигнал прерывания
            //Исключение не означает, что поиск элемента не удался. Оно означает, что ожидание результата было прервано
            Thread.currentThread().interrupt();
            throw new IllegalStateException(
                    "Подсчёт был прерван", e
            );

        } catch (ExecutionException e) { //связан с ошибками, которые могут возникнуть внутри рабочей задачи
            throw new IllegalStateException(
                    "Ошибка при выполнении задачи в потоке", e.getCause()
            );

        } finally { //корректное завершение исполнителя задач
            executor.shutdown();
        }
    }
}

