/*
MIT License

Copyright (c) 2021-2023 Armin Reichert

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.
*/

package de.amr.games.pacman.lib;

import java.util.Arrays;
import java.util.Random;
import java.util.stream.Stream;

/**
 * @author Armin Reichert
 */
public class U {

	private U() {
	}

	public static final Random RND = new Random();

	/**
	 * @param left  left interval bound (included)
	 * @param right right interval bound (excluded)
	 * @return random integer from interval <code>[left; right[</code>
	 */
	public static int randomInt(int left, int right) {
		return left + RND.nextInt(right - left);
	}

	/**
	 * @param left  left interval bound (included)
	 * @param right right interval bound (excluded)
	 * @return random double from interval <code>[left; right[</code>
	 */
	public static double randomDouble(double left, double right) {
		return left + RND.nextDouble() * (right - left);
	}

	public static boolean isEven(int n) {
		return n % 2 == 0;
	}

	public static boolean isOdd(int n) {
		return n % 2 != 0;
	}

	/**
	 * @param value1 value1
	 * @param value2 value2
	 * @param t      "time" between 0 and 1
	 * @return linear interpolation between {@code value1} and {@code value2} values
	 */
	public static double lerp(double value1, double value2, double t) {
		return (1 - t) * value1 + t * value2;
	}

	/**
	 * @param value some value
	 * @param min   lower bound of interval
	 * @param max   upper bound of interval
	 * @return the value if inside the interval, the lower bound if the value is smaller, the upper bound if the value is
	 *         larger
	 */
	public static double clamp(double value, double min, double max) {
		if (value < min) {
			return min;
		}
		if (value > max) {
			return max;
		}
		return value;
	}

	/**
	 * @param value some value
	 * @param min   lower bound of interval
	 * @param max   upper bound of interval
	 * @return the value if inside the interval, the lower bound if the value is smaller, the upper bound if the value is
	 *         larger
	 */
	public static int clamp(int value, int min, int max) {
		if (value < min) {
			return min;
		}
		if (value > max) {
			return max;
		}
		return value;
	}

	/**
	 * @param difference maximum allowed deviation
	 * @param value      value
	 * @param target     target value
	 * @return {@code true} if the given values differ at most by the given difference
	 */
	public static boolean differsAtMost(double difference, double value, double target) {
		if (difference < 0) {
			throw new IllegalArgumentException("Difference must be positive but is %f".formatted(difference));
		}
		return value >= (target - difference) && value <= (target + difference);
	}

	public static byte[][] copyByteArray2D(byte[][] array) {
		return Arrays.stream(array).map(byte[]::clone).toArray(byte[][]::new);
	}

	public static String yesNo(boolean b) {
		return b ? "YES" : "NO";
	}

	public static String onOff(boolean b) {
		return b ? "ON" : "OFF";
	}

	@SafeVarargs
	public static <T> boolean oneOf(T value, T... alternatives) {
		return switch (alternatives.length) {
		case 0 -> false;
		case 1 -> value.equals(alternatives[0]);
		default -> Stream.of(alternatives).anyMatch(value::equals);
		};
	}

}