package aston.core;

import java.util.Comparator;

public final class AutomobileComparators {
    private AutomobileComparators(){}

    public static final Comparator<Automobile> BY_MODEL =
            Comparator.comparing(Automobile::getModel);

    public static final Comparator<Automobile> BY_POWER =
            Comparator.comparingInt(Automobile::getPower);

    public static final Comparator<Automobile> BY_YEAR =
            Comparator.comparingInt(Automobile::getYear);
}
