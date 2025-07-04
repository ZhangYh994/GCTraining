package com.zyh;

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class Training1 {

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        List<Integer> numbers = new ArrayList<>();
        System.out.println("请输入10个整数，用空格分隔:");

        while (true) {
            try {
                String input = scanner.nextLine();
                String[] inputNumbers = input.split("\\s+");

                if (inputNumbers.length != 10) {
                    System.out.println("错误：请输入恰好10个数字。请重新输入:");
                    continue;
                }

                for (String numStr : inputNumbers) {
                    numbers.add(Integer.parseInt(numStr));
                }
                break;

            } catch (NumberFormatException e) {
                System.out.println("错误：输入包含非数字内容。请重新输入10个整数:");
                numbers.clear();
            } catch (Exception e) {
                System.out.println("发生未知错误，请重新输入:");
                numbers.clear();
            }
        }

        quickSort(numbers, 0, numbers.size() - 1);

        System.out.println("排序后的结果:");
        for (int num : numbers) {
            System.out.print(num + " ");
        }
    }

    public static void quickSort(List<Integer> numbers, int left, int right) {
        if (left < right) {
            int pivotIndex = partition(numbers, left, right);
            quickSort(numbers, left, pivotIndex - 1);
            quickSort(numbers, pivotIndex + 1, right);
        }
    }

    private static int partition(List<Integer> numbers, int left, int right) {
        int base = numbers.get(left);

        while (left < right) {
            while (left < right && numbers.get(right) >= base) {
                right--;
            }
            numbers.set(left, numbers.get(right));

            while (left < right && numbers.get(left) <= base) {
                left++;
            }
            numbers.set(right, numbers.get(left));
        }
        numbers.set(left, base);
        return left;
    }
}