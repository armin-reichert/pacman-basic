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

import java.util.function.Function;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * @author Armin Reichert
 */
public class Option<T> {

	public static class BooleanOption extends Option<Boolean> {
		public BooleanOption(String name, Boolean defaultValue) {
			super(name, defaultValue, Boolean::valueOf);
		}
	}

	public static class IntegerOption extends Option<Integer> {
		public IntegerOption(String name, Integer defaultValue) {
			super(name, defaultValue, Integer::valueOf);
		}
	}

	public static class DoubleOption extends Option<Double> {
		public DoubleOption(String name, Double defaultValue) {
			super(name, defaultValue, Double::valueOf);
		}
	}

	private static final Logger logger = LogManager.getFormatterLogger();

	private final String name;
	private final Function<String, T> fnValueOf;
	private T value;

	public Option(String name, T defaultValue, Function<String, T> fnValueOf) {
		this.name = name;
		this.value = defaultValue;
		this.fnValueOf = fnValueOf;
	}

	public String getName() {
		return name;
	}

	public T getValue() {
		return value;
	}

	public T valueOf(String s) {
		return fnValueOf.apply(s);
	}

	public void parse(String s) {
		try {
			value = fnValueOf.apply(s);
			logger.info("Found option %s = %s", name, value);
		} catch (Exception e) {
			logger.error("Could not parse option '%s' from text '%s'", name, s);
		}
	}
}