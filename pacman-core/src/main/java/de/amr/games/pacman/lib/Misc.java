/*
MIT License

Copyright (c) 2021-22 Armin Reichert

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

import java.util.ArrayList;
import java.util.List;

public class Misc {

	/**
	 * @param value     some double value
	 * @param target    target value
	 * @param tolerance maximum allowed deviation
	 * @return {@code true} if the given value is inside the interval {@code [target - tolerance; target + tolerance]}
	 */
	public static boolean differsAtMost(double value, double target, double tolerance) {
		return value >= target - tolerance && value <= target + tolerance;
	}

	/**
	 * Trims given (array) list to its actual size. Lets other lists alone.
	 * 
	 * @param <T>  list entry type
	 * @param list some list
	 * @return trimmed list
	 */
	public static <T> List<T> trim(List<T> list) {
		if (list instanceof ArrayList) {
			((ArrayList<T>) list).trimToSize();
		}
		return list;
	}

}
