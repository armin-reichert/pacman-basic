package de.amr.games.pacman.test;

import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.AfterClass;
import org.junit.Test;

import de.amr.games.pacman.model.common.world.FloorPlan;
import de.amr.games.pacman.model.common.world.World;
import de.amr.games.pacman.model.mspacman.MsPacManGame;
import de.amr.games.pacman.model.pacman.PacManGame;

/**
 * Test for floor plan generation.
 * 
 * @author Armin Reichert
 */
public class TestFloorPlan {

	public static void main(String[] args) {
		new TestFloorPlan().test();
	}

	private static final Logger LOG = LogManager.getFormatterLogger();
	private static final File DIR = new File(System.getProperty("user.dir"));
	private static final String PACMAN_PATTERN = "fp-pacman-map%d-res-%d.txt";
	private static final String MS_PACMAN_PATTERN = "fp-mspacman-map%d-res-%d.txt";

	@AfterClass
	public static void cleanUp() {
		List.of(8, 4, 2, 1).forEach(res -> {
			deleteFile(PACMAN_PATTERN, 1, res);
			deleteFile(MS_PACMAN_PATTERN, 1, res);
			deleteFile(MS_PACMAN_PATTERN, 2, res);
			deleteFile(MS_PACMAN_PATTERN, 3, res);
			deleteFile(MS_PACMAN_PATTERN, 4, res);
		});
	}

	@Test
	public void test() {
		List.of(8, 4, 2, 1).forEach(res -> {
			createFloorPlan(new World(PacManGame.MAP), file(PACMAN_PATTERN, 1, res), res);
			createFloorPlan(new World(MsPacManGame.MAP1), file(MS_PACMAN_PATTERN, 1, res), res);
			createFloorPlan(new World(MsPacManGame.MAP2), file(MS_PACMAN_PATTERN, 2, res), res);
			createFloorPlan(new World(MsPacManGame.MAP3), file(MS_PACMAN_PATTERN, 3, res), res);
			createFloorPlan(new World(MsPacManGame.MAP4), file(MS_PACMAN_PATTERN, 4, res), res);
		});
		List.of(8, 4, 2, 1).forEach(res -> {
			assertTrue(file(PACMAN_PATTERN, 1, res).exists());
			assertTrue(file(MS_PACMAN_PATTERN, 1, res).exists());
			assertTrue(file(MS_PACMAN_PATTERN, 2, res).exists());
			assertTrue(file(MS_PACMAN_PATTERN, 3, res).exists());
			assertTrue(file(MS_PACMAN_PATTERN, 4, res).exists());
		});
	}

	private static File file(String pattern, int mapNumber, int resolution) {
		return new File(DIR, String.format(pattern, mapNumber, resolution));
	}

	private static void deleteFile(String pattern, int mapNumber, int res) {
		var file = file(pattern, mapNumber, res);
		file.delete();
		LOG.info("Deleted file %s", file);
	}

	private static void createFloorPlan(World world, File file, int resolution) {
		long time = System.nanoTime();
		var floorPlan = new FloorPlan(world, resolution);
		time = System.nanoTime() - time;
		var timeLog = "%.2f millis".formatted(time / 1e6);
		try (var w = new FileWriter(file, StandardCharsets.UTF_8)) {
			floorPlan.print(w, true);
			LOG.info("Created file %s (%s)", file.getAbsolutePath(), timeLog);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}