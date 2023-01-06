package de.amr.games.pacman.test;

import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.Before;
import org.junit.Test;

import de.amr.games.pacman.model.common.world.ArcadeWorld;
import de.amr.games.pacman.model.common.world.FloorPlan;
import de.amr.games.pacman.model.common.world.World;
import de.amr.games.pacman.model.mspacman.MsPacManGame;
import de.amr.games.pacman.model.pacman.PacManGame;

/**
 * Test for floor plan generation.
 * 
 * @author Armin Reichert
 */
public class FloorPlanTest {

	private static final Logger LOGGER = LogManager.getFormatterLogger();
	private static final File DIR = new File(System.getProperty("user.dir"));
	private static final String PACMAN_PATTERN = "fp-pacman-map%d-res-%d.txt";
	private static final String MS_PACMAN_PATTERN = "fp-mspacman-map%d-res-%d.txt";

	@Before
	public void setUp() {
		List.of(8, 4, 2, 1).forEach(res -> {
			createFloorPlan(new ArcadeWorld(PacManGame.MAP), file(PACMAN_PATTERN, 1, res), res);
			createFloorPlan(new ArcadeWorld(MsPacManGame.MAP1), file(MS_PACMAN_PATTERN, 1, res), res);
			createFloorPlan(new ArcadeWorld(MsPacManGame.MAP2), file(MS_PACMAN_PATTERN, 2, res), res);
			createFloorPlan(new ArcadeWorld(MsPacManGame.MAP3), file(MS_PACMAN_PATTERN, 3, res), res);
			createFloorPlan(new ArcadeWorld(MsPacManGame.MAP4), file(MS_PACMAN_PATTERN, 4, res), res);
		});
	}

	@Test
	public void test() {
		List.of(8, 4, 2, 1).forEach(res -> {
			assertTrue(file(PACMAN_PATTERN, 1, res).exists());
			assertTrue(file(MS_PACMAN_PATTERN, 1, res).exists());
			assertTrue(file(MS_PACMAN_PATTERN, 2, res).exists());
			assertTrue(file(MS_PACMAN_PATTERN, 3, res).exists());
			assertTrue(file(MS_PACMAN_PATTERN, 4, res).exists());
		});
	}

	private File file(String pattern, int mapNumber, int resolution) {
		return new File(DIR, String.format(pattern, mapNumber, resolution));
	}

	private void createFloorPlan(World world, File file, int resolution) {
		long time = System.nanoTime();
		var floorPlan = new FloorPlan(world, resolution);
		time = System.nanoTime() - time;
		var timeLog = "%.2f millis".formatted(time / 1e6);
		try (var w = new FileWriter(file, StandardCharsets.UTF_8)) {
			floorPlan.print(w, true);
			LOGGER.info("Created file %s (%s)", file.getAbsolutePath(), timeLog);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}