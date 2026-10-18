package aston.sort;

public class AllSortTests {

    private static final String BOLD_CYAN = "\u001B[1;36m";
    private static final String RESET = "\u001B[0m";

    public static void main(String[] args) {
        header("Алгоритмы сортировки");
        SortStrategyTest.main(args);

        System.out.println();
        header("Доп. задание 1: сортировка чётных значений");
        EvenValuesSorterTest.main(args);
    }

    private static void header(String title) {
        String line = "═".repeat(title.length() + 4);
        System.out.println(BOLD_CYAN + "╔" + line + "╗");
        System.out.println("║  " + title + "  ║");
        System.out.println("╚" + line + "╝" + RESET);
    }
}
