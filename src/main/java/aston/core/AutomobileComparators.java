package aston.core;

import java.util.Comparator;
/// Готовые правила сравнения автомобилей - по одному на каждое поле.
/// Передаются в алгоритм сортировки, чтобы выбрать, **по какому полю** сортировать:
/// `sorter.sort(cars, AutomobileComparators.BY_MODEL);`
/// Все компараторы упорядочивают по возрастанию.
public final class AutomobileComparators {
    private AutomobileComparators(){}

    public static final Comparator<Automobile> BY_MODEL =
            Comparator.comparing(Automobile::getModel);

    public static final Comparator<Automobile> BY_POWER =
            Comparator.comparingInt(Automobile::getPower);

    public static final Comparator<Automobile> BY_YEAR =
            Comparator.comparingInt(Automobile::getYear);
}
