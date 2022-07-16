package de.amr.games.pacman.model.world;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import de.amr.games.pacman.model.common.world.FloorPlan;
import de.amr.games.pacman.model.common.world.World;
import de.amr.games.pacman.model.mspacman.MsPacManGame;
import de.amr.games.pacman.model.pacman.PacManGame;

/**
 * Test for floor plan generation.
 * 
 * @author Armin Reichert
 */
public class FloorPlanGenerator {

	private static final Logger LOGGER = LogManager.getFormatterLogger();

	private static File dir = new File(System.getProperty("user.dir"));

	public static void main(String[] args) {
		List.of(8, 4, 2, 1).forEach(res -> {
			createFloorPlan(PacManGame.createWorld(), "floorplan-pacman-map1-res-%d.txt", res);
			createFloorPlan(MsPacManGame.createWorld(1), "floorplan-mspacman-map1-res-%d.txt", res);
			createFloorPlan(MsPacManGame.createWorld(2), "floorplan-mspacman-map2-res-%d.txt", res);
			createFloorPlan(MsPacManGame.createWorld(3), "floorplan-mspacman-map3-res-%d.txt", res);
			createFloorPlan(MsPacManGame.createWorld(4), "floorplan-mspacman-map4-res-%d.txt", res);
		});
	}

	private static void createFloorPlan(World world, String outputFileNamePattern, int resolution) {
		var floorPlan = new FloorPlan(world, resolution);
		var out = new File(dir, String.format(outputFileNamePattern, resolution));
		try (var w = new FileWriter(out, StandardCharsets.UTF_8)) {
			floorPlan.print(w, true);
			LOGGER.info("Floor plan %s created", out.getAbsolutePath());
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}