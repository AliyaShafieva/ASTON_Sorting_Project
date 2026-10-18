package aston.sort;

import aston.core.Automobile;
import aston.core.AutomobileComparators;
import aston.util.CustomList;
import static aston.sort.SortTestHelper.*;

public class SortStrategyTest {

    public static void main(String[] args) {

        reset();

        SortStrategy[] strategies = {new InsertionSort(), new BubbleSort()};

        for (SortStrategy strategy : strategies) {
            System.out.println("=== " + strategy.name() + " ===");
            sortsByPower(strategy);
            sortsByModel(strategy);
            sortsByYear(strategy);
            keepsOrderOfEqualElements(strategy);
            sortsNumbersWithDuplicates(strategy);
            sortsAlreadySortedList(strategy);
            sortsReversedList(strategy);
            sortsMoreThanTenElements(strategy);
            emptyListDoesNotFail(strategy);
            singleElementStaysInPlace(strategy);
            System.out.println();
        }

        printTotal();
    }

    // ---------- автомобили ----------

    private static void sortsByPower(SortStrategy strategy) {
        CustomList<Automobile> cars = cars();
        String before = models(cars, Automobile::getPower);
        strategy.sort(cars, AutomobileComparators.BY_POWER);
        check("по мощности", before, models(cars, Automobile::getPower),
                "Lada(90) Kia(120) Audi(190) BMW(250)");
    }

    private static void sortsByModel(SortStrategy strategy) {
        CustomList<Automobile> cars = cars();
        String before = models(cars);
        strategy.sort(cars, AutomobileComparators.BY_MODEL);
        check("по модели", before, models(cars), "Audi BMW Kia Lada");
    }

    private static void sortsByYear(SortStrategy strategy) {
        CustomList<Automobile> cars = cars();
        String before = models(cars, Automobile::getYear);
        strategy.sort(cars, AutomobileComparators.BY_YEAR);
        check("по году", before, models(cars, Automobile::getYear),
                "Audi(2012) Lada(2015) Kia(2018) BMW(2020)");
    }
    /// Opel и Kia с одинаковой мощностью 120 должны остаться в исходном порядке.
    private static void keepsOrderOfEqualElements(SortStrategy strategy) {
        CustomList<Automobile> cars = new CustomList<>();
        cars.add(car("Opel", 120, 2010));
        cars.add(car("BMW", 250, 2020));
        cars.add(car("Kia", 120, 2018));
        cars.add(car("Lada", 90, 2015));
        String before = models(cars, Automobile::getPower);
        strategy.sort(cars, AutomobileComparators.BY_POWER);
        check("равные остаются по порядку", before, models(cars, Automobile::getPower),
                "Lada(90) Opel(120) Kia(120) BMW(250)");
    }

    // ---------- числа и граничные случаи ----------

    private static void sortsNumbersWithDuplicates(SortStrategy strategy) {
        sortNumbers(strategy, "числа с повторами", numbers(5, 3, 5, 1, 9, 0), "0 1 3 5 5 9");
    }

    private static void sortsAlreadySortedList(SortStrategy strategy) {
        sortNumbers(strategy, "уже отсортированный", numbers(1, 2, 3, 4, 5), "1 2 3 4 5");
    }

    private static void sortsReversedList(SortStrategy strategy) {
        sortNumbers(strategy, "в обратном порядке", numbers(5, 4, 3, 2, 1), "1 2 3 4 5");
    }
    /// Больше 10 элементов: CustomList расширяет внутренний массив.
    private static void sortsMoreThanTenElements(SortStrategy strategy) {
        sortNumbers(strategy, "15 элементов",
                numbers(15, 14, 13, 12, 11, 10, 9, 8, 7, 6, 5, 4, 3, 2, 1),
                "1 2 3 4 5 6 7 8 9 10 11 12 13 14 15");
    }

    private static void emptyListDoesNotFail(SortStrategy strategy) {
        sortNumbers(strategy, "пустой список", numbers(), "");
    }

    private static void singleElementStaysInPlace(SortStrategy strategy) {
        sortNumbers(strategy, "один элемент", numbers(42), "42");
    }

    // ---------- данные и общий шаг ----------

    /// Сортирует числа по возрастанию и сверяет результат.
    private static void sortNumbers(SortStrategy strategy, String name, CustomList<Integer> nums, String expected) {
        String before = text(nums);
        strategy.sort(nums, Integer::compare);
        check(name, before, text(nums), expected);
    }
    /// Четыре машины - общие данные для тестов по полям.
    private static CustomList<Automobile> cars() {
        CustomList<Automobile> cars = new CustomList<>();
        cars.add(car("Lada", 90, 2015));
        cars.add(car("BMW", 250, 2020));
        cars.add(car("Audi", 190, 2012));
        cars.add(car("Kia", 120, 2018));
        return cars;
    }
}