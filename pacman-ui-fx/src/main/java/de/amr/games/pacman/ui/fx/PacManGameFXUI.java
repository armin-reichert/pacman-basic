package de.amr.games.pacman.ui.fx;

import static de.amr.games.pacman.world.PacManGameWorld.TS;

import java.util.Optional;

import de.amr.games.pacman.model.PacManGameModel;
import de.amr.games.pacman.sound.SoundManager;
import de.amr.games.pacman.ui.PacManGameAnimations;
import de.amr.games.pacman.ui.PacManGameUI;
import de.amr.games.pacman.ui.fx.input.Keyboard;
import javafx.scene.Scene;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.stage.Stage;

public class PacManGameFXUI implements PacManGameUI {

	private final Stage stage;
	private final Keyboard keyboard;

	private PacManGameModel game;
	private Scene scene;
	private Text text;

	public PacManGameFXUI(Stage stage, PacManGameModel game, int scaling) {
		this.stage = stage;
		this.game = game;
		scene = createPlayScene(scaling);
		stage.setScene(scene);
		stage.setTitle("Pac-Man / Ms. Pac-Man");
		stage.setOnCloseRequest(e -> {
			System.exit(0);
		});
		keyboard = new Keyboard(scene);
	}

	private Scene createPlayScene(float scaling) {
		Pane pane = new StackPane();
		text = new Text("Hello, JavaFX!");
		text.setFont(Font.font("Serif", 20));
		text.setStroke(Color.BLACK);
		pane.getChildren().add(text);
		Scene playScene = new Scene(pane, 28 * TS * scaling, 36 * TS * scaling);
		return playScene;
	}

	@Override
	public void setGame(PacManGameModel game) {
		this.game = game;
	}

	@Override
	public void setCloseHandler(Runnable handler) {
	}

	@Override
	public void updateScene() {
		text.setText(game.state != null ? game.stateDescription() : "no state");
	}

	@Override
	public void show() {
		stage.show();
	}

	@Override
	public void render() {
	}

	@Override
	public void showFlashMessage(String message) {
	}

	@Override
	public boolean keyPressed(String keySpec) {
		boolean pressed = keyboard.keyPressed(keySpec);
		keyboard.clearKey(keySpec); // TODO
		return pressed;
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