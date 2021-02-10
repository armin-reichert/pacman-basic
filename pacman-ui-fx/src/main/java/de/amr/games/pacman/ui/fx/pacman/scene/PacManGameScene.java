package de.amr.games.pacman.ui.fx.pacman.scene;

import de.amr.games.pacman.ui.fx.input.Keyboard;
import javafx.scene.Scene;

public interface PacManGameScene {

	Scene getFXScene();

	void start();

	void end();

	void update();

	void render();

	Keyboard keyboard();

}
