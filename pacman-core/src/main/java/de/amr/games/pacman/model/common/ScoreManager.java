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

import static de.amr.games.pacman.lib.Logging.log;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Properties;

import de.amr.games.pacman.event.GameEvent;
import de.amr.games.pacman.event.GameEventType;

/**
 * @author Armin Reichert
 */
public class ScoreManager {

	public static class Score {
		public int points;
		public int levelNumber;
		public LocalDate date;

		public Score() {
			reset();
		}

		public void reset() {
			points = 0;
			levelNumber = 1;
			date = LocalDate.now();
		}
	}

	private final File hiscoreFile;
	private final Score score;
	private final Score hiscore;
	public boolean enabled;

	public ScoreManager(File hiscoreFile) {
		this.hiscoreFile = hiscoreFile;
		score = new Score();
		hiscore = new Score();
		loadHiscore(hiscore);
	}

	public Score score() {
		return score;
	}

	public Score hiscore() {
		return hiscore;
	}

	public void reset() {
		score.reset();
		loadHiscore(hiscore);
	}

	public void add(GameModel game, int points) {
		if (!enabled) {
			return;
		}
		int pointsBefore = score.points;
		score.points += points;
		if (score.points > hiscore.points) {
			hiscore.points = score.points;
			hiscore.levelNumber = game.level.number;
		}
		if (pointsBefore < GameModel.EXTRA_LIFE_POINTS && score.points >= GameModel.EXTRA_LIFE_POINTS) {
			game.lives++;
			// TODO not sure if firing event belongs here
			game.eventSupport.publish(new GameEvent(game, GameEventType.PLAYER_GETS_EXTRA_LIFE, null, game.player.tile()));
		}
	}

	private void loadHiscore(Score hiscore) {
		try (var in = new FileInputStream(hiscoreFile)) {
			var props = new Properties();
			props.loadFromXML(in);
			// parse
			var points = Integer.parseInt(props.getProperty("points"));
			var levelNumber = Integer.parseInt(props.getProperty("level"));
			var date = LocalDate.parse(props.getProperty("date"), DateTimeFormatter.ISO_LOCAL_DATE);
			// parsing ok
			hiscore.points = points;
			hiscore.levelNumber = levelNumber;
			hiscore.date = date;
			log("Hiscore loaded. File: '%s' Points: %d Level: %d", hiscoreFile.getAbsolutePath(), hiscore.points,
					hiscore.levelNumber);
		} catch (Exception x) {
			log("Highscore could not be loaded. File '%s' Reason: %s", hiscoreFile, x.getMessage());
		}
	}

	public void saveHiscore() {
		Score latestHiscore = new Score();
		loadHiscore(latestHiscore); // get most recent one from disk
		if (score.points > latestHiscore.points) {
			var props = new Properties();
			props.setProperty("points", String.valueOf(score.points));
			props.setProperty("level", String.valueOf(score.levelNumber));
			props.setProperty("date", score.date.format(DateTimeFormatter.ISO_LOCAL_DATE));
			try (var out = new FileOutputStream(hiscoreFile)) {
				props.storeToXML(out, "");
				log("New hiscore saved. File: '%s' Points: %d Level: %d", hiscoreFile.getAbsolutePath(), hiscore.points,
						hiscore.levelNumber);
			} catch (Exception x) {
				log("Highscore could not be . File '%s' Reason: %s", hiscoreFile, x.getMessage());
			}
		}
	}
}