package aston.ui;

import aston.util.CustomList;
import java.io.IOException;
import java.util.InputMismatchException;
import java.util.Scanner;

import static aston.io.FillArray.fiilFromFile;

public class UserInterface {

    private static final Scanner scanner = new Scanner(System.in);
    private static boolean isFinish = true;
    private static String typeFiil;
    private static int chooseUser, typeSort;

    public static void menu() throws IOException, InterruptedException {

        CustomList customList = new CustomList();

        while (isFinish) {

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

            fillArray(typeFiil, customList);

            System.out.print("Хотите вывести исходный список? (1 - да, 2 - нет): ");
            chooseUser = readDataFromUser();

            if (chooseUser == 1) {
                outputList(customList);
            } else if (chooseUser != 2) {
                System.out.println("Неккоректный ввод. Вывод списка пропущен.");
            }

            while (true) {
                System.out.print("Выберите вариант сортировки данных списка (1 - пузырьком, 2 - вставками, 3 - по чётным значениям полей): ");
                typeSort = readDataFromUser();

                if (typeSort >= 1 && typeSort <= 3) {
                    sortList(typeSort);
                    break;
                } else {
                    System.out.println("Такого варианта сортировки не существует. Повторите попытку");
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
        System.out.print("Введите слово exit для завершения программы: ");
        String closeProgram = scanner.nextLine().trim();

        return closeProgram.contains("exit") ? false : true;
    }

    private static void outputList(CustomList customList) {
        for (int i = 0; i < customList.size(); i++) {
            System.out.println(customList.get(i));
        }
    }

    private static void sortList(int typeSort) {

        switch (typeSort) {
            case 1:
                /// сортировка пузырьком
                break;

            case 2:
                ///  сортировка вставками
                break;

            case 3:
                /// сортировка по чётным значениям числового поля
                break;

            default:
                System.out.println("Такого варианта сортировка не существует. Повторите попытку...");
                break;
        }
    }

    private static CustomList setLengthList(CustomList customList, int chooseUser) {

        if (chooseUser == 1) {
            /// увеличение списка на 50 элементов
        } else if (chooseUser == 2) {
            /// увеличение списка на 100 элементов
        } else if (chooseUser == 3) {
            /// увеличение списка на 500 элементов
        } else {
            /// увеличение списка на 1000 элементов
        }

        return customList;
    }

    private static CustomList fillArray(String typeFill, CustomList customList) throws IOException, InterruptedException {

        switch (typeFill) {

            case "from file":
                System.out.println("Заполнение списка данными из файла...");
                customList = fiilFromFile(customList);
                Thread.sleep(3000);
                System.out.println("Заполнение списка завершено!");
                break;

            case "random values":
                System.out.println("Заполнение списка рандомными значениями...");
                Thread.sleep(3000);
                System.out.println("Заполнение списка завершено!");
                break;

            case "handle input":
                /// заполнение списка вручную
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
            } catch (InputMismatchException e) {
                System.out.println("Ошибка: введено не число. Пожалуйста, повторите попытку!");
            }
        }
    }
}