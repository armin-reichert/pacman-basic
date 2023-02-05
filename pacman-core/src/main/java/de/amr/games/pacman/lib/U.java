/*
MIT License

Copyright (c) 2022 Armin Reichert

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
import java.util.stream.Stream;

/**
 * @author Armin Reichert
 */
public class U {

	private U() {
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