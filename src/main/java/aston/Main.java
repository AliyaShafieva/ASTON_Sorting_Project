package aston;

import aston.util.CustomList;
import java.io.IOException;
import java.util.Scanner;

import static aston.io.FillArray.*;

public class Main {

    private static final Scanner scanner = new Scanner(System.in);
    private static boolean isFinish = true;
    private static CustomList customList = new CustomList();

    static void main() throws IOException, InterruptedException {

        String typeFill;
        int chooseUser, typeSort;

        while (isFinish) {

            System.out.print("Выберите длину списка (0 - базовая на 10 элементов, 1 - 50, 2 - 100, 3 - 500, 4 - 1000): ");
            chooseUser = scanner.nextInt();
            setLengthList(customList, chooseUser);

            System.out.print("Выберите вариант заполнения списка данными (1 - из файла, 2 - рандомно, 3 - вручную): ");
            chooseUser = scanner.nextInt();

            if (chooseUser == 1) {
                typeFill = "from file";
            } else if (chooseUser == 2) {
                typeFill = "random value";
            } else {
                typeFill = "handle input";
            }

            fillArray(typeFill, customList);

            System.out.print("Хотите вывести исходный список? (1 - да, 2 - нет): ");
            chooseUser = scanner.nextInt();

            if (chooseUser == 1) {
                outputList(customList);
            }
            else {
                System.out.print("Выберите вариант сортировки данных списка (1 - пузырьком, 2 - вставками, 3 - по чётным значениям полей): ");
                typeSort = scanner.nextInt();
                sortList(typeSort);
            }

            isFinish = exitProgram();

            if (isFinish) {
                for (int i = 0; i < 50; i++) {
                    System.out.println();
                }
            }
        }
    }

    public static CustomList fillArray(String typeFill, CustomList customList) throws IOException, InterruptedException {

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

    public static void outputList(CustomList customList) {
        for (int i = 0; i < customList.size(); i++) {
            customList.get(i);
        }
    }

    public static void sortList(int typeSort) {

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

    public static boolean exitProgram() {
        System.out.print("Введите слово exit для завершения программы: ");
        String closeProgram = scanner.next();

        if (closeProgram.contains("exit")) {
            return false;
        } else {
            return true;
        }
    }

    public static CustomList setLengthList(CustomList customList, int chooseUser) {

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
}