package aston.ui;

import aston.core.Automobile;
import aston.core.AutomobileComparators;
import aston.io.AutomobileFileWriter;
import aston.io.FillArray;
import aston.sort.*;
import aston.util.ParallelOccurrenceCounter;
import aston.stream.StreamDataFiller;
import aston.util.CustomList;
import java.io.IOException;
import java.util.Comparator;
import java.util.Scanner;

public class UserInterface {

    private static final Scanner scanner = new Scanner(System.in);
    private static boolean isFinish = true;
    private static int targetCapacity = 10;

    public static void menu() {

        while (isFinish) {
            CustomList<Automobile> customList = new CustomList<>();

            int chooseUser;
            while (true) {
                System.out.print("Выберите длину списка (0 - базовая на 10 элементов, 1 - 50, 2 - 100, 3 - 500, 4 - 1000): ");
                chooseUser = readDataFromUser();

                if (setLengthList(chooseUser)) {
                    System.out.println("Новая целевая длина установлена: " + targetCapacity + "\n");
                    break;
                }
            }

            String typeFill;
            while (true) {
                System.out.print("Выберите вариант заполнения списка данными (1 - из файла, 2 - рандомно, 3 - вручную): ");
                chooseUser = readDataFromUser();

                if (chooseUser == 1) {
                    typeFill = "from file";
                    break;
                } else if (chooseUser == 2) {
                    typeFill = "random values";
                    break;
                } else if (chooseUser == 3){
                    typeFill = "handle input";
                    break;
                } else {
                    System.out.println("Такого варианта заполнения не существует. Повторите попытку....");
                }
            }

            try {
                customList = fillArray(typeFill, customList);
            } catch (IllegalArgumentException e) {
                System.out.println("\n[ОШИБКА ДАННЫХ]: " + e.getMessage());
                System.out.println("Возврат в главное меню...\n");
                continue;
            } catch (IOException e) {
                System.out.println("\n[ОШИБКА ФАЙЛА]: не удалось прочитать файл: " + e.getMessage());
                System.out.println("Возврат в главное меню...\n");
                continue;
            }

            while (true) {
                System.out.print("Хотите вывести исходный список? (1 - да, 2 - нет): ");
                chooseUser = readDataFromUser();

                if (chooseUser == 1) {
                    outputList(customList);
                    break;
                } else if (chooseUser != 2) {
                    System.out.println("Некорректный ввод. Вывод списка пропущен.");
                    break;
                } else {
                    System.out.println("Некорректный ввод. Пожалуйста, выберите 1 или 2!");
                }
            }

            while (true) {
                System.out.print("Выберите вариант сортировки данных списка (1 - пузырьком, 2 - вставками, 3 - по чётным значениям полей): ");
                int typeSort = readDataFromUser();

                if (typeSort >= 1 && typeSort <= 3) {
                    sortList(customList, typeSort);
                    break;
                } else {
                    System.out.println("Такого варианта сортировки не существует. Повторите попытку");
                }
            }

            System.out.println("\nСписок после сортировки:");
            outputList(customList);

            while (true) {
                System.out.print("\nХотите сохранить этот отсортированный список в файл? (1 - да, 2 - нет): ");
                chooseUser = readDataFromUser();

                if (chooseUser == 1) {
                    System.out.print("Введите путь или имя файла для сохранения (например, output.csv): ");
                    String fileName = scanner.nextLine().trim();

                    if (fileName.isEmpty()) {
                        System.out.println("[ОШИБКА]: Имя файла не может быть пустым. Сохранение отменено.");
                    } else {
                        try {
                            java.nio.file.Path outputPath = java.nio.file.Paths.get(fileName);
                            AutomobileFileWriter.writeAll(outputPath, customList);
                            System.out.println("[УСПЕХ]: Список успешно сохранен в файл: " + fileName);
                        } catch (IOException e) {
                            System.out.println("[ОШИБКА ЗАПИСИ]: Не удалось сохранить данные: " + e.getMessage());
                        }
                    }
                } else if (chooseUser == 2) {
                    break;
                } else {
                    System.out.println("Некорректный ввод. Пожалуйста, выберите 1 или 2.");
                }
            }

            if (!customList.isEmpty()) {

                while (true) {

                    System.out.print("\nПосчитать, сколько раз элемент встречается в списке (многопоточно)? (1 - да, 2 - нет): ");
                    chooseUser = readDataFromUser();

                    if (chooseUser == 1) {
                        int index;

                        while (true) {
                            System.out.print("Введите номер элемента (1-" + customList.size() + "): ");
                            index = readDataFromUser();
                            if (index >= 1 && index <= customList.size()) {
                                break;
                            }

                            System.out.println("Нет элемента с таким номером. Повторите попытку.");
                        }

                        Automobile target = customList.get(index - 1);
                        ParallelOccurrenceCounter.countOccurrences(customList, target, 4);
                    }
                }
            }

            isFinish = exitProgram();

            if (isFinish) {
                for (int i = 0; i < 50; i++) {
                    System.out.println();
                }
            }
        }
    }

    private static boolean exitProgram() {

        while (true) {

            System.out.print("\nВернуться в меню или завершить работу? (1 - в меню, 2 - выйти): ");
            int choose = readDataFromUser();

            if (choose == 1) {
                return true;
            } else if (choose == 2) {
                System.out.print("Введите слово exit для подтверждения выхода: ");
                String closeProgram = scanner.nextLine().trim();

                if ("exit".equalsIgnoreCase(closeProgram)) {
                    System.out.println("Программа завершена.");
                    return false;
                } else {
                    System.out.println("Ошибка: введено не 'exit'. Возврат к выбору действия.");
                }
            } else {
                System.out.println("Некорректный выбор. Пожалуйста, введите 1 или 2.");
            }
        }
    }

    private static void outputList(CustomList<Automobile> customList) {
        for (int i = 0; i < customList.size(); i++) {
            System.out.println(customList.get(i));
        }
    }

    private static void sortList(CustomList<Automobile> list, int typeSort) {
        if (list == null || list.isEmpty()) {
            System.out.println("[ПРЕДУПРЕЖДЕНИЕ]: Список пуст. Нечего сортировать.");
            return;
        }

        if (typeSort == 1 || typeSort == 2) {
            SortStrategy strategy = (typeSort == 1) ? new BubbleSort() : new InsertionSort();
            int fieldChoice;

            while (true) {
                System.out.print("Выберите поле для сортировки (1 - модель, 2 - мощность, 3 - год): ");
                fieldChoice = readDataFromUser();

                if (fieldChoice >= 1 && fieldChoice <= 3) {
                    break;
                }

                System.out.println("Неверный выбор поля. Повторите попытку!");
            }

            Comparator<Automobile> comparator = switch (fieldChoice) {
                case 1 -> AutomobileComparators.BY_MODEL;
                case 2 -> AutomobileComparators.BY_POWER;
                default -> AutomobileComparators.BY_YEAR;
            };

            Sorter sorter = new Sorter(strategy);
            System.out.println("Запуск: " + sorter.getStrategy().name());
            sorter.sort(list, comparator);

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

            java.util.function.ToIntFunction<Automobile> fieldExtractor;

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

    private static boolean setLengthList(int chooseUser) {

        return switch (chooseUser) {
            case 0 -> {
                targetCapacity = 10;
                yield true;
            }
            case 1 -> {
                targetCapacity = 50;
                yield true;
            }
            case 2 -> {
                targetCapacity = 100;
                yield true;
            }
            case 3 -> {
                targetCapacity = 500;
                yield true;
            }
            case 4 -> {
                targetCapacity = 1000;
                yield true;
            }
            default -> {
                System.out.println("[ОШИБКА]: Несуществующий пункт длины! Доступны только варианты от 0 до 4. Повторите попытку...");
                yield false;
            }
        };
    }

    private static CustomList<Automobile> fillArray(String typeFill, CustomList<Automobile> customList) throws IOException {

        int currentYear = java.time.Year.now().getValue();
        int nextYear = currentYear + 1;

        switch (typeFill) {
            case "from file":
                System.out.println("Заполнение списка данными из файла...");

                CustomList<Automobile> allAutomobiles = FillArray.fillFromFile(new CustomList<>());

                int limit = Math.min(allAutomobiles.size(), targetCapacity);
                for (int i = 0; i < limit; i++) {
                    Automobile auto = allAutomobiles.get(i);

                    if (auto != null) {
                        customList.add(auto);
                    }
                }

                System.out.println("Из файла прочитано записей: " + allAutomobiles.size());
                System.out.println("Добавлено: " + limit + " (лимит: " + targetCapacity + ")");
                System.out.println("Заполнение списка завершено!");
                break;

            case "random values":
                System.out.println("Заполнение списка рандомными значениями через StreamDataFiller...");
                customList = StreamDataFiller.fillCustomList(targetCapacity);
                System.out.println("Заполнение списка завершено!");
                break;

            case "handle input":

                System.out.println("Заполнение списка вручную. Требуется ввести элементов: " + targetCapacity);

                for (int i = 0; i < targetCapacity; i++) {
                    System.out.println("\n--- Автомобиль №" + (i + 1) + " ---");

                    String model;
                    while (true) {
                        System.out.print("Введите модель: ");
                        model = scanner.nextLine().trim();

                        if (model.isEmpty()) {
                            System.out.println("[ОШИБКА]: Модель автомобиля не может быть пустой. Попробуйте снова.");
                            continue;
                        }

                        if (model.startsWith("#") || model.contains(";")) {
                            System.out.println("[ОШИБКА]: Модель содержит запрещенные символы (# или ;). Попробуйте снова.");
                            continue;
                        }

                        break;
                    }

                    int power;
                    while (true) {
                        System.out.print("Введите мощность (целое число): ");
                        power = readDataFromUser();
                        if (power > 0) {
                            break;
                        }
                        System.out.println("[ОШИБКА]: Мощность двигателя должна быть больше 0. Попробуйте снова.");
                    }

                    int year;

                    while (true) {
                        System.out.print("Введите год производства: ");
                        year = readDataFromUser();
                        if (year >= 1900 && year <= nextYear) {
                            break;
                        }
                        System.out.println("[ОШИБКА]: Год должен быть в диапазоне от 1900 до " + nextYear + ". Попробуйте снова.");
                    }

                    Automobile auto = new Automobile.Builder()
                            .model(model)
                            .power(power)
                            .year(year)
                            .build();

                    customList.add(auto);
                }
                System.out.println("\nРучной ввод успешно завершен!");
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