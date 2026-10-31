package aston.ui;

import aston.core.Automobile;
import aston.sort.BubbleSort;
import aston.sort.EvenValuesSorter;
import aston.sort.InsertionSort;
import aston.sort.SortStrategy;
import aston.util.CustomList;
import aston.stream.StreamDataFiller;
import java.io.IOException;
import java.util.Comparator;
import java.util.Scanner;
import java.util.function.ToIntFunction;

import static aston.io.FillArray.fiilFromFile;

public class UserInterface {

    private static final Scanner scanner = new Scanner(System.in);
    private static boolean isFinish = true;
    private static String typeFiil;
    private static int chooseUser, typeSort;
    private static int targetCapacity = 10;

    public static void menu() throws IOException, InterruptedException {

        while (isFinish) {
            CustomList<Automobile> customList = new CustomList<>();

            System.out.print("Выберите длину списка (0 - базовая на 10 элементов, 1 - 50, 2 - 100, 3 - 500, 4 - 1000): ");
            chooseUser = readDataFromUser();
            setLengthList(customList, chooseUser);

            while (true) {
                System.out.print("Выберите вариант заполнения списка данными (1 - из файла, 2 - рандомно, 3 - вручную): ");
                chooseUser = readDataFromUser();

                if (chooseUser == 1) {
                    typeFiil = "from file";
                    break;
                } else if (chooseUser == 2) {
                    typeFiil = "random values";
                    break;
                } else if (chooseUser == 3){
                    typeFiil = "handle input";
                    break;
                } else {
                    System.out.println("Такого варианта заполнения не существует. Повторите попытку....");
                }
            }

            try {
                customList = fillArray(typeFiil, customList);
            } catch (IllegalArgumentException e) {
                System.out.println("\n[ОШИБКА ДАННЫХ]: " + e.getMessage());
                System.out.println("Возврат в главное меню...\n");
                continue;
            }

            System.out.print("Хотите вывести исходный список? (1 - да, 2 - нет): ");
            chooseUser = readDataFromUser();

            if (chooseUser == 1) {
                outputList(customList);
            } else if (chooseUser != 2) {
                System.out.println("Некорректный ввод. Вывод списка пропущен.");
            }

            while (true) {
                System.out.print("Выберите вариант сортировки данных списка (1 - пузырьком, 2 - вставками, 3 - по чётным значениям полей): ");
                typeSort = readDataFromUser();

                if (typeSort >= 1 && typeSort <= 3) {
                    sortList(customList, typeSort);
                    break;
                } else {
                    System.out.println("Такого варианта сортировки не существует. Повторите попытку");
                }
            }

            System.out.println("\nСписок после сортировки:");
            outputList(customList);

            isFinish = exitProgram();

            if (isFinish) {
                for (int i = 0; i < 50; i++) {
                    System.out.println();
                }
            }
        }
    }

    private static boolean exitProgram() {
        System.out.print("Введите слово exit для завершения программы: ");
        String closeProgram = scanner.nextLine().trim();
        return !closeProgram.contains("exit");
    }

    private static void outputList(CustomList<Automobile> customList) {
        for (int i = 0; i < customList.size(); i++) {
            System.out.println(customList.get(i));
        }
    }

    private static void sortList(CustomList<?> customList, int typeSort) {
        @SuppressWarnings("unchecked")
        CustomList<Automobile> list = (CustomList<Automobile>) customList;

        if (typeSort == 1 || typeSort == 2) {
            SortStrategy strategy = (typeSort == 1) ? new BubbleSort() : new InsertionSort();
            int fieldChoise;

            while (true) {
                System.out.print("Выберите поле для сортировки (1 - модель, 2 - мощность, 3 - год): ");
                fieldChoise = readDataFromUser();
                if (fieldChoise >= 1 && fieldChoise <= 3) {
                    break;
                }
                System.out.println("Неверный выбор поля. Повторите попытку!");
            }

            Comparator<Automobile> comparator = null;

            switch (fieldChoise) {
                case 1:
                    comparator = Comparator.comparing(Automobile::getModel);
                    break;
                case 2:
                    comparator = Comparator.comparingInt(Automobile::getPower);
                    break;
                case 3:
                    comparator = Comparator.comparingInt(Automobile::getYear);
                    break;
            }

            System.out.println("Запуск: " + strategy.name());
            strategy.sort(list, comparator);

        } else if (typeSort == 3) {
            System.out.println("Выбрана сортировка чётных значений числового поля!");
            SortStrategy internalStrategy;

            while (true) {
                System.out.print("Каким алгоритмом сортировать чётные элементы? (1 - пузырьком, 2 - вставками): ");
                int strategyChoice = readDataFromUser();

                if (strategyChoice == 1) {
                    internalStrategy = new BubbleSort();
                    break;
                } else if (strategyChoice == 2) {
                    internalStrategy = new InsertionSort();
                    break;
                }

                System.out.println("Неверный выбор алгоритма. Повторите попытку.");
            }

            ToIntFunction<Automobile> fieldExtractor;

            while (true) {
                System.out.print("Выберите числовое поле (1 - мощность, 2 - год производства): ");
                int fieldChoice = readDataFromUser();

                if (fieldChoice == 1) {
                    fieldExtractor = Automobile::getPower;
                    break;
                } else if (fieldChoice == 2) {
                    fieldExtractor = Automobile::getYear;
                    break;
                }

                System.out.println("Неверный выбор поля. Модель не является числовым полем! Повторите попытку.");
            }

            EvenValuesSorter evenSorter = new EvenValuesSorter(internalStrategy);
            System.out.println("Запуск сортировки чётных значений с использованием алгоритма: " + internalStrategy.name());
            evenSorter.sort(list, fieldExtractor);
        }

        System.out.println("Сортировка успешно завершена!");
    }

    private static CustomList<Automobile> setLengthList(CustomList<Automobile> customList, int chooseUser) {

        if (chooseUser == 0) {
            targetCapacity = 10;
        } else if (chooseUser == 1) {
            targetCapacity = 50;
        } else if (chooseUser == 2) {
            targetCapacity = 100;
        } else if (chooseUser == 3) {
            targetCapacity = 500;
        } else if (chooseUser == 4) {
            targetCapacity = 1000;
        }

        return customList;
    }

    @SuppressWarnings("unchecked")
    private static CustomList<Automobile> fillArray(String typeFill, CustomList<Automobile> customList) throws IOException, InterruptedException {

        switch (typeFill) {
            case "from file":
                System.out.println("Заполнение списка данными из файла...");
                customList = (CustomList<Automobile>) fiilFromFile(customList);
                Thread.sleep(1500);
                System.out.println("Заполнение списка завершено!");
                break;

            case "random values":
                System.out.println("Заполнение списка рандомными значениями через StreamDataFiller...");
                customList = StreamDataFiller.fillCustomList(targetCapacity);
                Thread.sleep(1500);
                System.out.println("Заполнение списка завершено!");
                break;

            case "handle input":
                System.out.println("Заполнение списка вручную. Требуется ввести элементов: " + targetCapacity);
                for (int i = 0; i < targetCapacity; i++) {
                    System.out.println("\n--- Автомобиль №" + (i + 1) + " ---");

                    System.out.print("Введите модель: ");
                    String model = scanner.nextLine().trim();

                    System.out.print("Введите мощность (целое число): ");
                    int power = readDataFromUser();

                    System.out.print("Введите год производства: ");
                    int year = readDataFromUser();

                    Automobile auto = new Automobile.Builder()
                            .model(model)
                            .power(power)
                            .year(year)
                            .build();

                    customList.add(auto);
                }
                System.out.println("Ручной ввод успешно завершен!");
                break;

            default:
                System.out.println("Такого варианта заполнения не существует. Повторите попытку...");
                break;
        }
        return customList;
    }

    private static int readDataFromUser() {
        while (true) {
            try {
                return Integer.parseInt(scanner.nextLine().trim());
            } catch (NumberFormatException e) {
                System.out.println("Ошибка: введено не число. Пожалуйста, повторите попытку!");
            }
        }
    }
}