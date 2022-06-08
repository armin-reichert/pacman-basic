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
import static de.amr.games.pacman.model.common.world.World.TS;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Properties;

import de.amr.games.pacman.event.GameEvent;
import de.amr.games.pacman.event.GameEventType;
import de.amr.games.pacman.event.GameEventing;
import de.amr.games.pacman.lib.V2d;
import de.amr.games.pacman.model.common.actors.Score;

/**
 * @author Armin Reichert
 */
public class GameScores {

	private static void loadFromFile(Score score, File file) {
		try (var in = new FileInputStream(file)) {
			var props = new Properties();
			props.loadFromXML(in);
			// parse
			var points = Integer.parseInt(props.getProperty("points"));
			var levelNumber = Integer.parseInt(props.getProperty("level"));
			var date = LocalDate.parse(props.getProperty("date"), DateTimeFormatter.ISO_LOCAL_DATE);
			// parsing ok
			score.points = points;
			score.levelNumber = levelNumber;
			score.date = date;
			log("Score loaded. File: '%s' Points: %d Level: %d", file.getAbsolutePath(), score.points, score.levelNumber);
		} catch (Exception x) {
			log("Score could not be loaded. File '%s' Reason: %s", file, x.getMessage());
		}
	}

	private final GameModel game;
	private final File hiscoreFile;
	private final Score score;
	private final Score hiscore;
	private boolean enabled;

	public GameScores(GameModel game, File hiscoreFile) {
		this.game = game;
		this.hiscoreFile = hiscoreFile;
		score = new Score();
		score.position = new V2d(TS, TS);
		hiscore = new Score();
		hiscore.position = new V2d(16 * TS, TS);
		loadFromFile(hiscore, hiscoreFile);
	}

	public Score gameScore() {
		return score;
	}

	public Score highScore() {
		return hiscore;
	}

	public void enable(boolean enabled) {
		this.enabled = enabled;
	}

	public void reset() {
		score.reset();
		loadFromFile(hiscore, hiscoreFile);
	}

	public void addPoints(int points) {
		if (!enabled) {
			return;
		}
		int scoreBeforeAddingPoints = score.points;
		score.points += points;
		if (score.points > hiscore.points) {
			hiscore.points = score.points;
			hiscore.levelNumber = game.level.number;
			hiscore.date = LocalDate.now();
		}
		if (scoreBeforeAddingPoints < game.extraLifeScore && score.points >= game.extraLifeScore) {
			game.lives++;
			GameEventing.publish(new GameEvent(game, GameEventType.PLAYER_GETS_EXTRA_LIFE, null, game.pac.tile()));
		}
	}

	public void saveHiscore() {
		Score latestHiscore = new Score();
		loadFromFile(latestHiscore, hiscoreFile);
		if (hiscore.points <= latestHiscore.points) {
			return;
		}
		var props = new Properties();
		props.setProperty("points", String.valueOf(hiscore.points));
		props.setProperty("level", String.valueOf(hiscore.levelNumber));
		props.setProperty("date", hiscore.date.format(DateTimeFormatter.ISO_LOCAL_DATE));
		try (var out = new FileOutputStream(hiscoreFile)) {
			props.storeToXML(out, "");
			log("New hiscore saved. File: '%s' Points: %d Level: %d", hiscoreFile.getAbsolutePath(), hiscore.points,
					hiscore.levelNumber);
		} catch (Exception x) {
			log("Highscore could not be saved. File '%s' Reason: %s", hiscoreFile, x.getMessage());
		}
	}
}