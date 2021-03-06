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
 *
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

	public void load() {
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
	}

	public void save() {
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
	}
}