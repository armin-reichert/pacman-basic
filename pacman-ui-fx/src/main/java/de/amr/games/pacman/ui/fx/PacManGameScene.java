package de.amr.games.pacman.ui.fx;

import de.amr.games.pacman.ui.fx.input.Keyboard;
import de.amr.games.pacman.ui.fx.pacman.rendering.Rendering;
import javafx.scene.Scene;

public interface PacManGameScene {

	Scene getFXScene();

	void start();

	void end();

	void update();

	void render();

	Keyboard keyboard();

	Rendering rendering();

}