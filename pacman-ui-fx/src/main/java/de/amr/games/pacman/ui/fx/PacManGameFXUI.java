package de.amr.games.pacman.ui.fx;

import static de.amr.games.pacman.lib.Logging.log;

import java.util.Optional;

import de.amr.games.pacman.model.PacManGameModel;
import de.amr.games.pacman.sound.SoundManager;
import de.amr.games.pacman.ui.PacManGameAnimations;
import de.amr.games.pacman.ui.PacManGameUI;
import de.amr.games.pacman.ui.fx.pacman.scene.PacManGamePlayScene;
import de.amr.games.pacman.ui.fx.pacman.scene.PacManGameScene;
import javafx.application.Platform;
import javafx.stage.Stage;

public class PacManGameFXUI implements PacManGameUI {

	private final Stage stage;
	private final double scaling;

	private PacManGameModel game;
	private PacManGameScene currentScene;

	private PacManGamePlayScene pacManPlayScene;

	public PacManGameFXUI(Stage stage, PacManGameModel game, double scaling) {

		this.scaling = scaling;
		this.stage = stage;

		stage.setTitle("Pac-Man / Ms. Pac-Man");
		stage.setOnCloseRequest(e -> {
			Platform.exit();
			System.exit(0);
		});

		setGame(game);
	}

	@Override
	public void setGame(PacManGameModel game) {
		this.game = game;
		createScenes();
	}

	private void createScenes() {
		pacManPlayScene = new PacManGamePlayScene(game, 28 * 8 * scaling, 36 * 8 * scaling, scaling);
	}

	@Override
	public void setCloseHandler(Runnable handler) {
	}

	@Override
	public void updateScene() {
		Platform.runLater(() -> {
			PacManGameScene scene = selectScene();
			if (currentScene != scene) {
				if (currentScene != null) {
					currentScene.end();
					log("%s: Current scene changed from %s to %s", this, currentScene.getClass().getSimpleName(),
							scene.getClass().getSimpleName());
				}
				currentScene = scene;
				stage.setScene(currentScene.getFXScene());
				currentScene.start();
				currentScene.update();
			}
		});
	}

	private PacManGameScene selectScene() {
		return pacManPlayScene;
	}

	@Override
	public void show() {
		stage.show();
	}

	@Override
	public void render() {
		if (currentScene != null) {
			Platform.runLater(currentScene::render);
		}
	}

	@Override
	public void showFlashMessage(String message) {
	}

	@Override
	public boolean keyPressed(String keySpec) {
		if (currentScene != null) {
			boolean pressed = currentScene.keyboard().keyPressed(keySpec);
			currentScene.keyboard().clearKey(keySpec); // TODO
			return pressed;
		} else {
			return false;
		}
	}

	@Override
	public Optional<SoundManager> sounds() {
		return Optional.empty();
	}

	@Override
	public void mute(boolean muted) {
	}

	@Override
	public Optional<PacManGameAnimations> animations() {
		return Optional.empty();
	}
}