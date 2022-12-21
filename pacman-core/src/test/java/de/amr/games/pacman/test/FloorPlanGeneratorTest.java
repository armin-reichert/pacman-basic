package de.amr.games.pacman.test;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

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
public class FloorPlanGeneratorTest {

	private static final Logger LOGGER = LogManager.getFormatterLogger();

	private static File dir = new File(System.getProperty("user.dir"));

	public static void main(String[] args) {
		List.of(8, 4, 2, 1).forEach(res -> {
			createFloorPlan(new ArcadeWorld(PacManGame.MAP), "floorplan-pacman-map1-res-%d.txt", res);
			createFloorPlan(new ArcadeWorld(MsPacManGame.MAP1), "floorplan-mspacman-map1-res-%d.txt", res);
			createFloorPlan(new ArcadeWorld(MsPacManGame.MAP2), "floorplan-mspacman-map2-res-%d.txt", res);
			createFloorPlan(new ArcadeWorld(MsPacManGame.MAP3), "floorplan-mspacman-map3-res-%d.txt", res);
			createFloorPlan(new ArcadeWorld(MsPacManGame.MAP4), "floorplan-mspacman-map4-res-%d.txt", res);
		});
	}

	private static void createFloorPlan(World world, String outputFileNamePattern, int resolution) {
		long time = System.nanoTime();
		var floorPlan = new FloorPlan(world, resolution);
		time = System.nanoTime() - time;
		LOGGER.info("Floorplan creation (resolution=%d) took %.2f milliseconds", resolution, time / 1e6);
		var out = new File(dir, String.format(outputFileNamePattern, resolution));
		try (var w = new FileWriter(out, StandardCharsets.UTF_8)) {
			floorPlan.print(w, true);
			LOGGER.info("Floor plan %s created", out.getAbsolutePath());
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}