package de.amr.games.pacman.ui.fx;

import static de.amr.games.pacman.lib.Logging.log;

import java.util.Optional;

import de.amr.games.pacman.controller.PacManGameState;
import de.amr.games.pacman.model.MsPacManGame;
import de.amr.games.pacman.model.PacManGame;
import de.amr.games.pacman.model.PacManGameModel;
import de.amr.games.pacman.sound.PacManGameSoundAssets;
import de.amr.games.pacman.sound.PacManGameSoundManager;
import de.amr.games.pacman.sound.SoundManager;
import de.amr.games.pacman.ui.PacManGameAnimations;
import de.amr.games.pacman.ui.PacManGameUI;
import de.amr.games.pacman.ui.fx.mspacman.scene.MsPacManGameIntroScene;
import de.amr.games.pacman.ui.fx.mspacman.scene.MsPacManGamePlayScene;
import de.amr.games.pacman.ui.fx.pacman.scene.PacManGameIntroScene;
import de.amr.games.pacman.ui.fx.pacman.scene.PacManGamePlayScene;
import javafx.application.Platform;
import javafx.stage.Stage;

public class PacManGameFXUI implements PacManGameUI {

	private final Stage stage;
	private final double scaling;

	private PacManGameModel game;

	private SoundManager soundManager;

	private PacManGameScene currentScene;

	// Pac-Man scenes
	private PacManGameScene pacManIntroScene;
	private PacManGameScene pacManPlayScene;

	// Ms. Pac_an scenes
	private PacManGameScene msPacManIntroScene;
	private PacManGameScene msPacManPlayScene;

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
	public String toString() {
		return getClass().getSimpleName();
	}

	@Override
	public void setGame(PacManGameModel game) {
		this.game = game;
		createScenes(game);
		soundManager = new PacManGameSoundManager(PacManGameSoundAssets::getPacManSoundURL);
	}

	private void createScenes(PacManGameModel game) {
		if (game instanceof MsPacManGame) {
			msPacManIntroScene = new MsPacManGameIntroScene(game, 28 * 8 * scaling, 36 * 8 * scaling, scaling);
			msPacManPlayScene = new MsPacManGamePlayScene(game, 28 * 8 * scaling, 36 * 8 * scaling, scaling);
		} else if (game instanceof PacManGame) {
			pacManIntroScene = new PacManGameIntroScene(game, 28 * 8 * scaling, 36 * 8 * scaling, scaling);
			pacManPlayScene = new PacManGamePlayScene(game, 28 * 8 * scaling, 36 * 8 * scaling, scaling);
		} else {
			log("%s: Cannot create scenes for invalid game: %s", this, game);
		}
	}

	@Override
	public void setCloseHandler(Runnable handler) {
	}

	@Override
	public void updateScene() {
		if (game == null) {
			log("%s: No game?", this);
			return;
		}
		Platform.runLater(() -> {
			PacManGameScene scene = selectScene();
			if (scene == null) {
				log("%s: No scene matches current game state %s", this, game.state);
				return;
			}
			if (currentScene != scene) {
				if (currentScene != null) {
					currentScene.end();
					log("%s: Current scene changed from %s to %s", this, currentScene.getClass().getSimpleName(),
							scene.getClass().getSimpleName());
				} else {
					log("%s: Scene changed to %s", this, scene.getClass().getSimpleName());
				}
				currentScene = scene;
				stage.setScene(currentScene.getFXScene());
				currentScene.start();
				currentScene.update();
			}
		});
	}

	private PacManGameScene selectScene() {
		if (game instanceof MsPacManGame) {
			if (game.state == PacManGameState.INTRO) {
				return msPacManIntroScene;
			}
			return msPacManPlayScene;
		}
		if (game instanceof PacManGame) {
			if (game.state == PacManGameState.INTRO) {
				return pacManIntroScene;
			}
			return pacManPlayScene;
		}
		return null;
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
		return Optional.ofNullable(soundManager);
	}

	@Override
	public void mute(boolean muted) {
		// TODO
	}

	@Override
	public Optional<PacManGameAnimations> animations() {
		return currentScene.animations();
	}
}