package de.amr.games.pacman.ui.fx;

import java.util.Map;
import java.util.function.Predicate;
import java.util.stream.Stream;

import de.amr.games.pacman.lib.V2i;
import de.amr.games.pacman.model.Bonus;
import de.amr.games.pacman.model.Ghost;
import de.amr.games.pacman.model.Pac;
import de.amr.games.pacman.model.PacManGameModel;
import de.amr.games.pacman.ui.PacManGameAnimations;
import javafx.geometry.Rectangle2D;
import javafx.scene.image.Image;
import javafx.scene.image.WritableImage;
import javafx.scene.paint.Color;

public interface RenderingWithAnimatedSprites extends PacManGameAnimations {

	Rectangle2D bonusSprite(Bonus bonus, PacManGameModel game);

	Rectangle2D pacSprite(Pac pac, PacManGameModel game);

	Rectangle2D ghostSprite(Ghost ghost, PacManGameModel game);

	void drawPac(Pac pac, PacManGameModel game);

	void drawGhost(Ghost ghost, PacManGameModel game);

	void drawBonus(Bonus bonus, PacManGameModel game);

	void hideTile(V2i tile);

	void drawMaze(int mazeNumber, int x, int y, boolean flashing);

	void drawFoodTiles(Stream<V2i> tiles, Predicate<V2i> eaten);

	void drawEnergizerTiles(Stream<V2i> energizerTiles);

	static Image exchangeColors(Image source, Map<Color, Color> exchanges) {
		WritableImage newImage = new WritableImage((int) source.getWidth(), (int) source.getHeight());
		for (int x = 0; x < source.getWidth(); ++x) {
			for (int y = 0; y < source.getHeight(); ++y) {
				Color oldColor = source.getPixelReader().getColor(x, y);
				for (Map.Entry<Color, Color> entry : exchanges.entrySet()) {
					if (oldColor.equals(entry.getKey())) {
						newImage.getPixelWriter().setColor(x, y, entry.getValue());
					}
				}
			}
		}
		return newImage;
	}
}