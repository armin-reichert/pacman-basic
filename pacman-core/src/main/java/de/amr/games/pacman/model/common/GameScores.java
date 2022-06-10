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

	public final GameModel game;
	public final Score gameScore;
	public final Score highScore;
	private File hiscoreFile;
	private boolean enabled;

	public GameScores(GameModel game) {
		this.game = game;
		gameScore = new Score("SCORE");
		gameScore.position = new V2d(TS, TS);
		highScore = new Score("HIGH SCORE");
		highScore.position = new V2d(16 * TS, TS);
	}

	public void setHiscoreFile(File hiscoreFile) {
		this.hiscoreFile = hiscoreFile;
		loadFromFile(highScore, hiscoreFile);
	}

	public void enable(boolean enabled) {
		this.enabled = enabled;
	}

	public void reload() {
		loadFromFile(highScore, hiscoreFile);
	}

	public void addPoints(int points) {
		if (!enabled) {
			return;
		}
		int scoreBeforeAddingPoints = gameScore.points;
		gameScore.points += points;
		if (gameScore.points > highScore.points) {
			highScore.points = gameScore.points;
			highScore.levelNumber = game.level.number;
			highScore.date = LocalDate.now();
		}
		if (scoreBeforeAddingPoints < game.extraLifeScore && gameScore.points >= game.extraLifeScore) {
			game.lives++;
			GameEventing.publish(new GameEvent(game, GameEventType.PLAYER_GETS_EXTRA_LIFE, null, game.pac.tile()));
		}
	}

	public void saveHiscore() {
		Score latestHiscore = new Score("");
		loadFromFile(latestHiscore, hiscoreFile);
		if (highScore.points <= latestHiscore.points) {
			return;
		}
		var props = new Properties();
		props.setProperty("points", String.valueOf(highScore.points));
		props.setProperty("level", String.valueOf(highScore.levelNumber));
		props.setProperty("date", highScore.date.format(DateTimeFormatter.ISO_LOCAL_DATE));
		try (var out = new FileOutputStream(hiscoreFile)) {
			props.storeToXML(out, "");
			log("New hiscore saved. File: '%s' Points: %d Level: %d", hiscoreFile.getAbsolutePath(), highScore.points,
					highScore.levelNumber);
		} catch (Exception x) {
			log("Highscore could not be saved. File '%s' Reason: %s", hiscoreFile, x.getMessage());
		}
	}
}