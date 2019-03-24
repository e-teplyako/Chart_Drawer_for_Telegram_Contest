package com.example.android.telegramcontest.Utils;

import android.content.Context;
import android.util.TypedValue;

import com.example.android.telegramcontest.LineData;

import java.util.ArrayList;

public class MathUtils {

    public static float clamp (float num, float max, float min) {
        if (num < min) return min;
        if (num > max) return max;
        return num;
    }

    public static float lerp (float a, float b, float t) {
        return a + (b - a) * t;
    }

    public static float inverseLerp (float a, float b, float value) {
        return (value - a) / (b - a);
    }

    public static float pixesToDP (float px, Context context) {
        return px / context.getResources().getSystem().getDisplayMetrics().density;
    }

    public static float dpToPixels (float dp, Context context) {
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, context.getResources().getDisplayMetrics());
    }

    public static int getMax(int[][] array) {
        int max = array[0][0];

        for (int i = 0; i < array.length; i++) {
            for (int j = 0; j < array[i].length; j++) {
                if (array[i][j] > max) {
                    max = array[i][j];
                }
            }
        }
        return max;
    }

    public static int getMin(int[][] array) {
        int min = array[0][0];

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

    public static long getMax (LineData[] lines) {
        long max = lines[0].posY[0];

        for (LineData line : lines) {
            long maxInLine = getMax(line.posY);
            if (maxInLine > max)
                max = maxInLine;
        }
        return max;
    }

    public static long getMin (LineData[] lines) {
        long min = lines[0].posY[0];

        for (LineData line : lines) {
            long minInLine = getMin(line.posY);
            if (minInLine < min)
                min = minInLine;
        }
        return min;
    }

    public static long getMaxY (LineData[] lines, int minIndex, int maxIndex) {
        long max = lines[0].posY[minIndex];

        for (LineData line : lines) {
            long maxInLine = getMax(line.posY, minIndex, maxIndex);
            if (maxInLine > max)
                max = maxInLine;
        }
        return max;
    }

    public static long getMin (LineData[] lines, int minIndex, int maxIndex) {
        long min = lines[0].posY[minIndex];

        for (LineData line : lines) {
            long minInLine = getMin(line.posY, minIndex, maxIndex);
            if (minInLine < min)
                min = minInLine;
        }
        return min;
    }

    public static long getMin (long[] array, int minIndex, int maxIndex) {
        long min = array[minIndex];

        for (int i = minIndex; i < maxIndex; i++) {
            if (array[i] < min) {
                min = array[i];
            }
        }
        return min;
    }

    public static long getMax (long[] array, int minIndex, int maxIndex) {
        long max = array[minIndex];

        for (int i = minIndex; i < maxIndex; i++) {
            if (array[i] > max) {
                max = array[i];
            }
        }
        return max;
    }

    public static float getEaseIn (float _t) {
        return getEase(0.42f, 0, 1, 1, _t);
    }

    public static float getEaseOut (float _t) {
        return getEase(0, 0, 0.58f, 1, _t);
    }

    public static float getEase (float m_A, float m_B, float m_C, float m_D, float _t) {
        float A = 1.0f - 3.0f * m_C + 3.0f * m_A;
        float B = 3.0f * m_C - 6.0f * m_A;
        float C = 3.0f * m_A;

        float E = 1.0f - 3.0f * m_D + 3.0f * m_B;
        float F = 3.0f * m_D - 6.0f * m_B;
        float G = 3.0f * m_B;

        float t = _t;
        for (int i=0; i < 5; i++)
        {
            float dt = A * (t * t * t) + B * (t * t) + C * t;

            float s = 3.0f * A * t * t + 2.0f * B * t + C;
            if (Math.abs(s) >= 0.0001f)
                t = clamp(t - (dt - _t) / s, 1, 0);
        }

        return E * (t * t * t) + F * (t * t) + G * t;
    }

    public static int[] add (int[] array, int element) {
        int[] result = new int[array.length + 1];
        for (int i = 0; i < result.length - 1; i++) {
            result[i] = array[i];
        }
        result[result.length - 1] = element;
        return result;
    }

    public static int[] remove (int[] array, int element) {
        int[] result = new int[array.length - 1];
        for (int i = 0, j = 0; i < array.length; i++) {
            if (array[i] != element) {
                result[j] = array[i];
                j++;
            }
        }
        return result;
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

    public static int getIndexOfNearestLeftElement (long[] array, long point) {
        int index = 0;
        long diff = Math.abs(array[0] - point);
        for (int i = 0; i < array.length; i++) {
            if (Math.abs(array[i] - point) < diff) {
                diff = Math.abs(array[i] - point);
                index = i;
            }
        }
        if (array[index] - point > 0)
            return index > 0 ? index - 1 : 0;
        return index;
    }

    public static int getIndexOfNearestRightElement (long[] array, long point) {
        int index = 0;
        long diff = Math.abs(array[0] - point);
        for (int i = 0; i < array.length; i++) {
            if (Math.abs(array[i] - point) < diff) {
                diff = Math.abs(array[i] - point);
                index = i;
            }
        }
        if (array[index] - point < 0)
            return index < array.length - 1 ? index + 1 : array.length - 1;
        return index;
    }

    public static int getIndexOfNearestElement (float[] array, float point) {
        int index = 0;
        float diff = Math.abs(array[0] - point);
        for (int i = 0; i < array.length; i++) {
            if (Math.abs(array[i] - point) < diff) {
                diff = Math.abs(array[i] - point);
                index = i;
            }
        }
        return index;
    }


    public static long getNearestSixDivider(long num) {
        if (num % 6 == 0)
            return (num + 6);
        return (num + (6 - num % 6));
    }

    public static String getFriendlyNumber (long num) {
        String result = "";
        long div = num / 1000;
        if (div < 10)
            return String.valueOf(num);
        else if (div < 1000)
            return String.valueOf(div) + "K";
        else if (div < 100000)
            return String.valueOf(div / 1000) + "M";
        else if (div < 100000000)
            return String.valueOf(div / 1000000) + "B";
        return String.valueOf(num);
    }

    public static String getFriendlyNumber (int num) {
        String result = "";
        int div = num / 1000;
        if (div < 10)
            return String.valueOf(num);
        else if (div < 1000)
            return String.valueOf(div) + "K";
        else if (div < 10000)
            return String.valueOf(div / 1000) + "M";
        return String.valueOf(num);
    }

    public static void optimizePoints(float[] pointsX, float[] pointsY, float tolerance, ArrayList<Float> optimizedPointsX, ArrayList<Float> optimizedPointsY)
    {
        if (optimizedPointsX == null || optimizedPointsY == null)
            return;

        optimizedPointsX.clear();
        optimizedPointsY.clear();

        float sqTolerance = tolerance * tolerance;

        float prevPointX = pointsX[0];
        float prevPointY = pointsY[0];

        float pointX = 0;
        float pointY = 0;

        int arraySize = pointsX.length;

        for (int i = 1; i < arraySize; i++) {
            pointX = pointsX[i];
            pointY = pointsY[i];

            if (getSqDist(pointX, pointY, prevPointX, prevPointY) > sqTolerance) {
                optimizedPointsX.add(pointX);
                optimizedPointsY.add(pointY);

                prevPointX = pointX;
                prevPointY = pointY;
            }
        }

        if (Math.abs(prevPointX - pointX) > 0.001 || Math.abs(prevPointY - pointY) > 0.001)
        {
            optimizedPointsX.add(pointX);
            optimizedPointsY.add(pointY);
        }
    }


    static float getSqDist(float p1X, float p1Y, float p2X, float p2Y) {
        float dx = p1X - p2X;
        float dy = p1Y - p2Y;

        return dx * dx + dy * dy;
    }
}
