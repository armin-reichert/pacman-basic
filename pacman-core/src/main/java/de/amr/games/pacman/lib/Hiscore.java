/*
MIT License

Copyright (c) 2021 Armin Reichert

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

import static de.amr.games.pacman.lib.Logging.log;
import static java.time.format.DateTimeFormatter.ISO_DATE_TIME;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.time.ZonedDateTime;
import java.util.Properties;

/**
 * Hiscore management.
 * 
 * @author Armin Reichert
 */
public class Hiscore {

	private final File file;

	public int points;
	public int level;
	public ZonedDateTime time;

	public Hiscore(File file) {
		this.file = file;
		points = 0;
		level = 1;
		time = ZonedDateTime.now();
	}

	public Hiscore load() {
		try (FileInputStream in = new FileInputStream(file)) {
			Properties content = new Properties();
			content.loadFromXML(in);
			points = Integer.parseInt(content.getProperty("points"));
			level = Integer.parseInt(content.getProperty("level"));
			time = ZonedDateTime.parse(content.getProperty("date"));
			log("Hiscore file: %s", file);
			log("Hiscore loaded: %d points in level %d", points, level);
		} catch (Exception x) {
			log("Could not load hiscore file");
		}
		return this;
	}

	public Hiscore save() {
		Properties content = new Properties();
		content.setProperty("points", String.valueOf(points));
		content.setProperty("level", String.valueOf(level));
		content.setProperty("date", ZonedDateTime.now().format(ISO_DATE_TIME));
		try (FileOutputStream out = new FileOutputStream(file)) {
			content.storeToXML(out, "");
			log("Hiscore file saved: %s", file);
		} catch (Exception x) {
			log("Could not save hiscore");
			x.printStackTrace(System.err);
		}
		return this;
	}
}