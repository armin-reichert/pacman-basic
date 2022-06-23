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

import java.util.List;
import java.util.Optional;
import java.util.function.Function;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import de.amr.games.pacman.model.common.GameVariant;

/**
 * @author Armin Reichert
 */
public abstract class OptionParser {

	private static final Logger logger = LogManager.getFormatterLogger();

	//@formatter:off
	public static final String OPT_VARIANT_MSPACMAN = "-mspacman";
	public static final String OPT_VARIANT_PACMAN   = "-pacman";
	//@formatter:on

	public static GameVariant convertGameVariant(String s) {
		return switch (s) {
		case OPT_VARIANT_MSPACMAN -> GameVariant.MS_PACMAN;
		case OPT_VARIANT_PACMAN -> GameVariant.PACMAN;
		default -> null;
		};
	}

	private final List<String> names;
	private final List<String> args;
	private int i;

	protected OptionParser(List<String> names, List<String> args) {
		this.names = names;
		this.args = args;
	}

	protected boolean hasMoreArgs() {
		return i < args.size();
	}

	protected void printOptions() {
	}

	protected <T> Optional<T> parseNameValue(String name, Function<String, T> fnConvert) {
		if (i + 1 < args.size()) {
			var arg1 = args.get(i);
			var arg2 = args.get(i + 1);
			if (name.equals(arg1)) {
				++i;
				if (names.contains(arg2)) {
					logger.error("Missing value for parameter '%s'.", name);
				} else {
					++i;
					try {
						T value = fnConvert.apply(arg2);
						logger.info("Found parameter %s = %s", name, value);
						return Optional.ofNullable(value);
					} catch (Exception x) {
						logger.error("'%s' is no legal value for parameter '%s'.", arg2, name);
					}
				}
			}
		}
		return Optional.empty();
	}

	protected <T> Optional<T> parseName(String name, Function<String, T> fnConvert) {
		if (i < args.size()) {
			var arg = args.get(i);
			if (name.equals(arg)) {
				logger.info("Found parameter %s", name);
				++i;
				try {
					T value = fnConvert.apply(name);
					return Optional.ofNullable(value);
				} catch (Exception x) {
					logger.error("'%s' is no legal parameter.", name);
				}
			}
		}
		return Optional.empty();
	}

	protected Optional<Boolean> parseBoolean(String name) {
		if (i < args.size()) {
			var arg = args.get(i);
			if (name.equals(arg)) {
				++i;
				logger.info("Found parameter %s", name);
				return Optional.of(true);
			}
		}
		return Optional.empty();
	}
}