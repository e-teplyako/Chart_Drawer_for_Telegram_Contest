package com.teplyakova.april.telegramcontest.Utils;

import android.content.Context;
import android.util.TypedValue;

import com.teplyakova.april.telegramcontest.Data.LineData;

public class MathUtils {

	public static float dpToPixels (float dp, Context context) {
		return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, context.getResources().getDisplayMetrics());
	}

	public static float clamp (float num, float min, float max) {
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

	public static int getMax(int[] array) {
		int max = array[0];

		for (int i = 0; i < array.length; i++)
			if (array[i] > max) {
				max = array[i];
			}
		return max;
	}

	public static int getMin(int[] array) {
		int min = array[0];

		for (int i = 0; i < array.length; i++) {
			if (array[i] < min) {
				min = array[i];
			}
		}
		return min;
	}

	public static int getMax(LineData[] lines) {
		if (lines == null || lines.length == 0)
			return 0;
		int max = lines[0].getPoints()[0];

		for (LineData line : lines) {
			int maxInLine = getMax(line.getPoints());
			if (maxInLine > max)
				max = maxInLine;
		}
		return max;
	}

	public static int getMin(LineData[] lines) {
		if (lines == null || lines.length == 0)
			return 0;

		int min = lines[0].getPoints()[0];

		for (LineData line : lines) {
			int minInLine = getMin(line.getPoints());
			if (minInLine < min)
				min = minInLine;
		}
		return min;
	}

	public static int getLocalMax(int[] array, int minIndex, int maxIndex) {
		int max = array[minIndex];

		for (int i = minIndex; i < maxIndex; i++) {
			if (array[i] > max) {
				max = array[i];
			}
		}
		return max;
	}

	public static int getLocalMin(int[] array, int minIndex, int maxIndex) {
		int min = array[minIndex];

		for (int i = minIndex; i < maxIndex; i++) {
			if (array[i] < min) {
				min = array[i];
			}
		}
		return min;
	}

	public static int getLocalMax(LineData[] lines, int minIndex, int maxIndex) {
		if (lines == null || lines.length == 0)
			return 0;

		int max = lines[0].getPoints()[minIndex];

		for (LineData line : lines) {
			int maxInLine = getLocalMax(line.getPoints(), minIndex, maxIndex);
			if (maxInLine > max)
				max = maxInLine;
		}
		return max;
	}

	public static int getLocalMin(LineData[] lines, int minIndex, int maxIndex) {
		if (lines == null || lines.length == 0)
			return 0;

		int min = lines[0].getPoints()[minIndex];

		for (LineData line : lines) {
			int minInLine = getLocalMin(line.getPoints(), minIndex, maxIndex);
			if (minInLine < min)
				min = minInLine;
		}
		return min;
	}

	public static int getMaxYForStackedChart(LineData[] lines, int minIndex, int maxIndex) {
		if (lines == null || lines.length == 0)
			return 0;
		int oneArrayLength = maxIndex - minIndex + 1;
		int[] wholeArray = new int[oneArrayLength];
		int m = 0;
		for (int i = minIndex; i <= maxIndex; i++) {
			for (int j = 0; j < lines.length; j++) {
				wholeArray[m] = wholeArray[m] + lines[j].getPoints()[i];
			}
			m++;
		}
		int max = getMax(wholeArray);
		return max;
	}

	public static int getMaxIndex(float[] array) {
		if (array.length == 0) {
			return 0;
		}

		float max = array[0];
		int maxIndex = 0;
		for (int i = 1; i < array.length; i++) {
			if (array[i] > max) {
				maxIndex = i;
				max = array[i];
			}
		}
		return maxIndex;
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

	public static float[] concatArraysForDrawing(float[] array1, float[] array2) {
		if ( array1 == null || array2 == null || array1.length < 2 || array2.length < 2)
			return null;
		if (array1.length != array2.length)
			return null;

		int length = array1.length;
		float[] result = new float[4 * length - 4];
		result[0] = array1[0];
		result[1] = array2[0];
		int n = 2;
		for (int i = 1; i < length - 1; i++) {
			for (int j = 0; j < 2; j++) {
				result[n] = array1[i];
				n++;
				result[n] = array2[i];
				n++;
			}
		}
		result[n] = array1[length - 1];
		n++;
		result[n] = array2[length - 1];
		return result;
	}
}
