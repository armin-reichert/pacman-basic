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
	private final List<String> args;
	private int i;

	public OptionParser(List<Option<?>> options, List<String> args) {
		optionMap = new HashMap<>();
		options.forEach(option -> optionMap.put(option.getName(), option));
		this.args = args;
	}

	public boolean hasMoreArgs() {
		return i < args.size();
	}

	public <T> void parseValue(Option<T> option) {
		if (i < args.size()) {
			var arg1 = args.get(i);
			if (!optionMap.keySet().contains(arg1)) {
				logger.error("Skip garbage '%s'", arg1);
				++i;
				return;
			}
			if (option.getName().equals(arg1)) {
				++i;
				if (i < args.size()) {
					var arg2 = args.get(i);
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