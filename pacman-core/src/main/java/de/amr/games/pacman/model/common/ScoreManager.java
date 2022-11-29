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
package de.amr.games.pacman.model.common;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Properties;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * @author Armin Reichert
 */
public class ScoreManager {

	private ScoreManager() {
	}

	private static final Logger LOGGER = LogManager.getFormatterLogger();

	public static void saveHiscore(Score highScore, File hiscoreFile, GameVariant variant) {
		Score existingHiscore = new Score("");
		loadScore(existingHiscore, hiscoreFile);
		if (highScore.points() <= existingHiscore.points()) {
			return;
		}
		var props = new Properties();
		props.setProperty("points", String.valueOf(highScore.points()));
		props.setProperty("level", String.valueOf(highScore.levelNumber()));
		props.setProperty("date", highScore.date().format(DateTimeFormatter.ISO_LOCAL_DATE));
		try (var out = new FileOutputStream(hiscoreFile)) {
			props.storeToXML(out, "%s Hiscore".formatted(variant));
			LOGGER.info("New hiscore saved. File: '%s' Points: %d Level: %d", hiscoreFile.getAbsolutePath(),
					highScore.points(), highScore.levelNumber());
		} catch (Exception x) {
			LOGGER.info("Highscore could not be saved. File '%s' Reason: %s", hiscoreFile, x.getMessage());
		}
	}

	public static void loadScore(Score score, File file) {
		try (var in = new FileInputStream(file)) {
			var props = new Properties();
			props.loadFromXML(in);
			var points = Integer.parseInt(props.getProperty("points"));
			var levelNumber = Integer.parseInt(props.getProperty("level"));
			var date = LocalDate.parse(props.getProperty("date"), DateTimeFormatter.ISO_LOCAL_DATE);
			score.setPoints(points);
			score.setLevelNumber(levelNumber);
			score.setDate(date);
			LOGGER.info("Score loaded. File: '%s' Points: %d Level: %d", file.getAbsolutePath(), score.points(),
					score.levelNumber());
		} catch (Exception x) {
			LOGGER.info("Score could not be loaded. File '%s' Reason: %s", file, x.getMessage());
		}
	}
}