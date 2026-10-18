package aston.sort;

import aston.core.Automobile;
import aston.util.CustomList;
import static aston.sort.SortTestHelper.*;

public class EvenValuesSorterTest {

    public static void main(String[] args) {

        reset();

        SortStrategy[] strategies = {new InsertionSort(), new BubbleSort()};

        for (SortStrategy strategy : strategies) {
            System.out.println("=== " + strategy.name() + " ===");
            EvenValuesSorter sorter = new EvenValuesSorter(strategy);
            sortsOnlyEvenNumbers(sorter);
            allOddStayInPlace(sorter);
            allEvenAreFullySorted(sorter);
            worksWithNegativeNumbers(sorter);
            emptyListDoesNotFail(sorter);
            sortsCarsByEvenPower(sorter);
            sortsCarsByEvenYear(sorter);
            System.out.println();
        }

        printTotal();
    }
    /// Пример из задания: чётные 8, 2, 6 сортируются и встают на места чётных, нечётные 5, 3, 7 не двигаются.
    private static void sortsOnlyEvenNumbers(EvenValuesSorter sorter) {
        sortNumbers(sorter, "только чётные", numbers(5, 8, 3, 2, 7, 6), "5 2 3 6 7 8");
    }

    private static void allOddStayInPlace(EvenValuesSorter sorter) {
        sortNumbers(sorter, "все нечётные — без изменений", numbers(9, 1, 7, 3), "9 1 7 3");
    }

    private static void allEvenAreFullySorted(EvenValuesSorter sorter) {
        sortNumbers(sorter, "все чётные — полная сортировка", numbers(8, 2, 6, 4), "2 4 6 8");
    }

    private static void worksWithNegativeNumbers(EvenValuesSorter sorter) {
        sortNumbers(sorter, "отрицательные и ноль", numbers(4, -3, -2, 1, 0), "-2 -3 0 1 4");
    }

    private static void emptyListDoesNotFail(EvenValuesSorter sorter) {
        sortNumbers(sorter, "пустой список", numbers(), "");
    }
    /// Мощности: 250 (чёт), 91 (нечёт), 120 (чёт), 75 (нечёт), 190 (чёт).
    private static void sortsCarsByEvenPower(EvenValuesSorter sorter) {
        CustomList<Automobile> cars = new CustomList<>();
        cars.add(car("BMW", 250, 2020));
        cars.add(car("Lada", 91, 2015));
        cars.add(car("Kia", 120, 2018));
        cars.add(car("Fiat", 75, 2011));
        cars.add(car("Audi", 190, 2012));
        String before = models(cars, Automobile::getPower);
        sorter.sort(cars, Automobile::getPower);
        check("машины по чётной мощности", before, models(cars, Automobile::getPower),
                "Kia(120) Lada(91) Audi(190) Fiat(75) BMW(250)");
    }
    /// Годы: 2020 (чёт), 2015 (нечёт), 2012 (чёт), 2018 (чёт).
    private static void sortsCarsByEvenYear(EvenValuesSorter sorter) {
        CustomList<Automobile> cars = new CustomList<>();
        cars.add(car("BMW", 250, 2020));
        cars.add(car("Lada", 91, 2015));
        cars.add(car("Audi", 190, 2012));
        cars.add(car("Kia", 120, 2018));
        String before = models(cars, Automobile::getYear);
        sorter.sort(cars, Automobile::getYear);
        check("машины по чётному году", before, models(cars, Automobile::getYear),
                "Audi(2012) Lada(2015) Kia(2018) BMW(2020)");
    }
    /// Сортирует чётные числа и сверяет результат.
    private static void sortNumbers(EvenValuesSorter sorter, String name, CustomList<Integer> nums, String expected) {
        String before = text(nums);
        sorter.sort(nums, Integer::intValue);
        check(name, before, text(nums), expected);
    }
}