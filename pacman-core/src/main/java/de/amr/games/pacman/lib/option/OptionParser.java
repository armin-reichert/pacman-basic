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

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.tinylog.Logger;

/**
 * @author Armin Reichert
 */
public class OptionParser {

	private final Map<String, Option<?>> optionMap = new HashMap<>();
	private int cursor;

	public OptionParser(Option<?>... options) {
		Arrays.asList(options).forEach(option -> optionMap.put(option.getName(), option));
	}

	public void parse(List<String> arglist) {
		cursor = 0;
		while (cursor < arglist.size()) {
			optionMap.values().forEach(option -> parseValue(option, arglist));
		}
	}

	public void parse(String... args) {
		parse(Arrays.asList(args));
	}

	private <T> void parseValue(Option<T> option, List<String> arglist) {
		if (cursor < arglist.size()) {
			var arg1 = arglist.get(cursor);
			if (!optionMap.keySet().contains(arg1)) {
				Logger.error("Skip garbage '{}'", arg1);
				++cursor;
				return;
			}
			if (option.getName().equals(arg1)) {
				++cursor;
				if (cursor < arglist.size()) {
					var arg2 = arglist.get(cursor);
					if (optionMap.keySet().contains(arg2)) {
						Logger.error("Missing value for parameter '{}'.", option.getName());
					} else {
						++cursor;
						option.parse(arg2);
					}
				}
			}
		}
	}
}