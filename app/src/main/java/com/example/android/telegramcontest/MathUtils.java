package com.example.android.telegramcontest;

public class MathUtils {

    public static float clamp (float num, float max, float min) {
        if (num < min) return min;
        if (num > max) return max;
        return num;
    }

    public static long getMax(long[][] array) {
        long max = array[0][0];

        for (int i = 0; i < array.length; i++) {
            for (int j = 0; j < array[i].length; j++) {
                if (array[i][j] > max) {
                    max = array[i][j];
                }
            }
        }
        return max;
    }

    public static long getMin(long[][] array) {
        long min = array[0][0];

        for (int i = 0; i < array.length; i++) {
            for (int j = 0; j < array[i].length; j++) {
                if (array[i][j] < min) {
                    min = array[i][j];
                }
            }
        }
        return min;
    }


    public static long getMax(long[] array) {
        long max = array[0];

        for (int i = 0; i < array.length; i++)
            if (array[i] > max) {
                max = array[i];
            }
        return max;
    }

    public static long getMin(long[] array) {
        long min = array[0];

        for (int i = 0; i < array.length; i++) {
            if (array[i] < min) {
                min = array[i];
            }
        }
        return min;
    }

    public static long[] removeFirst (long[] array) {
        long[] removed = new long[array.length - 1];
        for (int i = 0, j = 1; i < removed.length; i++, j++) {
            removed[i] = array[j];
        }
        return removed;
    }

    public static long[] removeLast (long[] array) {
        long[] removed = new long[array.length - 1];
        for (int i = 0; i < removed.length; i++) {
            removed[i] = array[i];
        }
        return removed;
    }


    public static long nearestSixDivider(long num) {
        if (num % 6 == 0)
            return (num + 6);
        return (num + (6 - num % 6));
    }
}
