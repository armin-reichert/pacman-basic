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

package de.amr.games.pacman.lib.option;

import java.util.function.Function;

import org.tinylog.Logger;

/**
 * @author Armin Reichert
 */
public class Option<T> {

	public static Option<Boolean> booleanOption(String name, boolean defaultValue) {
		return new Option<>(name, defaultValue, Boolean::valueOf);
	}

	public static Option<Integer> integerOption(String name, int defaultValue) {
		return new Option<>(name, defaultValue, Integer::valueOf);
	}

	public static Option<Float> floatOption(String name, float defaultValue) {
		return new Option<>(name, defaultValue, Float::valueOf);
	}

	public static Option<Double> doubleOption(String name, double defaultValue) {
		return new Option<>(name, defaultValue, Double::valueOf);
	}

	public static <X> Option<X> option(String name, X defaultValue, Function<String, X> fnValueOf) {
		return new Option<>(name, defaultValue, fnValueOf);
	}

	private final String name;
	private final Function<String, T> fnValueOf;
	private T value;

	private Option(String name, T defaultValue, Function<String, T> fnValueOf) {
		this.name = name;
		this.value = defaultValue;
		this.fnValueOf = fnValueOf;
	}

	@Override
	public String toString() {
		return "[Option name=%s value=%s]".formatted(name, value);
	}

	public String getName() {
		return name;
	}

	public T getValue() {
		return value;
	}

	public void parse(String s) {
		try {
			value = fnValueOf.apply(s);
			Logger.info("Found option: {} = {}", name, value);
		} catch (Exception e) {
			Logger.error("Could not parse option '{}' from text '{}'", name, s);
		}
	}
}