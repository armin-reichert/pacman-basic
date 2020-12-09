package de.amr.games.pacman;

import static de.amr.games.pacman.common.Logging.log;
import static java.time.format.DateTimeFormatter.ISO_DATE_TIME;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.time.ZonedDateTime;
import java.util.Properties;

public class Hiscore {

	public int points;
	public int level;
	public ZonedDateTime time;
	public boolean changed;

	public Hiscore() {
		points = 0;
		level = 1;
		time = ZonedDateTime.now();
		changed = false;
	}

	public void load(File file) {
		try (FileInputStream in = new FileInputStream(file)) {
			Properties content = new Properties();
			content.loadFromXML(in);
			points = Integer.parseInt(content.getProperty("points"));
			level = Integer.parseInt(content.getProperty("level"));
			time = ZonedDateTime.parse(content.getProperty("date"));
			changed = false;
			log("Hiscore file loaded: %s", file);
		} catch (Exception x) {
			log("Could not load hiscore file");
		}
	}

	public void save(File file) {
		Properties content = new Properties();
		content.setProperty("points", String.valueOf(points));
		content.setProperty("level", String.valueOf(level));
		content.setProperty("date", time.format(ISO_DATE_TIME));
		try (FileOutputStream out = new FileOutputStream(file)) {
			content.storeToXML(out, "Pac-Man Hiscore");
			log("Hiscore file saved: %s", file);
		} catch (Exception x) {
			log("Could not save hiscore");
			x.printStackTrace(System.err);
		}
	}

	public void update(int score, int levelNumber) {
		if (score > points) {
			points = score;
			level = levelNumber;
			time = ZonedDateTime.now();
			changed = true;
		}
	}
}