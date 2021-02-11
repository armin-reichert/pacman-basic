package de.amr.games.pacman.ui.fx;

import static de.amr.games.pacman.lib.Logging.log;
import static de.amr.games.pacman.world.PacManGameWorld.TS;

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
import de.amr.games.pacman.ui.fx.scene.common.PacManGameScene;
import de.amr.games.pacman.ui.fx.scene.common.PlayScene;
import de.amr.games.pacman.ui.fx.scene.mspacman.MsPacManGameIntroScene;
import de.amr.games.pacman.ui.fx.scene.pacman.PacManGameIntermissionScene1;
import de.amr.games.pacman.ui.fx.scene.pacman.PacManGameIntermissionScene2;
import de.amr.games.pacman.ui.fx.scene.pacman.PacManGameIntermissionScene3;
import de.amr.games.pacman.ui.fx.scene.pacman.PacManGameIntroScene;
import javafx.application.Platform;
import javafx.scene.image.Image;
import javafx.stage.Stage;

/**
 * JavaFX implementation of the Pac-Man game UI.
 * 
 * @author Armin Reichert
 */
public class PacManGameFXUI implements PacManGameUI {

	private final Stage stage;
	private final double scaling;
	private final double sizeX, sizeY;

	private PacManGameModel game;

	private final SoundManager pacManSoundManager;
	private final SoundManager msPacManSoundManager;
	private SoundManager soundManager;

	private PacManGameScene currentScene;

	// Pac-Man scenes
	private PacManGameScene pacManIntroScene;
	private PacManGameScene pacManPlayScene;
	private PacManGameScene pacManIntermissionScene1;
	private PacManGameScene pacManIntermissionScene2;
	private PacManGameScene pacManIntermissionScene3;

	// Ms. Pac_an scenes
	private PacManGameScene msPacManIntroScene;
	private PacManGameScene msPacManPlayScene;
	private PacManGameScene msPacManIntermissionScene1;
	private PacManGameScene msPacManIntermissionScene2;
	private PacManGameScene msPacManIntermissionScene3;

	public PacManGameFXUI(Stage stage, PacManGameModel game, double scaling) {
		sizeX = 28 * TS * scaling;
		sizeY = 36 * TS * scaling;
		this.scaling = scaling;
		this.stage = stage;
		stage.setTitle("JavaFX: Pac-Man / Ms. Pac-Man");
		stage.getIcons().add(new Image("/pacman/graphics/pacman.png"));
		stage.setOnCloseRequest(e -> {
			Platform.exit();
			System.exit(0);
		});

		pacManSoundManager = new PacManGameSoundManager(PacManGameSoundAssets::getPacManSoundURL);
		msPacManSoundManager = new PacManGameSoundManager(PacManGameSoundAssets::getMsPacManSoundURL);

		setGame(game);
	}

	@Override
	public void setGame(PacManGameModel game) {
		this.game = game;
		if (game instanceof MsPacManGame) {
			soundManager = msPacManSoundManager;
			msPacManIntroScene = new MsPacManGameIntroScene(game, sizeX, sizeY, scaling);
			msPacManPlayScene = new PlayScene(game, sizeX, sizeY, scaling, true);
			msPacManIntermissionScene1 = msPacManIntroScene; // TODO
			msPacManIntermissionScene2 = msPacManIntroScene; // TODO
			msPacManIntermissionScene3 = msPacManIntroScene; // TODO
		} else if (game instanceof PacManGame) {
			soundManager = pacManSoundManager;
			pacManIntroScene = new PacManGameIntroScene(game, sizeX, sizeY, scaling);
			pacManPlayScene = new PlayScene(game, sizeX, sizeY, scaling, false);
			pacManIntermissionScene1 = new PacManGameIntermissionScene1(game, soundManager, sizeX, sizeY, scaling);
			pacManIntermissionScene2 = new PacManGameIntermissionScene2(game, soundManager, sizeX, sizeY, scaling);
			pacManIntermissionScene3 = new PacManGameIntermissionScene3(game, soundManager, sizeX, sizeY, scaling);
		} else {
			log("%s: Cannot create scenes for invalid game: %s", this, game);
		}
	}

	private boolean updateScene() {
		PacManGameScene newScene = selectScene();
		if (newScene == null) {
			log("%s: No scene matches current game state %s", this, game.state);
			return false;
		}
		if (currentScene != newScene) {
			currentScene.end();
			newScene.start();
			currentScene = newScene;
			log("%s: Scene changed from %s to %s", this, currentScene.getClass().getSimpleName(),
					newScene.getClass().getSimpleName());
			return true;
		}
		return false;
	}

	private PacManGameScene selectScene() {
		if (game instanceof MsPacManGame) {
			if (game.state == PacManGameState.INTRO) {
				return msPacManIntroScene;
			}
			if (game.state == PacManGameState.INTERMISSION) {
				if (game.intermissionNumber == 1) {
					return msPacManIntermissionScene1;
				}
				if (game.intermissionNumber == 2) {
					return msPacManIntermissionScene2;
				}
				if (game.intermissionNumber == 3) {
					return msPacManIntermissionScene3;
				}
			}
			return msPacManPlayScene;
		}
		if (game instanceof PacManGame) {
			if (game.state == PacManGameState.INTRO) {
				return pacManIntroScene;
			}
			if (game.state == PacManGameState.INTERMISSION) {
				if (game.intermissionNumber == 1) {
					return pacManIntermissionScene1;
				}
				if (game.intermissionNumber == 2) {
					return pacManIntermissionScene2;
				}
				if (game.intermissionNumber == 3) {
					return pacManIntermissionScene3;
				}
			}
			return pacManPlayScene;
		}
		throw new IllegalStateException("No scene found for game state " + game.stateDescription());
	}

	@Override
	public void setCloseHandler(Runnable handler) {
	}

	@Override
	public void show() {
		currentScene = selectScene();
		log("Initial scene is %s", currentScene);
		currentScene.start();
		stage.setScene(currentScene.getFXScene());
		stage.sizeToScene();
		stage.show();
	}

	@Override
	public void render() {
		Platform.runLater(() -> {
			boolean changed = updateScene();
			if (currentScene != null) {
				if (changed) {
					stage.setScene(currentScene.getFXScene());
				}
				currentScene.render();
			}
		});
	}

	@Override
	public void showFlashMessage(String message) {
	}

	@Override
	public boolean keyPressed(String keySpec) {
		boolean pressed = currentScene.keyboard().keyPressed(keySpec);
		currentScene.keyboard().clearKey(keySpec); // TODO
		return pressed;
	}

	@Override
	public Optional<SoundManager> sounds() {
		return Optional.ofNullable(soundManager);
	}

	@Override
	public void mute(boolean muted) {
		soundManager = muted ? null : game instanceof PacManGame ? pacManSoundManager : msPacManSoundManager;
	}

	@Override
	public Optional<PacManGameAnimations> animations() {
		return currentScene.animations();
	}

	@Override
	public String toString() {
		return getClass().getSimpleName();
	}
}