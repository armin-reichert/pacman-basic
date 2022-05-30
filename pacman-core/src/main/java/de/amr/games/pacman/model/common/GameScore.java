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
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Properties;

import de.amr.games.pacman.event.GameEvent;
import de.amr.games.pacman.event.GameEventType;

/**
 * @author Armin Reichert
 */
public class GameScore {

	private final GameModel game;
	public boolean enabled;
	private int points;

	private final File hiscoreFile;
	private int hiscore;
	private int hiscoreLevel = 1;
	private LocalDateTime hiscoreTime;
	private boolean newHiscore;

	public GameScore(GameModel game, File hiscoreFile) {
		this.game = game;
		this.hiscoreFile = hiscoreFile;
	}

	public int points() {
		return points;
	}

	public int hiscore() {
		return hiscore;
	}

	public int hiscoreLevel() {
		return hiscoreLevel;
	}

	public void clear() {
		points = 0;
	}

	public void add(int number) {
		if (!enabled) {
			return;
		}
		int oldscore = points;
		points += number;
		if (points > hiscore) {
			newHiscore = true;
			hiscore = points;
			hiscoreLevel = game.level.number;
			hiscoreTime = LocalDateTime.now();
		}
		// TODO not sure if firing event belongs here
		if (oldscore < GameModel.EXTRA_LIFE_POINTS && points >= GameModel.EXTRA_LIFE_POINTS) {
			game.lives++;
			game.eventSupport.publish(new GameEvent(game, GameEventType.PLAYER_GETS_EXTRA_LIFE, null, game.player.tile()));
		}
	}

	public void loadHiscore() {
		var data = new Properties();
		try (var in = new FileInputStream(hiscoreFile)) {
			data.loadFromXML(in);
			hiscore = Integer.parseInt(data.getProperty("points"));
			hiscoreLevel = Integer.parseInt(data.getProperty("level"));
			hiscoreTime = LocalDateTime.parse(data.getProperty("date"), DateTimeFormatter.ISO_LOCAL_DATE);
			log("Hiscore loaded. File: '%s' Points: %d Level: %d", hiscoreFile.getAbsolutePath(), hiscore, hiscoreLevel);
		} catch (Exception x) {
			log("Highscore could not be loaded. File '%s' Reason: %s", hiscoreFile, x.getMessage());
		}
	}

	public void saveHiscore() {
		if (newHiscore) {
			var data = new Properties();
			data.setProperty("points", String.valueOf(hiscore));
			data.setProperty("level", String.valueOf(hiscoreLevel));
			data.setProperty("date", hiscoreTime.format(DateTimeFormatter.ISO_LOCAL_DATE));
			try (var out = new FileOutputStream(hiscoreFile)) {
				data.storeToXML(out, "");
				log("New hiscore saved. File: '%s' Points: %d Level: %d", hiscoreFile.getAbsolutePath(), hiscore, hiscoreLevel);
			} catch (Exception x) {
				log("Highscore could not be . File '%s' Reason: %s", hiscoreFile, x.getMessage());
			}
		}
	}
}