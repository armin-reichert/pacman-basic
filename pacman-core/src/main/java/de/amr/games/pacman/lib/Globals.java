/*
MIT License

Copyright (c) 2023 Armin Reichert

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

import de.amr.games.pacman.lib.math.Vector2f;
import de.amr.games.pacman.lib.math.Vector2i;

/**
 * @author Armin Reichert
 */
public class Globals {

	/** Tile size (8px). */
	public static final int TS = 8;

	/** Half tile size (4px). */
	public static final int HTS = 4;

	public static Vector2i v2i(int x, int y) {
		return new Vector2i(x, y);
	}

	public static Vector2f v2f(double x, double y) {
		return new Vector2f((float) x, (float) y);
	}

	public static final Random RND = new Random();

	/**
	 * @param a left interval bound
	 * @param b right interval bound
	 * @return Random integer number from right-open interval <code>[a; b[</code>. Interval bounds are rearranged to
	 *         guarantee <code>a<=b</code>
	 */
	public static int randomInt(int a, int b) {
		if (a > b) {
			var tmp = a;
			a = b;
			b = tmp;
		}
		return a + RND.nextInt(b - a);
	}

	/**
	 * @param a left interval bound
	 * @param b right interval bound
	 * @return Random floating-point number from right-open interval <code>[a; b[</code>. Interval bounds are rearranged
	 *         to guarantee <code>a<=b</code>
	 */
	public static float randomFloat(float a, float b) {
		if (a > b) {
			var tmp = a;
			a = b;
			b = tmp;
		}
		return a + RND.nextFloat(b - a);
	}

	/**
	 * @param a left interval bound
	 * @param b right interval bound
	 * @return Random double-precision floating-point number from right-open interval <code>[a; b[</code>. Interval bounds
	 *         are rearranged to guarantee <code>a<=b</code>
	 */
	public static double randomDouble(double a, double b) {
		if (a > b) {
			var tmp = a;
			a = b;
			b = tmp;
		}
		return a + RND.nextDouble(b - a);
	}

	public static boolean inPercentOfCases(int percent) {
		if (percent < 0 || percent > 100) {
			throw new IllegalArgumentException("Percent value must be in range [0, 100] but is %d".formatted(percent));
		}
		if (percent == 0) {
			return false;
		}
		if (percent == 100) {
			return true;
		}
		return randomInt(0, 100) < percent;
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

	@SafeVarargs
	public static <T> boolean oneOf(T value, T... alternatives) {
		return switch (alternatives.length) {
		case 0 -> false;
		case 1 -> value.equals(alternatives[0]);
		default -> Stream.of(alternatives).anyMatch(value::equals);
		};
	}
}