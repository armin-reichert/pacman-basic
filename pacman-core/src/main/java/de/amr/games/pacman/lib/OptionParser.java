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

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * @author Armin Reichert
 */
public class OptionParser {

	private static final Logger logger = LogManager.getFormatterLogger();

	private final Map<String, Option<?>> optionMap;
	private int i;

	public OptionParser(Option<?>... options) {
		optionMap = new HashMap<>();
		Arrays.asList(options).forEach(option -> optionMap.put(option.getName(), option));
	}

	public void parse(List<String> arglist) {
		i = 0;
		while (i < arglist.size()) {
			optionMap.values().forEach(option -> parseValue(option, arglist));
		}
	}

	public void parse(String... args) {
		parse(Arrays.asList(args));
	}

	private <T> void parseValue(Option<T> option, List<String> arglist) {
		if (i < arglist.size()) {
			var arg1 = arglist.get(i);
			if (!optionMap.keySet().contains(arg1)) {
				logger.error("Skip garbage '%s'", arg1);
				++i;
				return;
			}
			if (option.getName().equals(arg1)) {
				++i;
				if (i < arglist.size()) {
					var arg2 = arglist.get(i);
					if (optionMap.keySet().contains(arg2)) {
						logger.error("Missing value for parameter '%s'.", option.getName());
					} else {
						++i;
						option.parse(arg2);
					}
				}
			}
		}
	}
}